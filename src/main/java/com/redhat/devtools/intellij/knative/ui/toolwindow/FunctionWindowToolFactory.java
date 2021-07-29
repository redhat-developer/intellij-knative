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
import com.redhat.devtools.intellij.knative.tree.KnFunctionsTreeStructure;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public class FunctionWindowToolFactory extends KnBaseWindowTool<KnFunctionsTreeStructure> implements ToolWindowFactory {

    private static final String ACTION_GROUP_ID = "com.redhat.devtools.intellij.knative.tree.functions";
    private static final String TOOLBAR_ACTION_GROUP_ID = "com.redhat.devtools.intellij.knative.view.actionsFunctionToolbar";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Icon icon = IconLoader.findIcon("/images/knative-logo.svg", FunctionWindowToolFactory.class);
        if (icon != null) {
            toolWindow.setIcon(icon);
        }
        toolWindow.setStripeTitle("Knative Functions");

        KnTreeStructure knTreeStructure = TreeHelper.getKnTreeStructure(project);
        if (knTreeStructure != null) {
            KnRootNode root = (KnRootNode) knTreeStructure.getRootElement();
            Kn kn = root.getKn();
            KnFunctionsTreeStructure knFunctionsTreeStructure = new KnFunctionsTreeStructure(project);
            if (kn != null) {
                Tree tree = createTree(project, knFunctionsTreeStructure, false);
                createContent(toolWindow, tree, ACTION_GROUP_ID, TOOLBAR_ACTION_GROUP_ID);
            } else {
                root.load().whenComplete((kncli, err) -> {
                    Tree tree = createTree(project, knFunctionsTreeStructure, false);
                    createContent(toolWindow, tree, ACTION_GROUP_ID, TOOLBAR_ACTION_GROUP_ID);
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
