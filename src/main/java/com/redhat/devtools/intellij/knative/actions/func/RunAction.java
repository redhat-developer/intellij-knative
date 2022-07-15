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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.func.RunFuncActionPipeline;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.func.RunFuncActionTask;
import com.redhat.devtools.intellij.knative.func.FuncActionTask;
import com.redhat.devtools.intellij.knative.func.FuncActionPipelineBuilder;
import com.redhat.devtools.intellij.knative.func.IFuncActionPipeline;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;

import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_MISC;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class RunAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(RunAction.class);
    private static final TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_MISC + "run func");

    public RunAction() {
        super(KnFunctionNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {

        ParentableNode node = getElement(selected);
        String name = node.getName();
        String namespace = knCli.getNamespace();
        Project project = getEventProject(anActionEvent);
        Function function = ((KnFunctionNode) node).getFunction();
        String localPathFunc = function.getLocalPath();
        if (localPathFunc.isEmpty()) {
            telemetry
                    .result(anonymizeResource(name, namespace, "Function " + name + "is not opened locally"))
                    .send();
            return;
        }

        IFuncActionPipeline runPipeline = new FuncActionPipelineBuilder()
                .createRunPipeline(project, function)
                .withBuildTask((task) -> doBuild(knCli, task))
                .withRunTask((task) -> doRun(name, knCli, task, telemetry))
                .build();

        doRunPipeline(project, knCli, runPipeline, name);
    }

    public static void Run(Project project, Function function, Kn knCli, String name, Runnable callbackWhenListeningReady) {
        RunFuncActionPipeline runPipeline = (RunFuncActionPipeline) new FuncActionPipelineBuilder()
                .createRunPipeline(project, function)
                .withRunTask((task) -> doRun(name, knCli, task, telemetry))
                .build();
        RunFuncActionTask runTask = (RunFuncActionTask) runPipeline.getSteps().get(0);
        runTask.setCallbackWhenListeningReady(callbackWhenListeningReady);
        doRunPipeline(project, knCli, runPipeline, name);
    }

    private static void doRunPipeline(Project project, Kn knCli, IFuncActionPipeline runPipeline, String name) {
        boolean isStarted = knCli.getFuncActionPipelineManager().start(runPipeline);
        if (!isStarted) {
            int response = Messages.showYesNoDialog(
                    project,
                    "Process run " + name + " is not allowed to run in parallel.\nWould you like to stop the running one?",
                    "Process run " + name + " is running",
                    "Stop and Rerun",
                    "Cancel",
                    AllIcons.General.QuestionDialog
            );
            if (response == Messages.YES) {
                knCli.getFuncActionPipelineManager().stopAndRerun(runPipeline);
            }
        }
    }

    private static void doBuild(Kn knCli, FuncActionTask funcActionTask) {
        ExecHelper.submit(() -> BuildAction.execute(
                funcActionTask.getProject(),
                funcActionTask.getFunction(),
                knCli,
                funcActionTask)
        );
    }

    private static void doRun(String name, Kn knCli, FuncActionTask funcActionTask, TelemetryMessageBuilder.ActionMessage telemetry) {
        ExecHelper.submit(() -> {
            try {
                knCli.runFunc(funcActionTask.getFunction().getLocalPath(), funcActionTask.getTerminalExecutionConsole(),
                        funcActionTask.getProcessHandlerFunction(), funcActionTask.getProcessListener());
                telemetry
                        .result(anonymizeResource(name, knCli.getNamespace(), "Function " + name + " is running locally"))
                        .send();
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
                telemetry
                        .error(anonymizeResource(name, knCli.getNamespace(), e.getLocalizedMessage()))
                        .send();
            }
        });
    }

    @Override
    public boolean isVisible(Object selected) {
        return selected instanceof KnFunctionNode && !((KnFunctionNode) selected).getFunction().getLocalPath().isEmpty();
    }
}
