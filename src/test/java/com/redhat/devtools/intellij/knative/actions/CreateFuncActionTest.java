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
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.actions.func.CreateFuncAction;
import com.redhat.devtools.intellij.knative.ui.createFunc.CreateFuncDialog;
import com.redhat.devtools.intellij.knative.ui.createFunc.CreateFuncModel;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.IOException;
import javax.swing.tree.TreePath;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class CreateFuncActionTest extends  ActionTest {

    @Test
    public void ActionPerformed_CreateDialogIsCancelled_DoNothing() throws IOException {
        AnAction action = new CreateFuncAction();
        AnActionEvent anActionEvent = createCreateFuncActionEvent();
        try(MockedConstruction<CreateFuncDialog> createFuncDialogMockedConstruction = mockConstruction(CreateFuncDialog.class,
                (mock, context) -> {
                    when(mock.isOK()).thenReturn(false);
                })) {
            action.actionPerformed(anActionEvent);
            verify(kn, times(0)).createFunc(any(CreateFuncModel.class));
        }
    }

    @Test
    public void ActionPerformed_SelectedHasLocalPathAndUserImportFunctionInProject_DoRun() throws IOException {
        AnAction action = new CreateFuncAction();
        AnActionEvent anActionEvent = createCreateFuncActionEvent();
        CreateFuncModel createFuncModel = mock(CreateFuncModel.class);
        try(MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.submit(any(Runnable.class))).then((Answer) invocation -> {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            });
            try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
                try (MockedConstruction<CreateFuncDialog> createFuncDialogMockedConstruction = mockConstruction(CreateFuncDialog.class,
                        (mock, context) -> {
                            when(mock.isOK()).thenReturn(true);
                            when(mock.getModel()).thenReturn(createFuncModel);
                        })) {
                    treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
                    action.actionPerformed(anActionEvent);
                    verify(kn, times(1)).createFunc(any(CreateFuncModel.class));
                }
            }
        }
    }


    private AnActionEvent createCreateFuncActionEvent() throws IOException {
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(anActionEvent.getData(CommonDataKeys.PROJECT)).thenReturn(project);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(knFunctionsTreeStructure);
        when(anActionEvent.getProject()).thenReturn(project);
        when(model.getSelectionPath()).thenReturn(path3);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path3});

        return anActionEvent;
    }
}
