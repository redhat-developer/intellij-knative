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
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.common.actions.TreeAction;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.EditorHelper;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class OpenInEditorActionTest {

    private Presentation presentation;
    private Tree tree;
    private KnTreeStructure knTreeStructure;
    private TreeAction treeAction;

    @Before
    public void setUp() throws Exception {
        //presentation = mock(Presentation.class);
        tree = mock(Tree.class);
        TreeSelectionModel model = mock(TreeSelectionModel.class);
        TreePath path = mock(TreePath.class);
        KnRootNode applicationsRootNode = mock(KnRootNode.class);
        when(path.getLastPathComponent()).thenReturn(applicationsRootNode);
        when(tree.getSelectionModel()).thenReturn(model);
        when(model.getSelectionPath()).thenReturn(path);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path});
        presentation = new Presentation();

        knTreeStructure = mock(KnTreeStructure.class);
        treeAction = mock(TreeAction.class);
    }

    @Test
    public void ActionPerformed_OpenKnComponentInEditorIsCalled() {
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
