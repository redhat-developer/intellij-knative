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
package com.redhat.devtools.intellij.knative.actions.toolbar;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.BuildRunDeployFuncPanel;
import com.redhat.devtools.intellij.knative.func.IFuncAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class StopFunctionTaskAction extends DumbAwareAction {

    private final BuildRunDeployFuncPanel panel;
    private static final Icon showHistoryIcon = AllIcons.Actions.Suspend;

    public StopFunctionTaskAction(BuildRunDeployFuncPanel panel) {
        super("Stop Function Execution", "Stop function task execution", showHistoryIcon);
        this.panel = panel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        IFuncAction funcAction = panel.getSelectedFuncActionNode();
        if (funcAction != null) {
            funcAction.stop();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        IFuncAction funcAction = panel.getSelectedFuncActionNode();
        e.getPresentation().setEnabled(funcAction != null && !funcAction.isFinished());
    }
}
