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
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.FuncActionPipeline;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.FuncActionTask;
import com.redhat.devtools.intellij.knative.ui.brdWindowTabs.FuncActionsPipelineBuilder;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;

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
        Project project = getEventProject(anActionEvent);
        Function function = ((KnFunctionNode) node).getFunction();
        String localPathFunc = function.getLocalPath();
        if (localPathFunc.isEmpty()) {
            telemetry
                    .result(anonymizeResource(name, namespace, "Function " + name + "is not opened locally"))
                    .send();
            return;
        }

        FuncActionPipeline runPipeline = new FuncActionsPipelineBuilder()
                .createRunPipeline(project, function)
                .withBuildTask((task) -> doBuild(knCli, task))
                .withTask("runFunc", (task) -> doRun(name, knCli, task, telemetry))
                .build();
        runPipeline.start();
    }

    private void doBuild(Kn knCli, FuncActionTask funcActionTask) {
        ExecHelper.submit(() -> BuildAction.execute(
                funcActionTask.getProject(),
                funcActionTask.getFunction(),
                knCli,
                funcActionTask)
        );
    }

    private void doRun(String name, Kn knCli, FuncActionTask funcActionTask, TelemetryMessageBuilder.ActionMessage telemetry) {
        ExecHelper.submit(() -> {
            try {
                knCli.runFunc(funcActionTask.getFunction().getLocalPath(), funcActionTask.getTerminalExecutionConsole(), funcActionTask.getProcessListener());
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
