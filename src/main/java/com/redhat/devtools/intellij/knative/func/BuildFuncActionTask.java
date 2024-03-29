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
package com.redhat.devtools.intellij.knative.func;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.buildFuncWindowTab.BuildFuncPanel;

import java.util.function.Consumer;

import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_CONTENT_NAME;
import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_TOOLWINDOW_ID;

public class BuildFuncActionTask extends FuncActionTask {
    public BuildFuncActionTask(Consumer<FuncActionTask> doExecute) {
        super("buildImage", doExecute);
    }

    @Override
    public void doExecute() {
        if (!(pipeline instanceof BuildFuncActionPipeline)) {
            ToolWindow toolWindow = ToolWindowManager.getInstance(pipeline.getProject()).getToolWindow(BUILDFUNC_TOOLWINDOW_ID);
            BuildFuncPanel buildOutputPanel = (BuildFuncPanel) toolWindow.getContentManager().findContent(BUILDFUNC_CONTENT_NAME);
            buildOutputPanel.drawFuncAction(this);
        }
        super.doExecute();
    }
}
