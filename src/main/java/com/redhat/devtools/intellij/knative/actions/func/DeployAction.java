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
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.func.FuncActionPipelineBuilder;
import com.redhat.devtools.intellij.knative.func.FuncActionTask;
import com.redhat.devtools.intellij.knative.func.IFuncActionPipeline;
import com.redhat.devtools.intellij.knative.utils.FuncUtils;
import com.redhat.devtools.intellij.knative.utils.model.ImageRegistryModel;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;

import static com.intellij.openapi.ui.Messages.getCancelButton;
import static com.intellij.openapi.ui.Messages.getOkButton;
import static com.intellij.openapi.ui.Messages.showOkCancelDialog;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_BUILD_DEPLOY;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class DeployAction extends BuildAction {

    private static final Logger logger = LoggerFactory.getLogger(DeployAction.class);

    public DeployAction() {
        super();
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
        if (!Strings.isNullOrEmpty(model.getImage())) {
            function.setImage(model.getImage());
        }

        IFuncActionPipeline deployPipeline = new FuncActionPipelineBuilder()
                .createDeployPipeline(project, function)
                .withBuildTask((task) -> doBuild(knCli, task))
                .withTask("deployFunc", (task) -> doDeploy(node.getName(), knCli, task,
                        model, telemetry))
                .build();
        knCli.getFuncActionPipelineManager().start(deployPipeline);
    }

    protected void doBuild(Kn knCli, FuncActionTask funcActionTask) {
        ExecHelper.submit(() -> BuildAction.execute(
                funcActionTask.getProject(),
                funcActionTask.getFunction(),
                knCli,
                funcActionTask)
        );
    }

    protected void doDeploy(String name, Kn knCli, FuncActionTask funcActionTask, ImageRegistryModel model, TelemetryMessageBuilder.ActionMessage telemetry) {
        ExecHelper.submit(() -> {
            String namespace = knCli.getNamespace();
            try {
                knCli.deployFunc(namespace, funcActionTask.getFunction().getLocalPath(), model,
                        funcActionTask.getTerminalExecutionConsole(), funcActionTask.getProcessListener());
                telemetry
                        .result(anonymizeResource(name, knCli.getNamespace(), getSuccessMessage(namespace, name)))
                        .send();
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
                telemetry
                        .error(anonymizeResource(name, namespace, e.getLocalizedMessage()))
                        .send();
            }
        });
    }

    @Override
    protected boolean isActionConfirmed(Project project, String name, String funcNamespace, String activeNamespace) {
        String message = "";
        if (!Strings.isNullOrEmpty(funcNamespace)) {
            if (!funcNamespace.equalsIgnoreCase(activeNamespace)) {
                message = "Function namespace (declared in func.yaml) is different from the current active namespace. \n";
            }
        }
        message += "Deploy function " + name + " to namespace " + activeNamespace + "?";

        int result = showOkCancelDialog(project,
                message,
                "Deploy Function " + name,
                getOkButton(), getCancelButton(), null);
        return result == Messages.OK;
    }

    protected TelemetryMessageBuilder.ActionMessage createTelemetry() {
        return TelemetryService.instance().action(NAME_PREFIX_BUILD_DEPLOY + "deploy func");
    }

    protected String getSuccessMessage(String namespace, String name) {
        return "Function " + name + " in namespace " + namespace + " has been successfully deployed";
    }

    @Override
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible) {
            Kn kn = ((KnFunctionNode) selected).getRootNode().getKn();
            return FuncUtils.isKnativeReady(kn);
        }
        return false;
    }


}
