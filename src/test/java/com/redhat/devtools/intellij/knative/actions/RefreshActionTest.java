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
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RefreshActionTest extends ActionTest {

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
        AnActionEvent anActionEvent = createRefreshActionEvent(knTreeStructure);
        when(anActionEvent.getPlace()).thenReturn(Constants.TOOLBAR_PLACE);

        action.actionPerformed(anActionEvent);
        verify(knTreeStructure).fireModified(any());
    }

    @Test
    public void ActionPerformed_EventFromTree_Refresh() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = createRefreshActionEvent(knTreeStructure);
        try(MockedStatic<StructureTreeAction> structureTreeActionMockedStatic = mockStatic(StructureTreeAction.class)) {
            when(kn.getNamespace()).thenReturn("namespace");
            when(knRootNode.getKn()).thenReturn(kn);
            when(knServiceNode.getRootNode()).thenReturn(knRootNode);
            when(knServiceNode.getName()).thenReturn("name");
            structureTreeActionMockedStatic.when(() -> StructureTreeAction.getElement(any())).thenReturn(knServiceNode);
            action.actionPerformed(anActionEvent);
            structureTreeActionMockedStatic.verify(() -> StructureTreeAction.getElement(any()), times(1));
            verify(knTreeStructure).fireModified(any());
        }
    }

    @Test
    public void Update_EventNotFromToolbarAndNotFromTree_NotEnabled() {
        presentation = new Presentation();
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
        AnActionEvent anActionEvent = createRefreshActionEvent(null);
        when(anActionEvent.getPlace()).thenReturn(Constants.TOOLBAR_PLACE);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(null);

        action.update(anActionEvent);
        assertFalse(presentation.isEnabled());
    }

    @Test
    public void Update_EventFromToolbarWithKnTreeStructure_Enabled() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = createRefreshActionEvent(knTreeStructure);
        when(anActionEvent.getPlace()).thenReturn(Constants.TOOLBAR_PLACE);

        action.update(anActionEvent);
        assertTrue(presentation.isEnabled());
    }

    @Test
    public void Update_EventFromToolbarWithoutKnTreeStructure_NotEnabled() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = createRefreshActionEvent(null);
        when(anActionEvent.getPlace()).thenReturn(Constants.TOOLBAR_PLACE);

        action.update(anActionEvent);
        assertFalse(presentation.isEnabled());
    }

    @Test
    public void Update_EventFromTreeWithValidSelectedNode_Enabled() {
        AnAction action = new RefreshAction();
        AnActionEvent anActionEvent = createRefreshActionEvent(null);

        action.update(anActionEvent);
        assertTrue(presentation.isEnabled());
    }

    public AnActionEvent createRefreshActionEvent(KnTreeStructure structureProperty) {
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(kn.getNamespace()).thenReturn("namespace");
        when(knRootNode.getKn()).thenReturn(kn);
        when(knServiceNode.getRootNode()).thenReturn(knRootNode);
        when(knServiceNode.getName()).thenReturn("name");
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(structureProperty);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(anActionEvent.getPresentation()).thenReturn(presentation);
        return anActionEvent;
    }
}
