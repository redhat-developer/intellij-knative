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

import com.fasterxml.jackson.databind.JsonNode;
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

public class RemoveEnvAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(RemoveEnvAction.class);

    public RemoveEnvAction() {
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
                knCli.removeEnv(localPathFunc);
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        });
    }

    @Override
    public boolean isVisible(Object selected) {
        // removeEnv action is enabled only if the user works with a func opened locally and atleast an env has been set
        if (selected instanceof KnFunctionNode) {
            Kn kn = ((KnFunctionNode) selected).getRootNode().getKn();
            String localPath = ((KnFunctionNode) selected).getFunction().getLocalPath();
            JsonNode envsSection;
            try {
                envsSection = FuncUtils.getFuncSection(kn, localPath, new String[] { "envs" });
            } catch (IOException e) {
                return false;
            }
            return !localPath.isEmpty() && envsSection != null && !envsSection.isEmpty();
        }
        return false;
    }
}
