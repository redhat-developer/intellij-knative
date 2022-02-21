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
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.tree.TreePath;

import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.intellij.openapi.ui.Messages.CANCEL_BUTTON;
import static com.intellij.openapi.ui.Messages.OK_BUTTON;
import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_BUILD_DEPLOY;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_CRUD;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class BuildAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(BuildAction.class);

    protected TelemetryMessageBuilder.ActionMessage telemetry;

    public BuildAction() {
        super(KnFunctionNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        telemetry = createTelemetry();
        ParentableNode node = getElement(selected);
        String name = node.getName();
        String namespace = knCli.getNamespace();
        Function function = ((KnFunctionNode) node).getFunction();
        String localPathFunc = function.getLocalPath();
        if (localPathFunc.isEmpty()) {
            telemetry
                    .result(anonymizeResource(name, namespace, "Function " + name + "is not opened locally"))
                    .send();
            return;
        }
        // get image or registry in func.yaml
        Pair<String, String> dataToDeploy = getDataToDeploy(Paths.get(localPathFunc), knCli);
        String registry = dataToDeploy.getFirst();
        String image = dataToDeploy.getSecond();
        if (Strings.isNullOrEmpty(image) && Strings.isNullOrEmpty(registry)) {
            // ask input to user
            image = getImageFromUser(node.getName());
            if (image.isEmpty()) {
                telemetry
                        .result(anonymizeResource(name, namespace, "No image name or registry has been added."))
                        .send();
                return;
            }
        }

        if (!isActionConfirmed(node.getName(), function.getNamespace(), namespace)) {
            telemetry
                    .result(anonymizeResource(name, namespace, "Action execution has been stopped by user."))
                    .send();
            return;
        }

        String finalImage = image;
        ExecHelper.submit(() -> {
            try {
                doExecute(knCli, namespace, localPathFunc, registry, finalImage);
                telemetry
                        .result(anonymizeResource(name, namespace, getSuccessMessage(namespace, name)))
                        .send();
                TreeHelper.refreshFuncTree(getEventProject(anActionEvent));
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
    }

    protected void doExecute(Kn knCli, String namespace, String localPathFunc, String registry, String image) throws IOException {
        knCli.buildFunc(localPathFunc, registry, image);
    }

    protected boolean isActionConfirmed(String name, String funcNamespace, String activeNamespace) {
        return true;
    }

    protected String getImageFromUser(String name) {
        String defaultUsername = System.getProperty("user.name");
        String defaultImage = "quay.io/" + defaultUsername + "/" + name + ":latest";
        Messages.InputDialog dialog = new Messages.InputDialog(null, "Provide full image name in the form [registry]/[namespace]/[name]:[tag] (e.g quay.io/boson/image:latest)",
                "Build Function " + name, null, defaultImage,
                new InputValidator() {
                    @Override
                    public boolean checkInput(String inputString) {
                        return !inputString.isEmpty();
                    }

                    @Override
                    public boolean canClose(String inputString) {
                        return true;
                    }
                },
                new String[]{OK_BUTTON, CANCEL_BUTTON},
                0, null);
        dialog.show();
        if (!dialog.isOK()) {
            return "";
        }
        return dialog.getInputString();
    }

    protected Pair<String, String> getDataToDeploy(Path root, Kn kncli) {
        try {
            URL funcFileURL = kncli.getFuncFileURL(root);
            String content = YAMLHelper.JSONToYAML(YAMLHelper.URLToJSON(funcFileURL));
            String registry = YAMLHelper.getStringValueFromYAML(content, new String[]{"registry"});
            String image = YAMLHelper.getStringValueFromYAML(content, new String[]{"image"});
            return Pair.create(registry, image);
        } catch(IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return Pair.empty();
    }

    protected TelemetryMessageBuilder.ActionMessage createTelemetry() {
        return TelemetryService.instance().action(NAME_PREFIX_BUILD_DEPLOY + "build func");
    }

    protected String getSuccessMessage(String namespace, String name) {
        return "Function " + name + " has been successfully built";
    }

    @Override
    public boolean isVisible(Object selected) {
        if (selected instanceof KnFunctionNode) {
            return !((KnFunctionNode) selected).getFunction().getLocalPath().isEmpty();
        }
        return false;
    }
}
