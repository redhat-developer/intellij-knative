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
import com.redhat.devtools.intellij.knative.kn.ServiceStatus;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
        AnActionEvent anActionEvent = createOpenInBrowserActionEvent();
        when(service.getStatus()).thenReturn(null);
        try(MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)){
            action.actionPerformed(anActionEvent);
            browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(0));
        }
    }

    @Test
    public void ActionPerformed_SelectedIsKnServiceNodeWithUrl_BrowseCalled() {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = createOpenInBrowserActionEvent();
        when(service.getStatus()).thenReturn(serviceStatus);
        try(MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)){
            action.actionPerformed(anActionEvent);
            browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(1));
        }
    }

    private AnActionEvent createOpenInBrowserActionEvent() {
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(knServiceNode.getService(false)).thenReturn(service);
        return anActionEvent;
    }
}
