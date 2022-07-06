/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.actions.func;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.runFuncWindowTab.RunFuncActionPipeline;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.runFuncWindowTab.RunFuncActionTask;
import com.redhat.devtools.intellij.knative.ui.invokeFunc.InvokeDialog;
import com.redhat.devtools.intellij.knative.utils.model.InvokeModel;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;

import java.io.IOException;

import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_MISC;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class InvokeAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(InvokeAction.class);
    private TelemetryMessageBuilder.ActionMessage telemetry;

    public InvokeAction() {
        super(KnFunctionNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        telemetry = createTelemetry();
        ParentableNode node = getElement(selected);
        String name = node.getName();
        String namespace = knCli.getNamespace();
        Function function = ((KnFunctionNode) node).getFunction();
        Project project = getEventProject(anActionEvent);

        if (function.getLocalPath().isEmpty()) {
            telemetry
                    .result(anonymizeResource(name, namespace, "Function " + name + "is not opened locally"))
                    .send();
            return;
        }

        InvokeModel model = new InvokeModel();
        model.setNamespace(namespace);
        model.setPath(function.getLocalPath());

        InvokeDialog dialog = new InvokeDialog("Invoke", project, function, model);
        dialog.show();

        if (dialog.isOK()) {
            ExecHelper.submit(() -> {
                try {
                    doInvokeWithRetry(project, knCli, model, function);
                } catch (IOException e) {
                    doInvokeExceptionHandler(e, name, namespace);
                }
            });
        } else {
            telemetry
                    .result(anonymizeResource(name, namespace, "Invoked function " + name + " operation has been cancelled"))
                    .send();
        }
    }

    private void doInvokeWithRetry(Project project, Kn knCli, InvokeModel model, Function function) throws IOException {
        String name = function.getName();
        try {
            doInvoke(knCli, model, name);
        } catch (IOException e) {
            if (e.getLocalizedMessage().contains("function not running")) {
                int response = UIHelper.executeInUI(() -> Messages.showYesNoDialog(
                        project,
                        "Cannot invoke a function if it is not running.\nRun function " + name + "?",
                        "Unable to invoke func " + name,
                        "Run",
                        "Cancel",
                        AllIcons.General.QuestionDialog
                ));
                if (response == Messages.YES) {
                    RunFuncActionPipeline pipeline = UIHelper.executeInUI(() -> RunAction.Run(project, function, knCli, name));
                    ((RunFuncActionTask)pipeline.getRunningStep()).setCallbackWhenListeningReady(() -> {
                        try {
                            doInvoke(knCli, model, name);
                        } catch (IOException ex) {
                            doInvokeExceptionHandler(ex, name, knCli.getNamespace());
                        }
                    });
                    return;
                }
            }
            throw e;
        }
    }

    private void doInvoke(Kn knCli, InvokeModel model, String name) throws IOException{
        String id = knCli.invokeFunc(model);
        Notification notification = new Notification(NOTIFICATION_ID,
                "Invoked successfully",
                "Function " + name + " has been successfully invoked with execution id " + id + " !",
                NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
        telemetry
                .result(anonymizeResource(name, model.getNamespace(), "Invoked function " + name))
                .send();
    }

    private void doInvokeExceptionHandler(IOException ex, String name, String namespace) {
        Notification notification = new Notification(NOTIFICATION_ID,
                "Invoking function " + name + " failed",
                ex.getLocalizedMessage(),
                NotificationType.ERROR);
        Notifications.Bus.notify(notification);
        logger.warn(ex.getLocalizedMessage(), ex);
        telemetry
                .error(anonymizeResource(name, namespace, ex.getLocalizedMessage()))
                .send();
    }

    protected TelemetryMessageBuilder.ActionMessage createTelemetry() {
        return TelemetryService.instance().action(NAME_PREFIX_MISC + "invoke func");
    }

    @Override
    public boolean isVisible(Object selected) {
        if (selected instanceof KnFunctionNode) {
            return !((KnFunctionNode) selected).getFunction().getLocalPath().isEmpty();
        }
        return false;
    }
}
