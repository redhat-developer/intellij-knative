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
package com.redhat.devtools.intellij.knative.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;

import javax.swing.tree.TreePath;

public class OpenGettingStartedAction extends KnAction {
    private static final String GETTING_STARTED_WINDOW_ID = "KnativeGettingStarted";
    public OpenGettingStartedAction() {
        super(KnRootNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected, Kn kn) {
        Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GETTING_STARTED_WINDOW_ID);
        if (toolWindow == null) {
            return;
        }
        toolWindow.setAvailable(true, null);
        toolWindow.activate(null);
        toolWindow.show(null);
    }
}
