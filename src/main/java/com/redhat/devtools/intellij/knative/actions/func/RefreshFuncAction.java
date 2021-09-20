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
package com.redhat.devtools.intellij.knative.actions.func;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.actions.RefreshAction;
import com.redhat.devtools.intellij.knative.tree.KnFunctionLocalNode;
import com.redhat.devtools.intellij.knative.tree.KnLocalFunctionsTreeStructure;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import javax.swing.tree.TreePath;

public class RefreshFuncAction extends RefreshAction {

    public RefreshFuncAction() {
        super(KnFunctionLocalNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
        TreeHelper.refreshFunc(getEventProject(anActionEvent));
    }

    @Override
    public void update(AnActionEvent e) {
        if (Constants.TOOLBAR_PLACE.equals(e.getPlace())) {
            updateAction(e, KnLocalFunctionsTreeStructure.class);
        } else {
            super.update(e);
        }
    }
}
