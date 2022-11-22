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

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.listener.TreePopupMenuListener;
import com.redhat.devtools.intellij.common.tree.MutableModelSynchronizer;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.listener.KnTreeDoubleClickListener;
import com.redhat.devtools.intellij.knative.tree.AbstractKnTreeStructure;
import com.redhat.devtools.intellij.knative.tree.KnNodeComparator;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;

import javax.swing.Icon;

import static com.redhat.devtools.intellij.knative.Constants.FUNCTIONS_ACTION_GROUP_ID;
import static com.redhat.devtools.intellij.knative.Constants.KNATIVE_TOOLBAR_ACTION_GROUP_ID;

public abstract class KnBaseWindowTool<T extends AbstractKnTreeStructure> {

    protected void setTitleAndIcon(ToolWindow toolWindow, String title) {
        Icon icon = IconLoader.findIcon("/images/knative-logo.svg", KnBaseWindowTool.class);
        if (icon != null) {
            toolWindow.setIcon(icon);
        }
        toolWindow.setStripeTitle(title);
        toolWindow.setTitle(title);
    }

    protected Tree createTree(Disposable disposable, T structure, boolean isRootVisible) {
        StructureTreeModel<T> model = new StructureTreeModel<T>(structure, disposable);
        model.setComparator(new KnNodeComparator<>());
        new MutableModelSynchronizer<>(model, structure, structure);
        Tree tree = new Tree(new AsyncTreeModel(model, disposable));
        tree.putClientProperty(Constants.STRUCTURE_PROPERTY, structure);
        tree.setRootVisible(isRootVisible);
        tree.setCellRenderer(new NodeRenderer());
        return tree;
    }

    private void addMenuToTree(Tree tree, String actionGroup) {
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup group = (ActionGroup) actionManager.getAction(actionGroup);
        PopupHandler.installPopupHandler(tree, group, ActionPlaces.UNKNOWN, actionManager, new TreePopupMenuListener());
    }

    protected void addToolbarMenuToPanel(SimpleToolWindowPanel panel, String toolbarActionGroupId) {
        ActionManager actionManager = ActionManager.getInstance();
        if (actionManager.isGroup(toolbarActionGroupId)) {
            ActionToolbar actionToolbar = actionManager.createActionToolbar(Constants.TOOLBAR_PLACE, (ActionGroup) actionManager.getAction(toolbarActionGroupId), true);
            panel.setToolbar(actionToolbar.getComponent());
        }
    }

    protected void createToolWindowContent(ToolWindow toolWindow, T structure, String actionGroup, String toolbarActionGroup) {
        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true, true);
        Content content = createContent(toolWindow, panel, toolbarActionGroup);

        Tree tree = createTree(content, structure, true);
        addMenuToTree(tree, actionGroup);
        panel.setContent(new JBScrollPane(tree));
        new KnTreeDoubleClickListener(tree);
    }

    protected Content createContent(ToolWindow toolWindow, SimpleToolWindowPanel panel, String toolbarActionGroup) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        addToolbarMenuToPanel(panel, toolbarActionGroup);
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
        return content;
    }
}
