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
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.knative.kn.ServiceStatus;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.tree.TreePath;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class OpenInBrowserActionTest extends ActionTest {

    @Test
    public void IsVisible_SelectedIsKnServiceNodeWithNoURL_False() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        when(service.getStatus()).thenReturn(null);
        when(knServiceNode.getService(false)).thenReturn(service);
        boolean result = action.isVisible(knServiceNode);
        assertFalse(result);
    }

    @Test
    public void IsVisible_SelectedIsKnServiceNodeWithValidURL_True() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        ServiceStatus serviceStatus = mock(ServiceStatus.class);
        when(serviceStatus.getUrl()).thenReturn("url");
        when(service.getStatus()).thenReturn(serviceStatus);
        when(knServiceNode.getService(false)).thenReturn(service);
        boolean result = action.isVisible(knServiceNode);
        assertTrue(result);
    }

    @Test
    public void IsVisible_SelectedIsRevisionOfNotRunningService_False() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        when(service.getStatus()).thenReturn(null);
        when(knServiceNode.getService(false)).thenReturn(service);
        when(knRevisionNode.getParent()).thenReturn(knServiceNode);
        boolean result = action.isVisible(knRevisionNode);
        assertFalse(result);
    }

    @Test
    public void IsVisible_SelectedIsRevisionOfRunningService_True() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        ServiceStatus serviceStatus = mock(ServiceStatus.class);
        when(serviceStatus.getUrl()).thenReturn("url");
        when(service.getStatus()).thenReturn(serviceStatus);
        when(knServiceNode.getService(false)).thenReturn(service);
        when(knRevisionNode.getParent()).thenReturn(knServiceNode);
        boolean result = action.isVisible(knRevisionNode);
        assertTrue(result);
    }

    @Test
    public void IsVisible_SelectedIsNotAKnServiceNodeOrKnRevisionNode_False() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        boolean result = action.isVisible(knEventingNode);
        assertFalse(result);
    }

    @Test
    public void ActionPerformed_SelectedIsKnServiceNodeWithoutUrl_BrowseNotCalled() {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = createOpenInBrowserActionEventForService();
        when(service.getStatus()).thenReturn(null);
        try(MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)){
            action.actionPerformed(anActionEvent);
            browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(0));
        }
    }

    @Test
    public void ActionPerformed_SelectedIsKnServiceNodeWithUrl_BrowseCalled() {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = createOpenInBrowserActionEventForService();
        when(service.getStatus()).thenReturn(serviceStatus);
        try(MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)){
            action.actionPerformed(anActionEvent);
            browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(1));
        }
    }

    @Test
    public void ActionPerformed_SelectedIsKnRevisionNodeWithoutUrl_InputDialogIsCalled() {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = createOpenInBrowserActionEventForRevision();
        when(serviceTraffic.getUrl()).thenReturn("");
        try(MockedStatic<Messages> messagesMockedStatic = mockStatic(Messages.class)) {
            messagesMockedStatic.when(() -> Messages.showInputDialog(anyString(), anyString(), any())).thenReturn("");
            try (MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)) {
                action.actionPerformed(anActionEvent);
                browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(0));
            }
            messagesMockedStatic.verify(() -> Messages.showInputDialog(anyString(), anyString(), any()), times(1));
        }
    }

    @Test
    public void ActionPerformed_SelectedIsKnRevisionNodeWithoutTagAndUrlAndInputDialogReturnsTag_RevisionTaggedAndBrowseCalled() throws IOException {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = createOpenInBrowserActionEventForRevision();
        when(serviceTraffic.getUrl()).thenReturn("").thenReturn("url");
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            try (MockedStatic<Messages> messagesMockedStatic = mockStatic(Messages.class)) {
                messagesMockedStatic.when(() -> Messages.showInputDialog(anyString(), anyString(), any())).thenReturn("tag");
                try (MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)) {
                    action.actionPerformed(anActionEvent);
                    verify(kn, times(1)).tagRevision(anyString(), anyString(), anyString());
                    browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(1));
                }
                messagesMockedStatic.verify(() -> Messages.showInputDialog(anyString(), anyString(), any()), times(1));
            }
        }
    }

    @Test
    public void ActionPerformed_SelectedIsKnRevisionNodeWithTagAndUrl_DialogNotCalledAndBrowseCalled() {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = createOpenInBrowserActionEventForRevision();
        when(serviceTraffic.getUrl()).thenReturn("url");
        try (MockedStatic<Messages> messagesMockedStatic = mockStatic(Messages.class)) {
            try (MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)) {
                action.actionPerformed(anActionEvent);
                browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(1));
            }
            messagesMockedStatic.verify(() -> Messages.showInputDialog(anyString(), anyString(), any()), times(0));
        }
    }

    private AnActionEvent createOpenInBrowserActionEventForRevision() {
        AnActionEvent anActionEvent = createOpenInBrowserActionEvent();
        when(anActionEvent.getProject()).thenReturn(project);
        when(knServiceNode.getService(anyBoolean())).thenReturn(service);
        when(knServiceNode.getName()).thenReturn("service");
        when(knRevisionNode.getParent()).thenReturn(knServiceNode);
        when(service.getStatus()).thenReturn(serviceStatus);
        when(model.getSelectionPath()).thenReturn(path1);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path1});
        when(knRevisionNode.getName()).thenReturn("rev");
        when(serviceTraffic.getRevisionName()).thenReturn("rev");
        when(serviceTraffic.getLatestRevision()).thenReturn(true);
        when(serviceStatus.getTraffic()).thenReturn(Arrays.asList(serviceTraffic));
        return anActionEvent;
    }

    private AnActionEvent createOpenInBrowserActionEventForService() {
        when(knServiceNode.getService(false)).thenReturn(service);
        return createOpenInBrowserActionEvent();
    }

    private AnActionEvent createOpenInBrowserActionEvent() {
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        return anActionEvent;
    }
}
