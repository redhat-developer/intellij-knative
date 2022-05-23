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
package com.redhat.devtools.intellij.knative.ui.brdWindowTabs.buildFuncWindowTab;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.ActionFuncHandler;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.BRDFuncPanel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_CONTENT_NAME;

public class BuildFuncPanel extends BRDFuncPanel {

    public BuildFuncPanel(ToolWindow toolWindow) {
        super(toolWindow, BUILDFUNC_CONTENT_NAME);
    }

    @Override
    public ActionFuncHandler createActionFuncHandler(Project project, Function function, List<String> steps) {
        return super.createActionFuncHandler("Build", project, function, steps);
    }

    protected DefaultMutableTreeNode createActionFuncTreeNode(List<ActionFuncHandler> actionFuncHandlers) {
        ActionFuncHandler runningBuild = actionFuncHandlers.get(0);
        DefaultMutableTreeNode buildNode = new DefaultMutableTreeNode(
                new LabelAndIconDescriptor(runningBuild.getProject(),
                        runningBuild,
                        () -> showHistory ? runningBuild.getFuncName() + " [latest-build]:" : "Build " + runningBuild.getFuncName() + " [latest]:",
                        () -> createReadableBuildLocation(runningBuild),
                        runningBuild::getStateIcon,
                        null));
        if(showHistory) {
            addChildrenToBuildFuncTreeNode(buildNode, actionFuncHandlers);
        }
        return buildNode;
    }

    private void addChildrenToBuildFuncTreeNode(DefaultMutableTreeNode parent, List<ActionFuncHandler> actionFuncHandlers) {
        for (ActionFuncHandler buildFuncHandler: actionFuncHandlers) {
            DefaultMutableTreeNode buildNode = new DefaultMutableTreeNode(
                    new LabelAndIconDescriptor(buildFuncHandler.getProject(),
                            buildFuncHandler,
                            () -> "Build " + buildFuncHandler.getFuncName(),
                            () -> createReadableHistoryLocation(buildFuncHandler),
                            buildFuncHandler::getStateIcon,
                            null));
            parent.add(buildNode);
        }
    }
}
