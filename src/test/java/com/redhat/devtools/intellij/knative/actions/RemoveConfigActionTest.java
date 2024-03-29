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

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.actions.func.RemoveEnvAction;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.utils.FuncUtils;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
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

public class RemoveConfigActionTest extends ActionTest {

    private Function function;

    public void setUp() throws Exception {
        super.setUp();
        function = mock(Function.class);
    }

    public void testActionPerformed_SelectedHasNotLocalPath_DoNothing() throws IOException {
        AnAction action = new RemoveEnvAction();
        when(function.getLocalPath()).thenReturn("");
        AnActionEvent anActionEvent = createRemoveConfigActionEvent();
        try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            when(kn.getNamespace()).thenReturn("namespace");
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any(Project.class))).thenReturn(kn);
            action.actionPerformed(anActionEvent);
            verify(kn, times(0)).removeEnv(anyString());
        }
    }

    public void testActionPerformed_SelectedHasLocalPath_StartRemoveEnvWorkflow() {
        AnAction action = new RemoveEnvAction();
        when(function.getLocalPath()).thenReturn("path");
        AnActionEvent anActionEvent = createRemoveConfigActionEvent();
        try(MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
                when(kn.getNamespace()).thenReturn("namespace");
                treeHelperMockedStatic.when(() -> TreeHelper.getKn(any(Project.class))).thenReturn(kn);
                action.actionPerformed(anActionEvent);
                execHelperMockedStatic.verify(() -> ExecHelper.submit(any(Runnable.class)));
            }
        }
    }

    public void testIsVisible_SelectHasNoLocalPath_False() {
        RemoveEnvAction action = new RemoveEnvAction();
        setKnFunctionNode();
        when(function.getLocalPath()).thenReturn("");
        boolean isVisible = action.isVisible(knFunctionNode);
        assertFalse(isVisible);
    }

    public void testIsVisible_SelectHasLocalPathButNotEnvsSection_False() {
        RemoveEnvAction action = new RemoveEnvAction();
        setKnFunctionNode();
        when(function.getLocalPath()).thenReturn("path");
        try(MockedStatic<FuncUtils> funcUtilsMockedStatic = mockStatic(FuncUtils.class)) {
            funcUtilsMockedStatic.when(() -> FuncUtils.getFuncSection(any(Kn.class), anyString(), any())).thenReturn(null);
            boolean isVisible = action.isVisible(knFunctionNode);
            assertFalse(isVisible);
        }
    }

    public void testIsVisible_SelectHasLocalPathButEmptyEnvsSection_False() {
        RemoveEnvAction action = new RemoveEnvAction();
        setKnFunctionNode();
        when(function.getLocalPath()).thenReturn("path");
        JsonNode envSection = mock(JsonNode.class);
        try(MockedStatic<FuncUtils> funcUtilsMockedStatic = mockStatic(FuncUtils.class)) {
            when(envSection.isEmpty()).thenReturn(true);
            funcUtilsMockedStatic.when(() -> FuncUtils.getFuncSection(any(Kn.class), anyString(), any())).thenReturn(envSection);
            boolean isVisible = action.isVisible(knFunctionNode);
            assertFalse(isVisible);
        }
    }

    public void testIsVisible_SelectHasLocalPathAndAtleastAnEnv_True() {
        RemoveEnvAction action = new RemoveEnvAction();
        setKnFunctionNode();
        when(function.getLocalPath()).thenReturn("path");
        JsonNode envSection = mock(JsonNode.class);
        try(MockedStatic<FuncUtils> funcUtilsMockedStatic = mockStatic(FuncUtils.class)) {
            when(envSection.isEmpty()).thenReturn(false);
            funcUtilsMockedStatic.when(() -> FuncUtils.getFuncSection(any(Kn.class), anyString(), any())).thenReturn(envSection);
            boolean isVisible = action.isVisible(knFunctionNode);
            assertTrue(isVisible);
        }
    }

    public void testIsVisible_SelectedIsNotFunctionNode_False() {
        RemoveEnvAction action = new RemoveEnvAction();
        boolean isVisible = action.isVisible(knEventingNode);
        assertFalse(isVisible);
    }

    private void setKnFunctionNode() {
        when(knFunctionNode.getRootNode()).thenReturn(knRootNode);
        when(knRootNode.getKn()).thenReturn(kn);
        when(knFunctionNode.getFunction()).thenReturn(function);
    }

    private AnActionEvent createRemoveConfigActionEvent() {
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
