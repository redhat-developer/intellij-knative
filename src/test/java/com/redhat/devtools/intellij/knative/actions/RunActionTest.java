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
import com.intellij.ui.GroupedElementsRenderer;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.actions.func.RunAction;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.tree.KnFunctionsTreeStructure;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;

import java.io.IOException;
import javax.swing.tree.TreePath;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class RunActionTest extends ActionTest {

    private Function function;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        function = mock(Function.class);
    }

    @Test
    public void ActionPerformed_SelectedHasNotLocalPath_DoNothing() throws IOException, InterruptedException {
        AnAction action = new RunAction();
        AnActionEvent anActionEvent = createRunActionEvent();
        when(function.getLocalPath()).thenReturn("");
        try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            when(kn.getNamespace()).thenReturn("namespace");
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any(Project.class))).thenReturn(kn);
            action.actionPerformed(anActionEvent);
            Thread.sleep(1000);
            verify(kn, times(0)).runFunc(anyString(), any());
        }
    }

    @Test
    public void ActionPerformed_SelectedHasLocalPath_DoRun() throws IOException, InterruptedException {
        AnAction action = new RunAction();
        AnActionEvent anActionEvent = createRunActionEvent();
        when(function.getLocalPath()).thenReturn("path");
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            action.actionPerformed(anActionEvent);
            Thread.sleep(1000);
            verify(kn, times(1)).runFunc(anyString(), any());
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
