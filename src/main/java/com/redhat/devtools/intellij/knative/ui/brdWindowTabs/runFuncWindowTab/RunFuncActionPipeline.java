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
package com.redhat.devtools.intellij.knative.ui.brdWindowTabs.runFuncWindowTab;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.FuncActionPipeline;

import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_TOOLWINDOW_ID;
import static com.redhat.devtools.intellij.knative.Constants.RUNFUNC_CONTENT_NAME;

public class RunFuncActionPipeline extends FuncActionPipeline {
    public RunFuncActionPipeline(Project project, Function function) {
        super("Run", project, function);
    }

    @Override
    public void start() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(BUILDFUNC_TOOLWINDOW_ID);
        RunFuncPanel runOutputPanel = (RunFuncPanel) toolWindow.getContentManager().findContent(RUNFUNC_CONTENT_NAME);
        runOutputPanel.drawFuncAction(this);
        super.start();
    }
}
