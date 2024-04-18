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
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.BuildRunDeployFuncPanel;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.buildFuncWindowTab.BuildFuncPanel;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.deployFuncWindowTab.DeployFuncPanel;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.runFuncWindowTab.RunFuncPanel;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import org.jetbrains.annotations.NotNull;

public class BuildRunDeployWindowToolFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setIcon(AllIcons.Toolwindows.ToolWindowBuild);
        toolWindow.setStripeTitle("Functions");
        executeOnProjectClosing(project, () -> {
            Kn kn = TreeHelper.getKn(project);
            if (kn != null) {
                kn.dispose();
            }
        });
    }

    @Override
    public void init(ToolWindow window) {
        BuildFuncPanel buildFuncPanel = new BuildFuncPanel(window);
        window.getContentManager().addContent(buildFuncPanel);

        RunFuncPanel runFuncPanel = new RunFuncPanel(window);
        window.getContentManager().addContent(runFuncPanel);

        DeployFuncPanel deployFuncPanel = new DeployFuncPanel(window);
        window.getContentManager().addContent(deployFuncPanel);

        window.getContentManager().addContentManagerListener(new ContentChangeManagerListener());


    }

    private void executeOnProjectClosing(Project project, Runnable runnable) {
        ProjectManager.getInstance().addProjectManagerListener(project, new ProjectManagerListener() {
            @Override
            public void projectClosing(@NotNull Project project) {
                runnable.run();
            }
        });
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }
}

class ContentChangeManagerListener implements ContentManagerListener {

    @Override
    public void contentAdded(@NotNull ContentManagerEvent event) {

    }

    @Override
    public void contentRemoved(@NotNull ContentManagerEvent event) {

    }

    @Override
    public void contentRemoveQuery(@NotNull ContentManagerEvent event) {

    }

    @Override
    public void selectionChanged(@NotNull ContentManagerEvent event) {
        if (event.getOperation().equals(ContentManagerEvent.ContentOperation.add)) {
            ((BuildRunDeployFuncPanel) event.getContent()).setSelectionDefault();
        }
    }
}
