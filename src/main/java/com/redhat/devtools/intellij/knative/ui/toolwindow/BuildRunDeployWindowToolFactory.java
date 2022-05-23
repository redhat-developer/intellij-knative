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
package com.redhat.devtools.intellij.knative.ui.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.buildFuncWindowTab.BuildFuncPanel;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.runFuncWindowTab.RunFuncPanel;
import org.jetbrains.annotations.NotNull;

public class BuildRunDeployWindowToolFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setIcon(AllIcons.Toolwindows.ToolWindowBuild);
        toolWindow.setStripeTitle("Function Build");
    }

    @Override
    public void init(ToolWindow window) {
        BuildFuncPanel buildFuncPanel = new BuildFuncPanel(window);
        window.getContentManager().addContent(buildFuncPanel);
        RunFuncPanel runFuncPanel = new RunFuncPanel(window);
        window.getContentManager().addContent(runFuncPanel);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return true;
    }
}
