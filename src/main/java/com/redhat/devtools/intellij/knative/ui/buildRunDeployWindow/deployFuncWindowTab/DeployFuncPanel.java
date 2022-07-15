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
package com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.deployFuncWindowTab;

import com.intellij.openapi.wm.ToolWindow;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.BuildRunDeployFuncPanel;
import com.redhat.devtools.intellij.knative.func.FuncActionTask;
import com.redhat.devtools.intellij.knative.func.IFuncAction;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

import static com.redhat.devtools.intellij.knative.Constants.DEPLOYFUNC_CONTENT_NAME;

public class DeployFuncPanel extends BuildRunDeployFuncPanel {
    public DeployFuncPanel(ToolWindow toolWindow) {
        super(toolWindow, DEPLOYFUNC_CONTENT_NAME);
    }

    @Override
    protected DefaultMutableTreeNode createFuncActionTreeNode(List<IFuncAction> actionFuncHandlers) {
        IFuncAction deployAction = actionFuncHandlers.get(0);
        if (showHistory) {
            DefaultMutableTreeNode deployNode = createTreeNode(
                    deployAction,
                    () -> deployAction.getFuncName() + " [latest-" + getRunningStep(deployAction).getActionName() + "]:" ,
                    () -> getNodeLocation(deployAction)
            );
            for (IFuncAction actionFuncHandler: actionFuncHandlers) {
                deployNode.add(createDeployFuncTreeNode(actionFuncHandler));
            }
            return deployNode;
        } else {
            return createDeployFuncTreeNode(deployAction);
        }
    }

    private DefaultMutableTreeNode createDeployFuncTreeNode(IFuncAction deployFuncHandler) {
        DefaultMutableTreeNode deployNode = createTreeNode(
                deployFuncHandler,
                () -> deployFuncHandler.getFuncName() + " [" + getRunningStep(deployFuncHandler).getActionName() + "]:",
                () -> getNodeLocation(deployFuncHandler)
        );

        List<FuncActionTask> tasks = getSteps(deployFuncHandler);
        for (FuncActionTask task: tasks) {
            DefaultMutableTreeNode stepNode = createTreeNode(
                    task,
                    () -> ":" + task.getActionName(),
                    () -> getTaskLocation(task)
            );
            deployNode.add(stepNode);
        }
        return deployNode;
    }


}
