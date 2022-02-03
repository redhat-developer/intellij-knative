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
package com.redhat.devtools.intellij.knative.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.actions.func.AddEnvAction;
import com.redhat.devtools.intellij.knative.actions.func.AddVolumeAction;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.utils.FuncUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import javax.swing.tree.TreePath;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class AddConfigActionTest extends ActionTest {

    private Function function;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        function = mock(Function.class);
    }

    @Test
    public void AddEnvActionPerformed_SelectedHasNotLocalPath_DoNothing() throws IOException {
        AnAction action = new AddEnvAction();
        when(function.getLocalPath()).thenReturn("");
        AnActionEvent anActionEvent = createAddConfigActionEvent();
        action.actionPerformed(anActionEvent);
        verify(kn, times(0)).addEnv(anyString());
    }

    @Test
    public void AddVolumeActionPerformed_SelectedHasNotLocalPath_DoNothing() throws IOException {
        AnAction action = new AddVolumeAction();
        when(function.getLocalPath()).thenReturn("");
        AnActionEvent anActionEvent = createAddConfigActionEvent();
        action.actionPerformed(anActionEvent);
        verify(kn, times(0)).addEnv(anyString());
    }

    @Test
    public void ActionPerformed_SelectedHasLocalPath_StartEnvAddWorkflow() throws IOException, InterruptedException {
        AnAction action = new AddEnvAction();
        when(function.getLocalPath()).thenReturn("path");
        AnActionEvent anActionEvent = createAddConfigActionEvent();
        try(MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            action.actionPerformed(anActionEvent);
            execHelperMockedStatic.verify(() -> ExecHelper.submit(any(Runnable.class)));
        }
    }

    @Test
    public void ActionPerformed_SelectedHasLocalPath_StartVolumeAddWorkflow() throws IOException, InterruptedException {
        AnAction action = new AddVolumeAction();
        when(function.getLocalPath()).thenReturn("path");
        AnActionEvent anActionEvent = createAddConfigActionEvent();
        try(MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            action.actionPerformed(anActionEvent);
            execHelperMockedStatic.verify(() -> ExecHelper.submit(any(Runnable.class)));
        }
    }

    @Test
    public void IsVisible_SelectHasNoLocalPath_False() {
        AddEnvAction action = new AddEnvAction();
        setKnFunctionNode();
        when(function.getLocalPath()).thenReturn("");
        boolean isVisible = action.isVisible(knFunctionNode);
        assertFalse(isVisible);
    }

    @Test
    public void IsVisible_SelectHasLocalPathButNotKnativeReady_False() {
        AddEnvAction action = new AddEnvAction();
        setKnFunctionNode();
        when(function.getLocalPath()).thenReturn("path");
        try(MockedStatic<FuncUtils> funcUtilsMockedStatic = mockStatic(FuncUtils.class)) {
            funcUtilsMockedStatic.when(() -> FuncUtils.isKnativeReady(any(Kn.class))).thenReturn(false);
            boolean isVisible = action.isVisible(knFunctionNode);
            assertFalse(isVisible);
        }
    }

    @Test
    public void IsVisible_SelectHasLocalPathAndKnativeReady_True() {
        AddEnvAction action = new AddEnvAction();
        setKnFunctionNode();
        when(function.getLocalPath()).thenReturn("path");
        try(MockedStatic<FuncUtils> funcUtilsMockedStatic = mockStatic(FuncUtils.class)) {
            funcUtilsMockedStatic.when(() -> FuncUtils.isKnativeReady(any(Kn.class))).thenReturn(true);
            boolean isVisible = action.isVisible(knFunctionNode);
            assertTrue(isVisible);
        }
    }

    @Test
    public void IsVisible_SelectedIsNotFunctionNode_False() {
        AddEnvAction action = new AddEnvAction();
        boolean isVisible = action.isVisible(knEventingNode);
        assertFalse(isVisible);
    }

    private void setKnFunctionNode() {
        when(knFunctionNode.getRootNode()).thenReturn(knRootNode);
        when(knRootNode.getKn()).thenReturn(kn);
        when(knFunctionNode.getFunction()).thenReturn(function);
    }

    private AnActionEvent createAddConfigActionEvent() {
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(knFunctionsTreeStructure);
        setKnFunctionNode();
        when(kn.getNamespace()).thenReturn("namespace");
        when(anActionEvent.getProject()).thenReturn(project);
        when(model.getSelectionPath()).thenReturn(path3);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path3});

        return anActionEvent;
    }

}
