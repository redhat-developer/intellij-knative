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

import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.ActionFuncHandler;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.ActionFuncStepHandler;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.buildFuncWindowTab.BuildFuncPanel;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.runFuncWindowTab.RunFuncPanel;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_CONTENT_NAME;
import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_TOOLWINDOW_ID;
import static com.redhat.devtools.intellij.knative.Constants.RUNFUNC_CONTENT_NAME;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_MISC;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class RunAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(RunAction.class);

    public RunAction() {
        super(KnFunctionNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_MISC + "run func");
        ParentableNode node = getElement(selected);
        String name = node.getName();
        String namespace = knCli.getNamespace();
        String localPathFunc = ((KnFunctionNode) node).getFunction().getLocalPath();
        if (localPathFunc.isEmpty()) {
            telemetry
                    .result(anonymizeResource(name, namespace, "Function " + name + "is not opened locally"))
                    .send();
            return;
        }

        ActionFuncHandler runActionHandler = createRunFuncHandler(getEventProject(anActionEvent),
                ((KnFunctionNode) node).getFunction(),
                Arrays.asList("Build", "Run"));

        ExecHelper.submit(() -> {
            try {
                ActionFuncStepHandler buildStep = runActionHandler.getStep("Build");
                //CommonTerminalExecutionConsole terminalExecutionConsole = knCli.createTerminalTabToReuse();
                BuildAction.execute(getEventProject(anActionEvent), ((KnFunctionNode) node).getFunction(),
                        knCli, buildStep);

                ActionFuncStepHandler runStep = runActionHandler.getStep("Run");
                knCli.runFunc(localPathFunc, runStep.getTerminalExecutionConsole());
                telemetry
                        .result(anonymizeResource(name, namespace, "Function " + name + " is running locally"))
                        .send();
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
                telemetry
                        .error(anonymizeResource(name, namespace, e.getLocalizedMessage()))
                        .send();
            }
        });
    }

    private ActionFuncHandler createRunFuncHandler(Project project, Function function, List<String> steps) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(BUILDFUNC_TOOLWINDOW_ID);
        RunFuncPanel buildOutputPanel = (RunFuncPanel) toolWindow.getContentManager().findContent(RUNFUNC_CONTENT_NAME);
        return buildOutputPanel.createActionFuncHandler(project, function, steps);
    }

    @Override
    public boolean isVisible(Object selected) {
        return selected instanceof KnFunctionNode && !((KnFunctionNode) selected).getFunction().getLocalPath().isEmpty();
    }
}
