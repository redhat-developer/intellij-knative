/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
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
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.FuncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;

public abstract class AddConfigAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(AddConfigAction.class);

    public AddConfigAction() {
        super(KnFunctionNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        ParentableNode node = getElement(selected);
        String localPathFunc = ((KnFunctionNode) node).getFunction().getLocalPath();
        if (localPathFunc.isEmpty()) {
            return;
        }

        ExecHelper.submit(() -> {
            try {
                doAddConfig(knCli, localPathFunc);
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        });
    }

    public abstract void doAddConfig(Kn kncli, String path) throws IOException;

    @Override
    public boolean isVisible(Object selected) {
        // addConfig action is enabled only if the user works with a func opened locally and have access to a cluster
        // this is because addConfig action can work with secrets and configmaps
        if (selected instanceof KnFunctionNode) {
            Kn kn = ((KnFunctionNode) selected).getRootNode().getKn();
            return !((KnFunctionNode) selected).getFunction().getLocalPath().isEmpty()
                    && FuncUtils.isKnativeReady(kn);
        }
        return false;
    }
}
