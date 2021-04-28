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
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.ServiceStatus;
import com.redhat.devtools.intellij.knative.kn.ServiceTraffic;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import java.io.IOException;
import java.util.Optional;
import javax.swing.tree.TreePath;

public class OpenInBrowserAction extends KnAction {
    public OpenInBrowserAction() {
        super(KnServiceNode.class, KnRevisionNode.class);
    }

    @Override
    public boolean isVisible(Object selected) {
        if (selected instanceof KnRevisionNode) {
            selected = ((KnRevisionNode) selected).getParent();
        }
        return !Strings.isNullOrEmpty(getURL(getElement(selected), null));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn kncli) {
        String url = getURL(getElement(selected), kncli);
        if (!Strings.isNullOrEmpty(url)) {
            BrowserUtil.browse(url);
        }
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
        }
        return url;
    }

    private String getRevisionURL(KnRevisionNode revisionNode, Kn kncli) {
        String url = getRevisionURLFromService(revisionNode, false);
        if (url.isEmpty() && tagRevision(revisionNode, kncli)) {
            url = getRevisionURLFromService(revisionNode, true);
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
        String tag = Messages.showInputDialog("Tag the revision to create a custom URL", "Tag revision", null);
        if (Strings.isNullOrEmpty(tag)) {
            return false;
        }
        Optional<ServiceTraffic> traffic = getServiceTraffic(revisionNode, false);
        if (!traffic.isPresent()) {
            return false;
        }
        String revision = traffic.get().getLatestRevision() ? "@latest" : traffic.get().getRevisionName();
        try {
            kncli.tagRevision(revisionNode.getParent().getName(), revision, tag);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
