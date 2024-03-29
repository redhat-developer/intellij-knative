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

import com.intellij.ide.util.treeView.smartTree.TreeAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.knative.BaseTest;
import com.redhat.devtools.intellij.knative.func.FuncActionPipelineManager;
import com.redhat.devtools.intellij.knative.kn.Service;
import com.redhat.devtools.intellij.knative.kn.ServiceStatus;
import com.redhat.devtools.intellij.knative.kn.ServiceTraffic;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.buildFuncWindowTab.BuildFuncPanel;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.junit.Before;
import org.mockito.MockedStatic;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ActionTest extends BaseTest {

    protected Tree tree;
    protected TreeSelectionModel model;
    protected TreePath path, path1, path2, path3;
    protected Service service;
    protected ServiceStatus serviceStatus;
    protected ServiceTraffic serviceTraffic;
    protected Presentation presentation;
    protected TreeAction treeAction;
    protected TelemetryMessageBuilder.ActionMessage telemetry;
    protected FuncActionPipelineManager manager;


    public void setUp() throws Exception {
        super.setUp();

        tree = mock(Tree.class);
        model = mock(TreeSelectionModel.class);
        path = mock(TreePath.class);
        path1 = mock(TreePath.class);
        path2 = mock(TreePath.class);
        path3 = mock(TreePath.class);
        service = mock(Service.class);
        serviceStatus = mock(ServiceStatus.class);
        serviceTraffic = mock(ServiceTraffic.class);
        treeAction = mock(TreeAction.class);
        telemetry = mock(TelemetryMessageBuilder.ActionMessage.class);
        presentation = new Presentation();
        manager = mock(FuncActionPipelineManager.class);

        when(path.getLastPathComponent()).thenReturn(knServiceNode);
        when(path1.getLastPathComponent()).thenReturn(knRevisionNode);
        when(path2.getLastPathComponent()).thenReturn(knFunctionNode);
        when(path3.getLastPathComponent()).thenReturn(knFunctionNode);
        when(model.getSelectionPath()).thenReturn(path);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path});
        when(tree.getSelectionModel()).thenReturn(model);
        when(serviceStatus.getUrl()).thenReturn("url");

        when(kn.getFuncActionPipelineManager()).thenReturn(manager);
    }

    protected void mockToolWindow(MockedStatic<ToolWindowManager> toolWindowManagerMockedStatic) {
        ToolWindowManager toolWindowManager = mock(ToolWindowManager.class);
        toolWindowManagerMockedStatic.when(() -> ToolWindowManager.getInstance(any())).thenReturn(toolWindowManager);

        ToolWindow toolWindow = mock(ToolWindow.class);
        ContentManager contentManager = mock(ContentManager.class);
        BuildFuncPanel buildFuncPanel = mock(BuildFuncPanel.class);
        when(toolWindowManager.getToolWindow(anyString())).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.findContent(anyString())).thenReturn(buildFuncPanel);
    }

    protected void mockTelemetry(MockedStatic<TelemetryService> telemetryServiceMockedStatic) {
        telemetryServiceMockedStatic.when(TelemetryService::instance).thenReturn(telemetryMessageBuilder);
        when(telemetryMessageBuilder.action(anyString())).thenReturn(actionMessage);
        when(actionMessage.result(anyString())).thenReturn(actionMessage);
        when(actionMessage.send()).thenReturn(null);
    }
}
