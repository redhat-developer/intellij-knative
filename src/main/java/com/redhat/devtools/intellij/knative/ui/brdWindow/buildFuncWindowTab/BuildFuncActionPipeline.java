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
package com.redhat.devtools.intellij.knative.ui.brdWindow.buildFuncWindowTab;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.ui.brdWindow.FuncActionPipeline;

import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_CONTENT_NAME;
import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_TOOLWINDOW_ID;

public class BuildFuncActionPipeline extends FuncActionPipeline {
    public BuildFuncActionPipeline(Project project, Function function) {
        super("Build", project, function);
    }

    @Override
    public void start() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(BUILDFUNC_TOOLWINDOW_ID);
        BuildFuncPanel buildOutputPanel = (BuildFuncPanel) toolWindow.getContentManager().findContent(BUILDFUNC_CONTENT_NAME);
        buildOutputPanel.drawFuncAction(this);
        super.start();
    }
}
