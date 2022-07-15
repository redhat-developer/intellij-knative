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

import com.intellij.icons.AllIcons;
import com.intellij.terminal.TerminalExecutionConsole;
import com.redhat.devtools.intellij.knative.FixtureBaseTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mockConstruction;

public class FuncActionTaskTest extends FixtureBaseTest {
    private FuncActionPipeline pipeline;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        pipeline = new FuncActionPipeline("name", project, function) {
            @Override
            protected String getTabName() {
                return "test";
            }
        };
    }

    @Test
    public void Init_Pipeline_TaskIsSetWithTerminalAndProcessHandlers() {
        FuncActionTask task = new FuncActionTask("test", (t) -> {});
        assertNull(task.getProcessListener());
        assertNull(task.getTerminalExecutionConsole());
        assertNull(task.getProcessHandlerFunction());
        assertNull(task.getState());
        assertNull(task.getStateIcon());
        try (MockedConstruction<TerminalExecutionConsole> ignored = mockConstruction(TerminalExecutionConsole.class)) {
            task.init(pipeline);
            assertNotNull(task.getProcessListener());
            assertNotNull(task.getTerminalExecutionConsole());
            assertNotNull(task.getProcessHandlerFunction());
            assertEquals("Waiting to start", task.getState());
            assertEquals(AllIcons.Actions.Profile, task.getStateIcon());
        }

    }
}
