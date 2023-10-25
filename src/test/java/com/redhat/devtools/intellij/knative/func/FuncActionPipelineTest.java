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
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mockConstruction;

public class FuncActionPipelineTest extends BaseTest {
    private FuncActionPipeline pipeline;

    public void setUp() throws Exception {
        super.setUp();
        pipeline = new FuncActionPipeline("name", project, function) {
            @Override
            protected String getTabName() {
                return "test";
            }
        };
    }

    public void testSetTasks_TwoTasks() throws IOException {
        List<FuncActionTask> tasks = new ArrayList<>();
        tasks.add(new BuildFuncActionTask((task) -> {}));
        tasks.add(new FuncActionTask("test", (task) -> {}));

        try (MockedConstruction<TerminalExecutionConsole> ignored = mockConstruction(TerminalExecutionConsole.class)) {
            pipeline.setTasks(tasks);
            assertEquals("buildImage", pipeline.getRunningStep().getActionName());
            assertEquals(2, pipeline.getSteps().size());

            pipeline.removeTask(0);
            assertEquals(1, pipeline.getSteps().size());
            assertEquals("test", pipeline.getRunningStep().getActionName());
        }
    }

    public void testFireTerminatedStep_FailedTask_SkippedRemaining() throws IOException {
        List<FuncActionTask> tasks = new ArrayList<>();
        BuildFuncActionTask buildTask = new BuildFuncActionTask((task) -> {});

        tasks.add(buildTask);
        tasks.add(new FuncActionTask("test", (task) -> {}));

        try (MockedConstruction<TerminalExecutionConsole> ignored = mockConstruction(TerminalExecutionConsole.class)) {
            pipeline.setTasks(tasks);
            assertEquals("Waiting to start", pipeline.getSteps().get(1).getState());
            buildTask.setState(new String[] {"failed"});
            pipeline.fireTerminatedStep(() -> buildTask);
            assertEquals("skipped", pipeline.getSteps().get(1).getState());
        }
    }

    public void testFireTerminatedStep_LastTaskFinished_SetEndTime() throws IOException {
        List<FuncActionTask> tasks = new ArrayList<>();
        BuildFuncActionTask buildTask = new BuildFuncActionTask((task) -> {});
        FuncActionTask finalTask = new FuncActionTask("test", (task) -> {});

        tasks.add(buildTask);
        tasks.add(finalTask);

        try (MockedConstruction<TerminalExecutionConsole> ignored = mockConstruction(TerminalExecutionConsole.class)) {
            pipeline.setTasks(tasks);
            assertEquals(-1, pipeline.getEndTime());
            finalTask.setState(new String[] {"successful"});
            pipeline.fireTerminatedStep(() -> finalTask);
            assertNotEquals(-1, pipeline.getEndTime());
        }
    }

    public void testFireTerminatedStep_IntermediateTaskFinished_SetNextOneAsExecuting() throws IOException {
        List<FuncActionTask> tasks = new ArrayList<>();
        BuildFuncActionTask buildTask = new BuildFuncActionTask((task) -> {});
        FuncActionTask finalTask = new FuncActionTask("test", (task) -> {});
        tasks.add(buildTask);
        tasks.add(finalTask);

        try (MockedConstruction<TerminalExecutionConsole> ignored = mockConstruction(TerminalExecutionConsole.class)) {
            pipeline.setTasks(tasks);
            assertEquals(buildTask, pipeline.getRunningStep());

            buildTask.setState(new String[] {"successful"});
            pipeline.fireTerminatedStep(() -> buildTask);
            assertEquals(finalTask, pipeline.getRunningStep());
        }
    }
}
