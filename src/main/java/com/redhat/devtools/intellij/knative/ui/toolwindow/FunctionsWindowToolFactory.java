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
package com.redhat.devtools.intellij.knative.ui.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.knative.listener.KnTreeDoubleClickListener;
import com.redhat.devtools.intellij.knative.tree.KnFunctionsTreeStructure;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import org.jetbrains.annotations.NotNull;

import static com.redhat.devtools.intellij.knative.Constants.FUNCTIONS_ACTION_GROUP_ID;
import static com.redhat.devtools.intellij.knative.Constants.FUNCTIONS_TOOLBAR_ACTION_GROUP_ID;

public class FunctionsWindowToolFactory extends KnBaseWindowTool<KnTreeStructure> implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        setTitleAndIcon(toolWindow, "Functions");
        KnFunctionsTreeStructure structure = new KnFunctionsTreeStructure(project);
        createToolWindowContent(toolWindow, structure, FUNCTIONS_ACTION_GROUP_ID, FUNCTIONS_TOOLBAR_ACTION_GROUP_ID);
    }
}
