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
package com.redhat.devtools.intellij.knative.ui.brdWindow.buildFuncWindowTab;

import com.intellij.openapi.wm.ToolWindow;
import com.redhat.devtools.intellij.knative.ui.brdWindow.BRDFuncPanel;
import com.redhat.devtools.intellij.knative.ui.brdWindow.IFuncAction;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_CONTENT_NAME;

public class BuildFuncPanel extends BRDFuncPanel {

    public BuildFuncPanel(ToolWindow toolWindow) {
        super(toolWindow, BUILDFUNC_CONTENT_NAME);
    }

    protected DefaultMutableTreeNode createFuncActionTreeNode(List<IFuncAction> actionFuncHandlers) {
        IFuncAction buildAction = actionFuncHandlers.get(0);
        DefaultMutableTreeNode buildNode = createTreeNode(
                buildAction,
                () -> showHistory ? buildAction.getFuncName() + " [latest-build]:" : "Build " + buildAction.getFuncName() + " [latest]:",
                () -> createReadableBuildLocation(buildAction)
        );
        if(showHistory) {
            addChildrenToBuildFuncTreeNode(buildNode, actionFuncHandlers);
        }
        return buildNode;
    }

    private String createReadableBuildLocation(IFuncAction funcAction) {
        if (showHistory) {
            return getNodeLocation(funcAction);
        }
        if (funcAction instanceof BuildFuncActionPipeline
                && funcAction.isFinished()) {
            return getBuildLocation(funcAction);
        }
        if (funcAction instanceof BuildFuncActionTask) {
            return getBuildLocation(funcAction, "running... ");
        }
        return funcAction.getState();
    }

    private void addChildrenToBuildFuncTreeNode(DefaultMutableTreeNode parent, List<IFuncAction> actionFuncHandlers) {
        for (IFuncAction buildFuncHandler: actionFuncHandlers) {
            DefaultMutableTreeNode buildNode = createTreeNode(
                    buildFuncHandler,
                    () -> ":build",
                    () -> createReadableHistoryLocation(buildFuncHandler)
            );
            parent.add(buildNode);
        }
    }

    private String createReadableHistoryLocation(IFuncAction funcAction) {
        if (funcAction.isFinished()) {
            return (!funcAction.isSuccessfullyCompleted() ?
                    funcAction.getState() :
                    funcAction.getFunction().getImage()) +
                    " <span style=\"color: gray;\">At " + funcAction.getStartingDate() + "</span>";
        }
        return funcAction.getState();
    }


}
