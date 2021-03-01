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
package com.redhat.devtools.intellij.knative;

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.tree.MutableModelSynchronizer;
import com.redhat.devtools.intellij.knative.listener.TreePopupMenuListener;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class WindowToolFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        try {
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

            KnTreeStructure structure = new KnTreeStructure(project);
            StructureTreeModel<KnTreeStructure> model = buildModel(structure, project);
            new MutableModelSynchronizer<>(model, structure, structure);
            Tree tree = new Tree(new AsyncTreeModel(model, project));
            tree.putClientProperty(Constants.STRUCTURE_PROPERTY, structure);
            tree.setCellRenderer(new NodeRenderer());
            ActionManager actionManager = ActionManager.getInstance();
            ActionGroup group = (ActionGroup)actionManager.getAction("com.redhat.devtools.intellij.knative.tree");
            PopupHandler.installPopupHandler(tree, group, ActionPlaces.UNKNOWN, actionManager, new TreePopupMenuListener());
            toolWindow.getContentManager().addContent(contentFactory.createContent(new JBScrollPane(tree), "", false));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException((e));
        }
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
    private StructureTreeModel buildModel(KnTreeStructure structure, Project project) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        try {
            Constructor<StructureTreeModel> constructor = StructureTreeModel.class.getConstructor(new Class[]{AbstractTreeStructure.class});
            return constructor.newInstance(structure);
        } catch (NoSuchMethodException e) {
            Constructor<StructureTreeModel> constructor = StructureTreeModel.class.getConstructor(new Class[]{AbstractTreeStructure.class, Disposable.class});
            return constructor.newInstance(structure, project);
        }
    }
}
