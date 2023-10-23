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
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.EditorHelper;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class OpenInEditorActionTest extends ActionTest {

    public void testActionPerformed_OpenKnComponentInEditorIsCalled() {
        AnAction action = new OpenInEditorAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        ParentableNode node = mock(ParentableNode.class);
        try(MockedStatic<StructureTreeAction> structureTreeActionMockedStatic = mockStatic(StructureTreeAction.class)) {
            structureTreeActionMockedStatic.when(() -> StructureTreeAction.getElement(any())).thenReturn(node);
            try (MockedStatic<EditorHelper> editorHelperMockedStatic = mockStatic(EditorHelper.class)) {
                action.actionPerformed(anActionEvent);
                editorHelperMockedStatic.verify(() -> EditorHelper.openKnComponentInEditor(any(ParentableNode.class)), times(1));
            }
        }
    }
}
