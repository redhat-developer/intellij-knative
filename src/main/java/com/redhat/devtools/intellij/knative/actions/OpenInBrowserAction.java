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
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.ServiceStatus;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import javax.swing.tree.TreePath;

public class OpenInBrowserAction extends KnAction {
    public OpenInBrowserAction() {
        super(KnServiceNode.class);
    }

    @Override
    public boolean isVisible(Object selected) {
        return !Strings.isNullOrEmpty(getURL(getElement(selected)));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn tkncli) {
        String url = getURL(getElement(selected));
        if (!Strings.isNullOrEmpty(url)) {
            BrowserUtil.browse(url);
        }
    }

    private String getURL(Object selected) {
        String url = "";
        if (selected instanceof KnServiceNode) {
            ServiceStatus status = ((KnServiceNode) selected).getService(false).getStatus();
            if (status != null) {
                url = status.getUrl();
            }
        }
        return url;
    }
}
