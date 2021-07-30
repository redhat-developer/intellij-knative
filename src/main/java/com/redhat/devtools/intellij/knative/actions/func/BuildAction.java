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
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnFunctionLocalNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.intellij.openapi.ui.Messages.CANCEL_BUTTON;
import static com.intellij.openapi.ui.Messages.OK_BUTTON;
import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;

public class BuildAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(BuildAction.class);

    public BuildAction() {
        super(KnFunctionLocalNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        ParentableNode node = getElement(selected);
        Function function = ((KnFunctionLocalNode) node).getFunction();
        String localPathFunc = function.getLocalPath();
        if (localPathFunc.isEmpty()) {
            return;
        }
        // get image or registry in func.yaml
        Pair<String, String> dataToDeploy = getDataToDeploy(localPathFunc);
        String registry = dataToDeploy.getFirst();
        String image = dataToDeploy.getSecond();
        if (Strings.isNullOrEmpty(image) && Strings.isNullOrEmpty(registry)) {
            // ask input to user
            image = getImageFromUser(node.getName());
            if (image.isEmpty()) {
                return;
            }
        } else {
            if (!isActionConfirmed(node.getName())) {
                return;
            }
        }

        String namespace = node.getRootNode().getKn().getNamespace();
        try {
            doExecute(knCli, namespace, localPathFunc, registry, image);
            TreeHelper.refreshFunc(getEventProject(anActionEvent));
        } catch (IOException e) {
            Notification notification = new Notification(NOTIFICATION_ID,
                    "Error",
                    e.getLocalizedMessage(),
                    NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    protected void doExecute(Kn knCli, String namespace, String localPathFunc, String registry, String image) throws IOException {
        knCli.buildFunc(localPathFunc, registry, image);
    }

    protected boolean isActionConfirmed(String name) {
        return true;
    }

    protected String getImageFromUser(String name) {
        Messages.InputDialog dialog = new Messages.InputDialog(null, "Provide full image name in the form [registry]/[namespace]/[name]:[tag] (e.g quay.io/boson/image:latest)",
                "Build Function " + name, null, null,
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

    protected Pair<String, String> getDataToDeploy(String path) {
        try {
            File funcSettings = Paths.get(path, "func.yaml").toFile();
            if (funcSettings.exists()) {
                String content = YAMLHelper.JSONToYAML(YAMLHelper.URLToJSON(funcSettings.toURI().toURL()));
                String registry = YAMLHelper.getStringValueFromYAML(content, new String[]{"registry"});
                String image = YAMLHelper.getStringValueFromYAML(content, new String[]{"image"});
                return Pair.create(registry, image);
            }
        } catch(IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return Pair.empty();
    }

    @Override
    public boolean isVisible(Object selected) {
        if (selected instanceof KnFunctionLocalNode) {
            return !((KnFunctionLocalNode) selected).getFunction().getLocalPath().isEmpty();
        }
        return false;
    }
}
