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
import org.junit.Test;
import org.mockito.MockedConstruction;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockConstruction;


public class FuncActionPipelineBuilderTest extends BaseTest {
    public void testCreateBuildPipeline_BuildFuncActionPipeline() throws IOException {
        try (MockedConstruction<TerminalExecutionConsole> ignored = mockConstruction(TerminalExecutionConsole.class)) {
            IFuncActionPipeline buildPipeline = new FuncActionPipelineBuilder()
                    .createBuildPipeline(project, function)
                    .withBuildTask((task) -> {})
                    .build();

            assertTrue(buildPipeline instanceof BuildFuncActionPipeline);
            assertEquals(1, ((BuildFuncActionPipeline) buildPipeline).getSteps().size());
        }
    }

    public void testCreateRunPipeline_RunFuncActionPipeline() throws IOException {
        try (MockedConstruction<TerminalExecutionConsole> ignored = mockConstruction(TerminalExecutionConsole.class)) {
            IFuncActionPipeline runPipeline = new FuncActionPipelineBuilder()
                    .createRunPipeline(project, function)
                    .withBuildTask((task) -> {})
                    .withTask("run", (task) -> {})
                    .build();

            assertTrue(runPipeline instanceof RunFuncActionPipeline);
            assertEquals(2, ((RunFuncActionPipeline) runPipeline).getSteps().size());
        }
    }

    public void testCreateDeployPipeline_DeployFuncActionPipeline() throws IOException {
        try (MockedConstruction<TerminalExecutionConsole> ignored = mockConstruction(TerminalExecutionConsole.class)) {
            IFuncActionPipeline deployPipeline = new FuncActionPipelineBuilder()
                    .createDeployPipeline(project, function)
                    .withBuildTask((task) -> {})
                    .withTask("deploy", (task) -> {})
                    .build();

            assertTrue(deployPipeline instanceof DeployFuncActionPipeline);
            assertEquals(2, ((DeployFuncActionPipeline) deployPipeline).getSteps().size());
        }
    }
}
