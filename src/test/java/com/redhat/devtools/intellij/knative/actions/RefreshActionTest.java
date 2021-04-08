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
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RefreshActionTest {
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
    public void ActionPerformed_EventNotFromTree_Nothing() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(null);

        action.actionPerformed(anActionEvent);
        verify(knTreeStructure, never()).fireModified(any());
    }

    @Test
    public void ActionPerformed_EventFromTreeWithoutKnTreeStructure_Nothing() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);

        action.actionPerformed(anActionEvent);
        verify(knTreeStructure, never()).fireModified(any());
    }

    @Test
    public void ActionPerformed_EventFromToolbar_Refresh() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(knTreeStructure);
        when(anActionEvent.getPlace()).thenReturn(Constants.TOOLBAR_PLACE);

        action.actionPerformed(anActionEvent);
        verify(knTreeStructure).fireModified(any());
    }

    @Test
    public void ActionPerformed_EventFromTree_Refresh() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(knTreeStructure);
        try(MockedStatic<StructureTreeAction> structureTreeActionMockedStatic = mockStatic(StructureTreeAction.class)) {
            action.actionPerformed(anActionEvent);
            structureTreeActionMockedStatic.verify(times(1), () -> StructureTreeAction.getElement(any()));
            verify(knTreeStructure).fireModified(any());
        }
    }

    @Test
    public void Update_EventNotFromToolbarAndNotFromTree_NotEnabled() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getPlace()).thenReturn("unknown");
        when(anActionEvent.getPresentation()).thenReturn(presentation);

        action.update(anActionEvent);
        assertFalse(presentation.isEnabledAndVisible());
    }

    @Test
    public void Update_EventFromToolbarWithoutTreeView_NotEnabled() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getPlace()).thenReturn(Constants.TOOLBAR_PLACE);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(null);
        when(anActionEvent.getPresentation()).thenReturn(presentation);

        action.update(anActionEvent);
        assertFalse(presentation.isEnabled());
    }

    @Test
    public void Update_EventFromToolbarWithKnTreeStructure_Enabled() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getPlace()).thenReturn(Constants.TOOLBAR_PLACE);
        when(tree.getClientProperty(anyString())).thenReturn(knTreeStructure);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(anActionEvent.getPresentation()).thenReturn(presentation);

        action.update(anActionEvent);
        assertTrue(presentation.isEnabled());
    }

    @Test
    public void Update_EventFromToolbarWithoutKnTreeStructure_NotEnabled() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getPlace()).thenReturn(Constants.TOOLBAR_PLACE);
        when(tree.getClientProperty(anyString())).thenReturn(null);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(anActionEvent.getPresentation()).thenReturn(presentation);

        action.update(anActionEvent);
        assertFalse(presentation.isEnabled());
    }

    @Test
    public void Update_EventFromTreeWithValidSelectedNode_Enabled() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(tree.getClientProperty(anyString())).thenReturn(null);

        //when(action.isVisible(any())).thenReturn(true);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(anActionEvent.getPresentation()).thenReturn(presentation);

        action.update(anActionEvent);
        assertTrue(presentation.isEnabled());
    }

}
