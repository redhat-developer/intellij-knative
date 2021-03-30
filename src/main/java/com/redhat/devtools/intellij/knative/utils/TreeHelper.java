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

public class TreeHelper {

    private static final int MESSAGE_MAX_LENGTH = 130;

    public static String trimErrorMessage(String errorMessage) {
        return errorMessage.length() >= MESSAGE_MAX_LENGTH ? errorMessage.substring(0, MESSAGE_MAX_LENGTH) + "..." : errorMessage;
    }

    public static Tree getTree(Project project) {
        if (project == null) {
            return null;
        }
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Knative");
        if (window == null) {
            return null;
        }
        Content content = window.getContentManager().getContent(0);
        if (content == null) {
            return null;
        }
        SimpleToolWindowPanel simpleToolWindowPanel = (SimpleToolWindowPanel) content.getComponent();
        if (simpleToolWindowPanel == null) {
            return null;
        }
        JBScrollPane pane = (JBScrollPane) simpleToolWindowPanel.getContent();
        if (pane == null) {
            return null;
        }
        Component view = pane.getViewport().getView();
        if (view == null) {
            return null;
        }
        return (Tree) view;
    }

    public static KnTreeStructure getKnTreeStructure(Project project) {
        if (project == null) {
            return null;
        }
        Tree tree = getTree(project);
        if (tree == null) {
            return null;
        }
        Object property = tree.getClientProperty(Constants.STRUCTURE_PROPERTY);
        if (property == null) {
            return null;
        }
        return (KnTreeStructure) property;
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
        if (project != null && node != null) {
            KnTreeStructure structure = getKnTreeStructure(project);
            if (structure != null) {
                structure.fireModified(node);
            }
        }
    }
}
