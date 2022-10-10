/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.actions.func;

import com.google.common.base.Strings;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.redhat.devtools.intellij.common.model.ProcessHandlerInput;
import com.redhat.devtools.intellij.common.ui.InputDialogWithCheckbox;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.ExecProcessHandler;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.func.FuncActionPipelineBuilder;
import com.redhat.devtools.intellij.knative.func.FuncActionTask;
import com.redhat.devtools.intellij.knative.func.IFuncActionPipeline;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import com.redhat.devtools.intellij.knative.utils.model.ImageRegistryModel;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_BUILD_DEPLOY;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.PROP_CALLER_ACTION;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class BuildAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(BuildAction.class);
    private static final String ID = "com.redhat.devtools.intellij.knative.actions.func.BuildAction";

    public BuildAction() {
        super(KnFunctionNode.class);
    }

    public static void execute(Project project, Function function, Kn knCli,
                               FuncActionTask buildStepHandler) {
        if (project == null
                || function == null
                || knCli == null
                || buildStepHandler == null) {
            return;
        }
        TelemetryMessageBuilder.ActionMessage telemetry = createTelemetryBuild();
        telemetry.property(PROP_CALLER_ACTION, buildStepHandler.getPipeline().getActionName());
        BuildAction buildAction = (BuildAction) ActionManager.getInstance().getAction(ID);
        ImageRegistryModel model = new ImageRegistryModel(function.getImage(), "");
        if (Strings.isNullOrEmpty(model.getImage())) {
            model = UIHelper.executeInUI(() -> buildAction.confirmAndGetRegistryImage(project, function, knCli, false, telemetry));
        }
        buildAction.doExecuteAction(project, function, model, knCli, buildStepHandler, telemetry);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        ParentableNode node = getElement(selected);
        Function function = ((KnFunctionNode) node).getFunction();
        TelemetryMessageBuilder.ActionMessage telemetry = createTelemetry();
        Project project = getEventProject(anActionEvent);

        ImageRegistryModel model = confirmAndGetRegistryImage(project, function, knCli, false, telemetry);
        if (model == null || !model.isValid()) {
            return;
        }
        IFuncActionPipeline buildPipeline = new FuncActionPipelineBuilder()
                .createBuildPipeline(project, function)
                .withBuildTask((task) ->
                        ExecHelper.submit(() -> doExecuteAction(project, function, model, knCli, task, telemetry))
                )
                .build();
        knCli.getFuncActionPipelineManager().start(buildPipeline);
    }

    protected ImageRegistryModel confirmAndGetRegistryImage(Project project, Function function, Kn knCli, boolean forceAskImageToUser, TelemetryMessageBuilder.ActionMessage telemetry) {
        String namespace = knCli.getNamespace();
        if (!isLocalFunction(function, namespace, telemetry)) {
            return null;
        }

        ImageRegistryModel model = getRegistryAndImage(function, knCli, forceAskImageToUser, telemetry);
        if (model == null || !model.isValid()) {
            return null;
        }
        if (!isExecutionConfirmed(project, function, namespace, telemetry)) {
            return null;
        }
        return model;
    }

    private boolean isLocalFunction(Function function, String namespace, TelemetryMessageBuilder.ActionMessage telemetry) {
        String name = function.getName();
        String localPathFunc = function.getLocalPath();
        if (localPathFunc.isEmpty()) {
            telemetry
                    .result(anonymizeResource(name, namespace, "Function " + name + "is not opened locally"))
                    .send();
            return false;
        }
        return true;
    }

    protected ImageRegistryModel getRegistryAndImage(Function function, Kn knCli, boolean forceAskImageToUser, TelemetryMessageBuilder.ActionMessage telemetry) {
        ImageRegistryModel dataToDeploy = getDataToDeploy(Paths.get(function.getLocalPath()), knCli);
        String registry = dataToDeploy.getRegistry();
        String image = dataToDeploy.getImage();
        if (forceAskImageToUser || (Strings.isNullOrEmpty(image) && Strings.isNullOrEmpty(registry))) {
            // ask input to user
            return getImageFromUser(function.getName());
        }
        return dataToDeploy;
    }

    private boolean isExecutionConfirmed(Project project, Function function, String namespace, TelemetryMessageBuilder.ActionMessage telemetry) {
        if (!isActionConfirmed(project, function.getName(), function.getNamespace(), namespace)) {
            telemetry
                    .result(anonymizeResource(function.getName(), namespace, "Build action execution has been stopped by user."))
                    .send();
            return false;
        }
        return true;
    }

    protected void doExecuteAction(Project project, Function function, ImageRegistryModel model,
                                   Kn knCli, FuncActionTask task,
                                   TelemetryMessageBuilder.ActionMessage telemetry) {
        String name = function.getName();
        String namespace = knCli.getNamespace();
        String localPathFunc = function.getLocalPath();
        try {
            function.setBuilding(true);
            doExecute(task, knCli, namespace, localPathFunc, model);
            telemetry
                    .result(anonymizeResource(name, namespace, getSuccessMessage(namespace, name)))
                    .send();
            TreeHelper.refreshFuncTree(project);
        } catch (IOException e) {
            Notification notification = new Notification(NOTIFICATION_ID,
                    "Error",
                    e.getLocalizedMessage(),
                    NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            logger.warn(e.getLocalizedMessage(), e);
            telemetry
                    .error(anonymizeResource(name, namespace, e.getLocalizedMessage()))
                    .send();
        }
    }

    protected void doExecute(FuncActionTask task, Kn knCli, String namespace, String localPathFunc, ImageRegistryModel model) throws IOException {
        ConsoleView terminalExecutionConsole = task != null ? task.getTerminalExecutionConsole() : null;
        java.util.function.Function<ProcessHandlerInput, ExecProcessHandler> processHandlerFunction = task != null ? task.getProcessHandlerFunction() : null;
        ProcessListener processListener = task != null ? task.getProcessListener() : null;
        knCli.buildFunc(localPathFunc, model, terminalExecutionConsole, processHandlerFunction, processListener);
    }

    protected boolean isActionConfirmed(Project project, String name, String funcNamespace, String activeNamespace) {
        return true;
    }

    protected ImageRegistryModel getImageFromUser(String name) {
        String defaultUsername = System.getProperty("user.name");
        String defaultImage = "quay.io/" + defaultUsername + "/" + name + ":latest";
        InputDialogWithCheckbox dialog = new InputDialogWithCheckbox("Provide full image name in the form registry/namespace/name:[tag] (e.g quay.io/boson/image:latest)",
                "Build Function " + name,
                "Auto discover an internal registry and use it to deploy",
                false,
                true,
                null,
                defaultImage,
                new InputValidator() {
                    @Override
                    public boolean checkInput(String inputString) {
                        return !inputString.isEmpty();
                    }

                    @Override
                    public boolean canClose(String inputString) {
                        return true;
                    }
                });
        dialog.setDisableTextPanelWithCheckbox();
        dialog.show();
        if (!dialog.isOK()) {
            return null;
        }
        if (dialog.isChecked()) {
            ImageRegistryModel model = new ImageRegistryModel();
            model.setAutoDiscovery();
            return model;
        }
        return new ImageRegistryModel(dialog.getInputString(), "");
    }

    protected ImageRegistryModel getDataToDeploy(Path root, Kn kncli) {
        try {
            URL funcFileURL = kncli.getFuncFileURL(root);
            String content = YAMLHelper.JSONToYAML(YAMLHelper.URLToJSON(funcFileURL));
            String registry = YAMLHelper.getStringValueFromYAML(content, new String[]{"registry"});
            String image = YAMLHelper.getStringValueFromYAML(content, new String[]{"image"});
            return new ImageRegistryModel(image, registry);
        } catch(IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return new ImageRegistryModel();
    }

    protected TelemetryMessageBuilder.ActionMessage createTelemetry() {
        return createTelemetryBuild();
    }

    private static TelemetryMessageBuilder.ActionMessage createTelemetryBuild() {
        return TelemetryService.instance().action(NAME_PREFIX_BUILD_DEPLOY + "build func");
    }

    private String getSuccessMessage(String namespace, String name) {
        return "Function " + name + " has been successfully built";
    }

    @Override
    public boolean isEnabled(Object selected) {
        if (selected instanceof KnFunctionNode) {
            return !((KnFunctionNode) selected).getFunction().isBuilding();
        }
        return false;
    }

    @Override
    public boolean isVisible(Object selected) {
        if (selected instanceof KnFunctionNode) {
            return !((KnFunctionNode) selected).getFunction().getLocalPath().isEmpty();
        }
        return false;
    }
}
