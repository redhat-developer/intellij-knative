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
package com.redhat.devtools.intellij.knative.ui.brdWindowTabs.runFuncWindowTab;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.ActionFuncHandler;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.ActionFuncStepHandler;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.BRDFuncPanel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

import static com.redhat.devtools.intellij.knative.Constants.RUNFUNC_CONTENT_NAME;

public class RunFuncPanel extends BRDFuncPanel {

    public RunFuncPanel(ToolWindow toolWindow) {
        super(toolWindow, RUNFUNC_CONTENT_NAME);
    }

    @Override
    public ActionFuncHandler createActionFuncHandler(Project project, Function function, List<String> steps) {
        return super.createActionFuncHandler("Run", project, function, steps);
    }

    @Override
    protected DefaultMutableTreeNode createActionFuncTreeNode(List<ActionFuncHandler> actionFuncHandlers) {
        ActionFuncHandler runningBuild = actionFuncHandlers.get(0);
        if (showHistory) {
            DefaultMutableTreeNode runNode = new DefaultMutableTreeNode(
                    new LabelAndIconDescriptor(runningBuild.getProject(),
                            runningBuild,
                            () -> runningBuild.getFuncName() + " [" + runningBuild.getRunningStep().getActionName().toLowerCase() + "]:" ,
                            () -> createReadableBuildLocation(runningBuild),
                            runningBuild::getStateIcon,
                            null));
            for (ActionFuncHandler actionFuncHandler: actionFuncHandlers) {
                runNode.add(createRunFuncTreeNode(actionFuncHandler));
            }
            return runNode;
        } else {
            return createRunFuncTreeNode(runningBuild);
        }
    }

    private DefaultMutableTreeNode createRunFuncTreeNode(ActionFuncHandler runFuncHandler) {
        DefaultMutableTreeNode runNode = new DefaultMutableTreeNode(
                new LabelAndIconDescriptor(runFuncHandler.getProject(),
                        runFuncHandler,
                        () -> "Run " + runFuncHandler.getFuncName(),
                        () -> createReadableBuildLocation(runFuncHandler),
                        runFuncHandler::getStateIcon,
                        null));
        List<ActionFuncStepHandler> stepHandlers = runFuncHandler.getSteps();
        for (ActionFuncStepHandler step: stepHandlers) {
            DefaultMutableTreeNode stepNode = new DefaultMutableTreeNode(
                    new LabelAndIconDescriptor(runFuncHandler.getProject(),
                            runFuncHandler,
                            () -> step.getActionName() + " " + runFuncHandler.getFuncName(),
                            () -> createReadableBuildLocation(runFuncHandler),
                            runFuncHandler::getStateIcon,
                            null));
            runNode.add(stepNode);
        }
        return runNode;
    }
}
