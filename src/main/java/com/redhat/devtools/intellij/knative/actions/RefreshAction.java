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
package com.redhat.devtools.intellij.knative.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.tree.KnEventingNode;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.KnServingNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;

import javax.swing.tree.TreePath;

public class RefreshAction extends StructureTreeAction {
    public RefreshAction() {
        this(KnRootNode.class, KnServingNode.class, KnServiceNode.class, KnRevisionNode.class,
                KnEventingNode.class, KnFunctionNode.class);
    }

    public RefreshAction(Class... filters) {
        super(filters);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        Tree tree = getTree(anActionEvent);
        if (tree == null) {
            return;
        }
        KnTreeStructure structure = (KnTreeStructure) tree.getClientProperty(Constants.STRUCTURE_PROPERTY);
        if (structure == null) {
            return;
        }
        if (Constants.TOOLBAR_PLACE.equals(anActionEvent.getPlace())) {
            structure.fireModified(structure.getRootElement());
        } else {
            selected = StructureTreeAction.getElement(selected);
            structure.fireModified(selected);
        }

    }

    @Override
    public void update(AnActionEvent e) {
        if (Constants.TOOLBAR_PLACE.equals(e.getPlace())) {
            updateAction(e, KnTreeStructure.class);
        } else {
            super.update(e);
        }
    }

    protected void updateAction(AnActionEvent e, Class structureClass) {
        e.getPresentation().setVisible(true);
        Tree t = getTree(e);
        if (t != null) {
            Object structure = t.getClientProperty(Constants.STRUCTURE_PROPERTY);
            e.getPresentation().setEnabled(structureClass.isInstance(structure));
        } else {
            e.getPresentation().setEnabled(false);
        }

    }
}
