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
package com.redhat.devtools.intellij.knative.actions;

import com.google.common.base.Strings;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.ServiceStatus;
import com.redhat.devtools.intellij.knative.kn.ServiceTraffic;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;

import java.io.IOException;
import java.util.Optional;
import javax.swing.tree.TreePath;


import static com.intellij.openapi.ui.Messages.CANCEL_BUTTON;
import static com.intellij.openapi.ui.Messages.OK_BUTTON;
import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_MISC;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class OpenInBrowserAction extends KnAction {
    public OpenInBrowserAction() {
        super(KnServiceNode.class, KnRevisionNode.class, KnFunctionNode.class);
    }

    @Override
    public boolean isVisible(Object selected) {
        if (selected instanceof KnRevisionNode) {
            selected = ((KnRevisionNode) selected).getParent();
        }
        return !Strings.isNullOrEmpty(getURL(getElement(selected), null));
    }

    public boolean isEnabled(Object selected) {
        if (selected instanceof KnRevisionNode) {
            Optional<ServiceTraffic> traffic = getServiceTraffic((KnRevisionNode) selected, false);
            if (!traffic.isPresent()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn kncli) {
        TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_MISC + "open in browser");
        Object node = getElement(selected);

        ExecHelper.submit(() -> {
            String url = getURL(node, kncli);
            String name = "", namespace = "";
            if (node instanceof ParentableNode) {
                name = ((ParentableNode<?>) node).getName();
                namespace = kncli.getNamespace();
            }
            if (!Strings.isNullOrEmpty(url)) {
                BrowserUtil.browse(url);
                telemetry
                    .result(anonymizeResource(name, namespace, "Function " + name + " from namespace " + namespace + " opened on browser"))
                    .send();
            } else {
                notifyFailingAction(selected, NotificationType.WARNING, "");
                telemetry
                        .error(anonymizeResource(name, namespace, "Unable to open function " + name + " from namespace " + namespace + " on browser"))
                        .send();
            }
        });
    }

    private String getURL(Object selected, Kn kncli) {
        String url = "";
        if (selected instanceof KnServiceNode) {
            ServiceStatus status = ((KnServiceNode) selected).getService(false).getStatus();
            if (status != null) {
                url = status.getUrl();
            }
        } else if (selected instanceof KnRevisionNode) {
            url = getRevisionURL((KnRevisionNode) selected, kncli);
        } else if (selected instanceof KnFunctionNode) {
            url = ((KnFunctionNode) selected).getFunction().getUrl();
        }
        return url;
    }

    private String getRevisionURL(KnRevisionNode revisionNode, Kn kncli) {
        String url = getRevisionURLFromService(revisionNode, false);
        if (url.isEmpty() && tagRevision(revisionNode, kncli)) {
            url = getRevisionURLFromService(revisionNode, true);
            if (url.isEmpty()) {
                notifyFailingAction(revisionNode, NotificationType.WARNING, "");
            }
        }
        return url;
    }

    private String getRevisionURLFromService(KnRevisionNode revisionNode, boolean fetchUpdatedService) {
        Optional<ServiceTraffic> traffic = getServiceTraffic(revisionNode, fetchUpdatedService);
        if (traffic.isPresent()) {
            return traffic.get().getUrl();
        }
        return "";
    }

    private Optional<ServiceTraffic> getServiceTraffic(KnRevisionNode revisionNode, boolean fetchUpdatedService) {
        ServiceStatus status = revisionNode.getParent().getService(fetchUpdatedService).getStatus();
        if (status != null) {
            return status.getTraffic().stream().filter(svcTraffic -> svcTraffic.getRevisionName().equals(revisionNode.getName())).findFirst();
        }
        return Optional.empty();
    }

    private boolean tagRevision(KnRevisionNode revisionNode, Kn kncli) {
        Messages.InputDialog dialog = UIHelper.executeInUI(() -> {
            Messages.InputDialog id = new Messages.InputDialog(null, "Tag the revision to create a custom URL",
                    "Tag revision", null, null,
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
            id.show();
            return id;
        });

        if (dialog != null && !dialog.isOK()) {
            return false;
        }
        String tag = dialog.getInputString();
        if (Strings.isNullOrEmpty(tag)) {
            notifyFailingAction(revisionNode, NotificationType.WARNING, "");
            return false;
        }
        Optional<ServiceTraffic> traffic = getServiceTraffic(revisionNode, false);
        if (!traffic.isPresent()) {
            notifyFailingAction(revisionNode, NotificationType.WARNING, "");
            return false;
        }
        String revision = traffic.get().getLatestRevision() ? "@latest" : traffic.get().getRevisionName();
        try {
            kncli.tagRevision(revisionNode.getParent().getName(), revision, tag);
        } catch (IOException e) {
            notifyFailingAction(revisionNode, NotificationType.ERROR, e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    private void notifyFailingAction(Object selected, NotificationType type, String additionalText) {
        String kind = getKind(selected);
        Notification notification = new Notification(NOTIFICATION_ID,
                "Open in Browser",
                "Unable to open " + kind + " " + getName(selected) + " in browser. The " + kind + " has no URL available. " + additionalText,
                type);
        Notifications.Bus.notify(notification);
    }

    private String getName(Object selected) {
        if (selected instanceof ParentableNode) {
            return ((ParentableNode) selected).getName();
        }
        return "";
    }

    private String getKind(Object selected) {
        if (selected instanceof KnServiceNode) {
            return "Service";
        } else if (selected instanceof KnRevisionNode) {
            return "Revision";
        }
        return "";
    }
}
