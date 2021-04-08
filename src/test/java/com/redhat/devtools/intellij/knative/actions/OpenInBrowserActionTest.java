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

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.TreeAction;
import com.redhat.devtools.intellij.knative.kn.Service;
import com.redhat.devtools.intellij.knative.kn.ServiceStatus;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class OpenInBrowserActionTest {
    private Presentation presentation;
    private Tree tree;
    private KnTreeStructure knTreeStructure;
    private TreeAction treeAction;
    private TreePath path;
    private KnServiceNode knServiceNode;
    private KnRevisionNode knRevisionNode;
    private Service service;

    @Before
    public void setUp() {
        tree = mock(Tree.class);
        path = mock(TreePath.class);
        knServiceNode = mock(KnServiceNode.class);
        knRevisionNode = mock(KnRevisionNode.class);
        service = mock(Service.class);
        TreeSelectionModel model = mock(TreeSelectionModel.class);
        when(tree.getSelectionModel()).thenReturn(model);
        when(path.getLastPathComponent()).thenReturn(knServiceNode);
        when(model.getSelectionPath()).thenReturn(path);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path});
        presentation = new Presentation();

        knTreeStructure = mock(KnTreeStructure.class);
        treeAction = mock(TreeAction.class);
    }

    @Test
    public void IsVisible_SelectedIsKnServiceNodeWithNoURL_False() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(service.getStatus()).thenReturn(null);
        when(knServiceNode.getService(false)).thenReturn(service);
        boolean result = action.isVisible(knServiceNode);
        assertFalse(result);

    }

    @Test
    public void IsVisible_SelectedIsKnServiceNodeWithValidURL_False() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        ServiceStatus serviceStatus = mock(ServiceStatus.class);
        when(serviceStatus.getUrl()).thenReturn("url");
        when(service.getStatus()).thenReturn(serviceStatus);
        when(knServiceNode.getService(false)).thenReturn(service);
        boolean result = action.isVisible(knServiceNode);
        assertTrue(result);
    }

    @Test
    public void IsVisible_SelectedIsNotAKnServiceNode_False() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        boolean result = action.isVisible(knRevisionNode);
        assertFalse(result);
    }

    @Test
    public void ActionPerformed_SelectedIsKnServiceNodeWithoutUrl_BrowseNotCalled() {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(knServiceNode.getService(false)).thenReturn(service);
        when(service.getStatus()).thenReturn(null);
        try(MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)){
            action.actionPerformed(anActionEvent);
            browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(0));
        }
    }

    @Test
    public void ActionPerformed_SelectedIsKnServiceNodeWithUrl_BrowseCalled() {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(knServiceNode.getService(false)).thenReturn(service);
        ServiceStatus serviceStatus = mock(ServiceStatus.class);
        when(serviceStatus.getUrl()).thenReturn("url");
        when(service.getStatus()).thenReturn(serviceStatus);
        try(MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)){
            action.actionPerformed(anActionEvent);
            browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(1));
        }
    }
}
