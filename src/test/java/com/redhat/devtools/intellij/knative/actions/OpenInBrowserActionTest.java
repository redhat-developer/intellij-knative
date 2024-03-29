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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.kn.ServiceStatus;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.swing.tree.TreePath;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;


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

    public void testIsVisible_SelectedIsKnServiceNodeWithNoURL_False() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        when(service.getStatus()).thenReturn(null);
        when(knServiceNode.getService(false)).thenReturn(service);
        boolean result = action.isVisible(knServiceNode);
        assertFalse(result);
    }

    public void testIsVisible_SelectedIsKnServiceNodeWithValidURL_True() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        ServiceStatus serviceStatus = mock(ServiceStatus.class);
        when(serviceStatus.getUrl()).thenReturn("url");
        when(service.getStatus()).thenReturn(serviceStatus);
        when(knServiceNode.getService(false)).thenReturn(service);
        boolean result = action.isVisible(knServiceNode);
        assertTrue(result);
    }

    public void testIsVisible_SelectedIsRevisionOfNotRunningService_False() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        when(service.getStatus()).thenReturn(null);
        when(knServiceNode.getService(false)).thenReturn(service);
        when(knRevisionNode.getParent()).thenReturn(knServiceNode);
        boolean result = action.isVisible(knRevisionNode);
        assertFalse(result);
    }

    public void testIsVisible_SelectedIsRevisionOfRunningService_True() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        ServiceStatus serviceStatus = mock(ServiceStatus.class);
        when(serviceStatus.getUrl()).thenReturn("url");
        when(service.getStatus()).thenReturn(serviceStatus);
        when(knServiceNode.getService(false)).thenReturn(service);
        when(knRevisionNode.getParent()).thenReturn(knServiceNode);
        boolean result = action.isVisible(knRevisionNode);
        assertTrue(result);
    }

    public void testIsVisible_SelectedIsNotAKnServiceNodeOrKnRevisionNode_False() {
        OpenInBrowserAction action = new OpenInBrowserAction();
        boolean result = action.isVisible(knEventingNode);
        assertFalse(result);
    }

    public void testActionPerformed_SelectedIsKnServiceNodeWithoutUrl_BrowseNotCalled() {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = createOpenInBrowserActionEventForService();
        when(service.getStatus()).thenReturn(null);
        try(MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.submit(any(Runnable.class))).then((Answer) invocation -> {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            });
            try (MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)) {
                try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
                    when(kn.getNamespace()).thenReturn("namespace");
                    treeHelperMockedStatic.when(() -> TreeHelper.getKn(any(Project.class))).thenReturn(kn);
                    action.actionPerformed(anActionEvent);
                    browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(0));
                }
            }
        }
    }

    public void testActionPerformed_SelectedIsKnServiceNodeWithUrl_BrowseCalled() {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = createOpenInBrowserActionEventForService();
        when(service.getStatus()).thenReturn(serviceStatus);
        try(MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.submit(any(Runnable.class))).then((Answer) invocation -> {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            });
            try (MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)) {
                try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
                    when(kn.getNamespace()).thenReturn("namespace");
                    treeHelperMockedStatic.when(() -> TreeHelper.getKn(any(Project.class))).thenReturn(kn);
                    action.actionPerformed(anActionEvent);
                    verify(serviceStatus, times(1)).getUrl();
                    browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(1));
                }
            }
        }
    }

    public void testActionPerformed_SelectedIsKnRevisionNodeWithoutUrl_InputDialogIsCalled() {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = createOpenInBrowserActionEventForRevision();
        when(serviceTraffic.getUrl()).thenReturn("");
        Messages.InputDialog inputDialog = mock(Messages.InputDialog.class);
        when(inputDialog.isOK()).thenReturn(false);
        try(MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.submit(any(Runnable.class))).then((Answer) invocation -> {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            });
            try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
                uiHelperMockedStatic.when(() -> UIHelper.executeInUI(any(Supplier.class))).thenReturn(inputDialog);
                try (MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)) {
                    try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
                        when(kn.getNamespace()).thenReturn("namespace");
                        treeHelperMockedStatic.when(() -> TreeHelper.getKn(any(Project.class))).thenReturn(kn);
                        action.actionPerformed(anActionEvent);
                        browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(0));
                        uiHelperMockedStatic.verify(() -> UIHelper.executeInUI(any(Supplier.class)), times(1));
                    }
                }
            }
        }
    }

    public void testActionPerformed_SelectedIsKnRevisionNodeWithoutTagAndUrlAndInputDialogReturnsTag_RevisionTaggedAndBrowseCalled() throws IOException {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = createOpenInBrowserActionEventForRevision();
        when(serviceTraffic.getUrl()).thenReturn("").thenReturn("url");
        Messages.InputDialog inputDialog = mock(Messages.InputDialog.class);
        when(inputDialog.isOK()).thenReturn(true);
        when(inputDialog.getInputString()).thenReturn("tag");
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            try(MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
                execHelperMockedStatic.when(() -> ExecHelper.submit(any(Runnable.class))).then((Answer) invocation -> {
                    ((Runnable) invocation.getArguments()[0]).run();
                    return null;
                });
                try (MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
                    uiHelperMockedStatic.when(() -> UIHelper.executeInUI(any(Supplier.class))).thenReturn(inputDialog);
                    try (MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)) {
                        action.actionPerformed(anActionEvent);
                        verify(kn, times(1)).tagRevision(anyString(), anyString(), anyString());
                        browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(1));
                    }
                }
            }
        }
    }

    public void testActionPerformed_SelectedIsKnRevisionNodeWithTagAndUrl_DialogNotCalledAndBrowseCalled() {
        AnAction action = new OpenInBrowserAction();
        AnActionEvent anActionEvent = createOpenInBrowserActionEventForRevision();
        when(serviceTraffic.getUrl()).thenReturn("url");
        try (MockedStatic<Messages> messagesMockedStatic = mockStatic(Messages.class)) {
            try(MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
                try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
                    when(kn.getNamespace()).thenReturn("namespace");
                    treeHelperMockedStatic.when(() -> TreeHelper.getKn(any(Project.class))).thenReturn(kn);
                    execHelperMockedStatic.when(() -> ExecHelper.submit(any(Runnable.class))).then((Answer) invocation -> {
                        ((Runnable) invocation.getArguments()[0]).run();
                        return null;
                    });
                    try (MockedStatic<BrowserUtil> browserUtilMockedStatic = mockStatic(BrowserUtil.class)) {
                        action.actionPerformed(anActionEvent);
                        browserUtilMockedStatic.verify(() -> BrowserUtil.browse(anyString()), times(1));
                    }
                    messagesMockedStatic.verify(() -> Messages.showInputDialog(anyString(), anyString(), any()), times(0));
                }
            }

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
        when(anActionEvent.getProject()).thenReturn(project);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(knTreeStructure);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        return anActionEvent;
    }
}
