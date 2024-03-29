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
package com.redhat.devtools.intellij.knative.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.actions.func.RunAction;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.func.FuncActionTask;
import com.redhat.devtools.intellij.knative.func.IFuncActionPipeline;
import com.redhat.devtools.intellij.knative.func.RunFuncActionPipeline;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;

import java.io.IOException;
import javax.swing.tree.TreePath;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class RunActionTest extends ActionTest {

    private Function function;

    public void setUp() throws Exception {
        super.setUp();
        function = mock(Function.class);
    }

    public void testActionPerformed_SelectedHasNotLocalPath_DoNothing() throws IOException, InterruptedException {
        AnAction action = new RunAction();
        AnActionEvent anActionEvent = createRunActionEvent();
        when(function.getLocalPath()).thenReturn("");
        try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            when(kn.getNamespace()).thenReturn("namespace");
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any(Project.class))).thenReturn(kn);
            action.actionPerformed(anActionEvent);
            Thread.sleep(1000);
            verify(kn, times(0)).runFunc(anyString(), any(), any(), any());
        }
    }

    public void testActionPerformed_SelectedHasLocalPath_DoRun() throws IOException, InterruptedException {
        AnAction action = new RunAction();
        AnActionEvent anActionEvent = createRunActionEvent();
        when(function.getLocalPath()).thenReturn("path");
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try(MockedConstruction<FuncActionTask> ignored = mockConstruction(FuncActionTask.class)) {
                try(MockedConstruction<RunFuncActionPipeline> runFuncActionPipelineMockedConstruction = mockConstruction(RunFuncActionPipeline.class,
                        (mock, context) -> {
                            doNothing().when(mock).start();
                })) {
                    when(manager.start(any(IFuncActionPipeline.class))).thenReturn(true);
                    treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
                    action.actionPerformed(anActionEvent);
                    Thread.sleep(1000);
                    verify(manager, times(1)).start(runFuncActionPipelineMockedConstruction.constructed().get(0));
               }
            }
        }
    }

    private AnActionEvent createRunActionEvent() throws IOException {
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(knFunctionsTreeStructure);
        when(anActionEvent.getProject()).thenReturn(project);
        when(model.getSelectionPath()).thenReturn(path3);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path3});
        when(knFunctionNode.getFunction()).thenReturn(function);

        return anActionEvent;
    }
}
