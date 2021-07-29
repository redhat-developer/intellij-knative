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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.knative.listener.KnTreeDoubleClickListener;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import org.jetbrains.annotations.NotNull;

public class WindowToolFactory extends KnBaseWindowTool<KnTreeStructure> implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        KnTreeStructure structure = new KnTreeStructure(project);
        Tree tree = createTree(project, structure, true);
        new KnTreeDoubleClickListener(tree);
        createContent(toolWindow, tree, "com.redhat.devtools.intellij.knative.tree", "com.redhat.devtools.intellij.knative.view.actionsToolbar");
    }
}
