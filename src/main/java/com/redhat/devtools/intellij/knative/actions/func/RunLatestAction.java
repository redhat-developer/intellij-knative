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

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.ui.brdWindow.FuncActionPipeline;
import com.redhat.devtools.intellij.knative.ui.brdWindow.FuncActionTask;
import com.redhat.devtools.intellij.knative.ui.brdWindow.FuncActionsPipelineBuilder;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;

public class RunLatestAction extends RunAction{

    @Override
    protected FuncActionPipeline createPipeline(Project project, Kn knCli, Function function, String nodeName, TelemetryMessageBuilder.ActionMessage telemetry) {
        return new FuncActionsPipelineBuilder()
                .createRunPipeline(project, function)
                .withBuildTask((task) -> doBuild(knCli, task))
                .withTask("runFunc", (task) -> doRun(nodeName, knCli, task, telemetry))
                .build();
    }

    private void doBuild(Kn knCli, FuncActionTask funcActionTask) {
        ExecHelper.submit(() -> BuildAction.execute(
                funcActionTask.getProject(),
                funcActionTask.getFunction(),
                knCli,
                funcActionTask)
        );
    }
}
