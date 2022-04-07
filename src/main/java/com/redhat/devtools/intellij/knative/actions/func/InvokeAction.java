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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
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

        if (function.getLocalPath().isEmpty()) {
            telemetry
                    .result(anonymizeResource(name, namespace, "Function " + name + "is not opened locally"))
                    .send();
            return;
        }

        InvokeModel model = new InvokeModel();
        model.setNamespace(namespace);
        model.setPath(function.getLocalPath());

        InvokeDialog dialog = new InvokeDialog("Invoke", getEventProject(anActionEvent), function, model);
        dialog.show();

        if (dialog.isOK()) {
            ExecHelper.submit(() -> {
                try {
                    knCli.invokeFunc(model);
                    Notification notification = new Notification(NOTIFICATION_ID,
                            "Invoked successfully",
                            "Function " + name + " has been successfully invoked!",
                            NotificationType.INFORMATION);
                    Notifications.Bus.notify(notification);
                    telemetry
                            .result(anonymizeResource(name, namespace, "Invoked function " + name))
                            .send();
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
            });
        } else {
            telemetry
                    .result(anonymizeResource(name, namespace, "Invoked function " + name + " operation has been cancelled"))
                    .send();
        }
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
