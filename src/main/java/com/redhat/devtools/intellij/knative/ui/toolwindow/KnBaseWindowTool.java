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

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.listener.TreePopupMenuListener;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSynchronizer;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.tree.AbstractKnTreeStructure;
import com.redhat.devtools.intellij.knative.tree.KnNodeComparator;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Icon;

public abstract class KnBaseWindowTool<T extends AbstractKnTreeStructure> {

    protected void setTitleAndIcon(ToolWindow toolWindow, String title) {
        Icon icon = IconLoader.findIcon("/images/knative-logo.svg", KnBaseWindowTool.class);
        if (icon != null) {
            toolWindow.setIcon(icon);
        }
        toolWindow.setStripeTitle(title);
        toolWindow.setTitle(title);
    }

    protected Tree createTree(Project project, T structure, boolean isRootVisible) {
        try {
            StructureTreeModel<T> model = buildModel(structure, project);
            model.setComparator(new KnNodeComparator<>());
            new MutableModelSynchronizer<>(model, structure, (MutableModel) structure);
            Tree tree = new Tree(new AsyncTreeModel(model, project));
            tree.putClientProperty(Constants.STRUCTURE_PROPERTY, structure);
            tree.setRootVisible(isRootVisible);
            tree.setCellRenderer(new NodeRenderer());
            return tree;
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException((e));
        }
    }

    protected void addMenuToTree(Tree tree, String actionGroup) {
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup group = (ActionGroup) actionManager.getAction(actionGroup);
        PopupHandler.installPopupHandler(tree, group, ActionPlaces.UNKNOWN, actionManager, new TreePopupMenuListener());
    }

    protected SimpleToolWindowPanel createPanelWithTree(Tree tree) {
        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true, true);
        panel.setContent(new JBScrollPane(tree));
        return panel;
    }

    protected void addToolbarMenuToPanel(SimpleToolWindowPanel panel, String toolbarActionGroupId) {
        ActionManager actionManager = ActionManager.getInstance();
        if (actionManager.isGroup(toolbarActionGroupId)) {
            ActionToolbar actionToolbar = actionManager.createActionToolbar(Constants.TOOLBAR_PLACE, (ActionGroup) actionManager.getAction(toolbarActionGroupId), true);
            panel.setToolbar(actionToolbar.getComponent());
        }
    }

    protected void createContent(ToolWindow toolWindow, Tree tree, String actionGroup, String toolbarActionGroup) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        addMenuToTree(tree, actionGroup);
        SimpleToolWindowPanel panel = createPanelWithTree(tree);
        addToolbarMenuToPanel(panel, toolbarActionGroup);

        toolWindow.getContentManager().addContent(contentFactory.createContent(panel, "", false));

    }

    /**
     * Build the model through reflection as StructureTreeModel does not have a stable API.
     *
     * @param structure the structure to associate
     * @param project the IJ project
     * @return the build model
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    protected StructureTreeModel buildModel(AbstractTreeStructure structure, Project project) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        try {
            Constructor<StructureTreeModel> constructor = StructureTreeModel.class.getConstructor(new Class[]{AbstractTreeStructure.class});
            return constructor.newInstance(structure);
        } catch (NoSuchMethodException e) {
            Constructor<StructureTreeModel> constructor = StructureTreeModel.class.getConstructor(new Class[]{AbstractTreeStructure.class, Disposable.class});
            return constructor.newInstance(structure, project);
        }
    }
}
