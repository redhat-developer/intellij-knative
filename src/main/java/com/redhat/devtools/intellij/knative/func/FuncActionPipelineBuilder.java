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
package com.redhat.devtools.intellij.knative.func;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.knative.kn.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FuncActionPipelineBuilder {

    private final List<FuncActionTask> tasks = new ArrayList<>();
    private FuncActionPipeline pipeline;

    public FuncActionPipelineBuilder(){}

    public FuncActionPipelineBuilder createBuildPipeline(Project project, Function function) {
        pipeline = new BuildFuncActionPipeline(project, function);
        return this;
    }

    public FuncActionPipelineBuilder createRunPipeline(Project project, Function function) {
        pipeline = new RunFuncActionPipeline(project, function);
        return this;
    }

    public FuncActionPipelineBuilder createDeployPipeline(Project project, Function function) {
        pipeline = new DeployFuncActionPipeline(project, function);
        return this;
    }

    public FuncActionPipelineBuilder withBuildTask(Consumer<FuncActionTask> doExecute) {
        tasks.add(new BuildFuncActionTask(doExecute));
        return this;
    }

    public FuncActionPipelineBuilder withRunTask(Consumer<FuncActionTask> doExecute) {
        tasks.add(new RunFuncActionTask(doExecute));
        return this;
    }

    public FuncActionPipelineBuilder withTask(String name, Consumer<FuncActionTask> doExecute) {
        tasks.add(new FuncActionTask(name, doExecute));
        return this;
    }

    public IFuncActionPipeline build() {
        if (pipeline != null) {
            pipeline.setTasks(tasks);
        }
        return pipeline;
    }
}
