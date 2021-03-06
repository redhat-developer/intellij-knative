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
package com.redhat.devtools.intellij.knative.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.IOException;
import javax.swing.tree.TreePath;

public class KnAction  extends StructureTreeAction {

    public KnAction(Class... filters) {
        super(filters);
    }

    public KnAction(boolean acceptMultipleItems, Class... filters) {
        super(acceptMultipleItems, filters);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        try {
            this.actionPerformed(anActionEvent, path, selected, getKn(anActionEvent));
        } catch (IOException e) {
            Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error");
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected) {
        if (selected.length == 0) {
            return;
        }
        try {
            this.actionPerformed(anActionEvent, path, selected, getKn(anActionEvent));
        } catch (IOException e) {
            Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error");
        }
    }

    private Kn getKn(AnActionEvent anActionEvent) throws IOException {
        return TreeHelper.getKn(anActionEvent.getProject());
    }

    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn kn) {}
    
    public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected, Kn kn) {
        actionPerformed(anActionEvent, path[0], selected[0], kn);
    }

}
