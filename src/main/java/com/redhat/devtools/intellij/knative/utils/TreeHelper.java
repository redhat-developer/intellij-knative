/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.utils;

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JViewport;


import static com.redhat.devtools.intellij.knative.Constants.KIND_FUNCTIONS;
import static com.redhat.devtools.intellij.knative.Constants.KNATIVE_FUNC_TOOL_WINDOW_ID;
import static com.redhat.devtools.intellij.knative.Constants.KNATIVE_TOOL_WINDOW_ID;

public class TreeHelper {

    private static final int MESSAGE_MAX_LENGTH = 130;

    public static String trimErrorMessage(String errorMessage) {
        return errorMessage.length() >= MESSAGE_MAX_LENGTH ? errorMessage.substring(0, MESSAGE_MAX_LENGTH) + "..." : errorMessage;
    }

    public static Tree getTree(Project project, String toolWindowId) {
        if (project == null) {
            return null;
        }
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(toolWindowId);
        if (window == null) {
            return null;
        }
        Content content = window.getContentManager().getContent(0);
        if (content == null) {
            return null;
        }
        JComponent simpleToolWindowPanel = content.getComponent();
        if (simpleToolWindowPanel == null || !(simpleToolWindowPanel instanceof SimpleToolWindowPanel)) {
            return null;
        }
        JComponent pane = ((SimpleToolWindowPanel) simpleToolWindowPanel).getContent();
        if (pane == null || !(pane instanceof JBScrollPane)) {
            return null;
        }
        JViewport viewPort = ((JBScrollPane) pane).getViewport();
        if (viewPort == null) {
            return null;
        }
        Component view = viewPort.getView();
        if (view == null) {
            return null;
        }
        return (Tree) view;
    }

    public static KnTreeStructure getKnTreeStructure(Project project) {
        return getKnTreeStructure(project, KNATIVE_TOOL_WINDOW_ID);
    }

    public static KnTreeStructure getKnFunctionsTreeStructure(Project project) {
        return getKnTreeStructure(project, KNATIVE_FUNC_TOOL_WINDOW_ID);
    }

    private static KnTreeStructure getKnTreeStructure(Project project, String toolWindowId) {
        return (KnTreeStructure) getTreeStructure(project, toolWindowId);
    }

    private static AbstractTreeStructure getTreeStructure(Project project, String toolWindowId) {
        Tree tree = getTree(project, toolWindowId);
        if (tree == null) {
            return null;
        }
        Object property = tree.getClientProperty(Constants.STRUCTURE_PROPERTY);
        if (property == null) {
            return null;
        }
        return (AbstractTreeStructure) property;
    }

    public static Kn getKn(Project project) {
        try {
            KnTreeStructure treeStructure = getKnTreeStructure(project);
            KnRootNode root = (KnRootNode) treeStructure.getRootElement();
            return root.getKn();
        } catch(Exception ex) {
            return null;
        }
    }

    public static void refresh(Project project, ParentableNode node) {
        if (project != null) {
            refreshTreeStructure(getKnTreeStructure(project), node);
        }
    }

    public static void refreshFuncTree(Project project) {
        if (project != null) {
            KnTreeStructure knFunctionsTreeStructure = getKnFunctionsTreeStructure(project);
            refreshTreeStructure(knFunctionsTreeStructure, knFunctionsTreeStructure.getRootElement());
        }
    }

    private static void refreshTreeStructure(KnTreeStructure structure, Object node) {
        if (structure != null && node != null) {
            structure.fireModified(node);
        }
    }

    public static String getId(KnRootNode node) {
        Kn kn = node.getKn();
        return kn.getNamespace() + "-" + KIND_FUNCTIONS;
    }
}
