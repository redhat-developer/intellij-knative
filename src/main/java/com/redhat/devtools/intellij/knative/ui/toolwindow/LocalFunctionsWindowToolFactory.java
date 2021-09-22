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
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnLocalFunctionsTreeStructure;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;


import static com.redhat.devtools.intellij.knative.Constants.LOCAL_FUNCTIONS_ACTION_GROUP_ID;
import static com.redhat.devtools.intellij.knative.Constants.LOCAL_FUNCTIONS_TOOLBAR_ACTION_GROUP_ID;

public class LocalFunctionsWindowToolFactory extends KnBaseWindowTool<KnLocalFunctionsTreeStructure> implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        setTitleAndIcon(toolWindow, "Local Functions");

        KnTreeStructure knTreeStructure = TreeHelper.getKnTreeStructure(project);
        if (knTreeStructure != null) {
            KnRootNode root = (KnRootNode) knTreeStructure.getRootElement();
            Kn kn = root.getKn();
            KnLocalFunctionsTreeStructure knFunctionsTreeStructure = new KnLocalFunctionsTreeStructure(project);
            if (kn != null) {
                Tree tree = createTree(project, knFunctionsTreeStructure, false);
                createContent(toolWindow, tree, LOCAL_FUNCTIONS_ACTION_GROUP_ID, LOCAL_FUNCTIONS_TOOLBAR_ACTION_GROUP_ID);
            } else {
                root.load().whenComplete((kncli, err) -> {
                    Tree tree = createTree(project, knFunctionsTreeStructure, false);
                    createContent(toolWindow, tree, LOCAL_FUNCTIONS_ACTION_GROUP_ID, LOCAL_FUNCTIONS_TOOLBAR_ACTION_GROUP_ID);
                });
            }
        }

    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return false;
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

}
