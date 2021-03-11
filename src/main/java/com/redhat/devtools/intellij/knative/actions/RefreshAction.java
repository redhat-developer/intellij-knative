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
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.tree.KnEventingNode;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.KnServingNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;

import javax.swing.tree.TreePath;

public class RefreshAction extends StructureTreeAction {
    public RefreshAction() {
        super(KnRootNode.class, KnServingNode.class, KnServiceNode.class, KnRevisionNode.class, KnEventingNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        selected = StructureTreeAction.getElement(selected);
        KnTreeStructure structure = (KnTreeStructure) getTree(anActionEvent).getClientProperty(Constants.STRUCTURE_PROPERTY);
        structure.fireModified(selected);
    }

    @Override
    public void update(AnActionEvent e) {
        if (!(e.getData(PlatformDataKeys.CONTEXT_COMPONENT) instanceof Tree)) {
            e.getPresentation().setEnabled(false);
        } else {
            e.getPresentation().setEnabled(true);
            super.update(e);
        }
    }
}
