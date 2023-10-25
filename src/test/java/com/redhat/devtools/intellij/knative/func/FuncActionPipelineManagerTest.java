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

import com.intellij.terminal.TerminalExecutionConsole;
import com.redhat.devtools.intellij.knative.BaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockConstruction;

public class FuncActionPipelineManagerTest extends BaseTest {
    private FuncActionPipeline pipeline;

    public void setUp() throws Exception {
        super.setUp();
        pipeline = new FuncActionPipeline("name", project, function) {
            @Override
            protected String getTabName() {
                return "test";
            }

            @Override
            public void start() {

            }
        };
    }

    public void testOptimizePipeline_PipelineWithTwoTaskAndNoPreviousBuild_OptimizedPipelineHaveTwoTasks() throws IOException {
        List<FuncActionTask> tasks = new ArrayList<>();
        tasks.add(new BuildFuncActionTask((task) -> {}));
        tasks.add(new FuncActionTask("test", (task) -> {}));

        FuncActionPipelineManager manager = new FuncActionPipelineManager();

        try (MockedConstruction<TerminalExecutionConsole> ignored = mockConstruction(TerminalExecutionConsole.class)) {
            pipeline.setTasks(tasks);
            FuncActionPipeline optimizedPipeline = (FuncActionPipeline) manager.optimizePipeline(pipeline);
            assertEquals(2, optimizedPipeline.getSteps().size());
        }
    }

    public void testOptimizePipeline_PipelineWithTwoTaskAndPreviousBuildSuccessful_OptimizedPipelineHaveOneTask() throws IOException {
        List<FuncActionTask> tasks = new ArrayList<>();
        tasks.add(new BuildFuncActionTask((task) -> {}));
        tasks.add(new FuncActionTask("test", (task) -> {}));

        FuncActionPipelineManager manager = new FuncActionPipelineManager();

        try (MockedConstruction<TerminalExecutionConsole> ignored = mockConstruction(TerminalExecutionConsole.class)) {
            pipeline.setTasks(tasks);
            manager.start(pipeline);
            pipeline.getSteps().get(0).setState(new String[] {"successful"});
            FuncActionPipeline optimizedPipeline = (FuncActionPipeline) manager.optimizePipeline(pipeline);
            assertEquals(1, optimizedPipeline.getSteps().size());
        }
    }

    public void testOptimizePipeline_PipelineWithTwoTaskAndPreviousBuildFailed_OptimizedPipelineHaveTwoTasks() throws IOException {
        List<FuncActionTask> tasks = new ArrayList<>();
        tasks.add(new BuildFuncActionTask((task) -> {}));
        tasks.add(new FuncActionTask("test", (task) -> {}));

        FuncActionPipelineManager manager = new FuncActionPipelineManager();

        try (MockedConstruction<TerminalExecutionConsole> ignored = mockConstruction(TerminalExecutionConsole.class)) {
            pipeline.setTasks(tasks);
            manager.start(pipeline);
            pipeline.getSteps().get(0).setState(new String[] {"failed"});
            FuncActionPipeline optimizedPipeline = (FuncActionPipeline) manager.optimizePipeline(pipeline);
            assertEquals(2, optimizedPipeline.getSteps().size());
        }
    }
}
