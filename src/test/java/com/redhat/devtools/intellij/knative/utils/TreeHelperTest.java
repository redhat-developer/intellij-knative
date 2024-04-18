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
package com.redhat.devtools.intellij.knative.utils;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.knative.BaseTest;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import org.mockito.MockedStatic;

import javax.swing.*;

import static com.redhat.devtools.intellij.knative.Constants.KNATIVE_FUNC_TOOL_WINDOW_ID;
import static com.redhat.devtools.intellij.knative.Constants.KNATIVE_TOOL_WINDOW_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TreeHelperTest extends BaseTest {
    private ToolWindowManager toolWindowManager;
    private ToolWindow toolWindow;
    private ContentManager contentManager;
    private Content content;
    private SimpleToolWindowPanel simpleToolWindowPanel;
    private JBScrollPane jbScrollPane;
    private JViewport jViewport;
    private Tree tree;
    private MockedStatic<ToolWindowManager> toolWindowManagerMockedStatic;

    public void setUp() throws Exception {
        super.setUp();
        toolWindowManager = mock(ToolWindowManager.class);
        toolWindowManagerMockedStatic = mockStatic(ToolWindowManager.class);
        toolWindowManagerMockedStatic.when(() -> ToolWindowManager.getInstance(any())).thenReturn(toolWindowManager);
        toolWindow = mock(ToolWindow.class);
        contentManager = mock(ContentManager.class);
        content = mock(Content.class);
        simpleToolWindowPanel = mock(SimpleToolWindowPanel.class);
        jbScrollPane = mock(JBScrollPane.class);
        jViewport = mock(JViewport.class);
        tree = mock(Tree.class);
    }

    public void tearDown() throws Exception {
        toolWindowManagerMockedStatic.close();
        super.tearDown();
    }

    public void testTrimErrorMessage_ErrorMessageLongerThan130_ErrorMessageTrimmed() {
        String longErrorMessage = StringUtil.repeat("a", 140);
        String resultingErrorMessage = longErrorMessage.substring(0, 130) + "...";
        assertEquals(resultingErrorMessage, TreeHelper.trimErrorMessage(longErrorMessage));
    }

    public void testTrimErrorMessage_ErrorMessageShorterThan130_ErrorMessage() {
        String errorMessage = "aaaa";
        assertEquals(errorMessage, TreeHelper.trimErrorMessage(errorMessage));
    }

    public void testGetTree_ProjectIsNull_Null() {
        assertNull(TreeHelper.getTree(null, KNATIVE_TOOL_WINDOW_ID));
    }

    public void testGetTree_ProjectWithoutWindow_Null() {
        assertNull(TreeHelper.getTree(project, KNATIVE_TOOL_WINDOW_ID));
    }

    public void testGetTree_ProjectWithNoContent_Null() {
        when(toolWindowManager.getToolWindow("Knative")).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(null);
        assertNull(TreeHelper.getTree(project, KNATIVE_TOOL_WINDOW_ID));
        verify(toolWindowManager).getToolWindow(any());
        verify(toolWindow).getContentManager();
        verify(contentManager).getContent(0);
    }

    public void testGetTree_ProjectWithoutToolPanel_Null() {
        when(toolWindowManager.getToolWindow("Knative")).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(content);
        when(content.getComponent()).thenReturn(null);
        assertNull(TreeHelper.getTree(project, KNATIVE_TOOL_WINDOW_ID));
        verify(toolWindowManager).getToolWindow(any());
        verify(toolWindow).getContentManager();
        verify(contentManager).getContent(0);
        verify(content).getComponent();
    }

    public void testGetTree_ProjectWithoutScrollPanel_Null() {
        when(toolWindowManager.getToolWindow("Knative")).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(content);
        when(content.getComponent()).thenReturn(simpleToolWindowPanel);
        when(simpleToolWindowPanel.getContent()).thenReturn(null);
        assertNull(TreeHelper.getTree(project, KNATIVE_TOOL_WINDOW_ID));
        verify(toolWindowManager).getToolWindow(any());
        verify(toolWindow).getContentManager();
        verify(contentManager).getContent(0);
        verify(content).getComponent();
        verify(simpleToolWindowPanel).getContent();
    }

    public void testGetTree_ProjectWithoutViewPort_Null() {
        when(toolWindowManager.getToolWindow("Knative")).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(content);
        when(content.getComponent()).thenReturn(simpleToolWindowPanel);
        when(simpleToolWindowPanel.getContent()).thenReturn(jbScrollPane);
        when(jbScrollPane.getViewport()).thenReturn(null);
        assertNull(TreeHelper.getTree(project, KNATIVE_TOOL_WINDOW_ID));
        verify(toolWindowManager).getToolWindow(any());
        verify(toolWindow).getContentManager();
        verify(contentManager).getContent(0);
        verify(content).getComponent();
        verify(simpleToolWindowPanel).getContent();
        verify(jbScrollPane).getViewport();
    }

    public void testGetTree_ProjectWithoutView_Null() {
        when(toolWindowManager.getToolWindow("Knative")).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(content);
        when(content.getComponent()).thenReturn(simpleToolWindowPanel);
        when(simpleToolWindowPanel.getContent()).thenReturn(jbScrollPane);
        when(jbScrollPane.getViewport()).thenReturn(jViewport);
        when(jViewport.getView()).thenReturn(null);
        assertNull(TreeHelper.getTree(project, KNATIVE_TOOL_WINDOW_ID));
        verify(toolWindowManager).getToolWindow(any());
        verify(toolWindow).getContentManager();
        verify(contentManager).getContent(0);
        verify(content).getComponent();
        verify(simpleToolWindowPanel).getContent();
        verify(jbScrollPane).getViewport();
        verify(jViewport).getView();
    }

    public void testGetTree_Project_Tree() {
        getTree(KNATIVE_TOOL_WINDOW_ID);
        Tree resultingTree = TreeHelper.getTree(project, KNATIVE_TOOL_WINDOW_ID);
        assertNotNull(resultingTree);
        assertEquals(tree, resultingTree);
    }

    public void testGetKnTreeStructure_ProjectIsNull_Null() {
        assertNull(TreeHelper.getKnTreeStructure(null));
    }

    public void testGetKnTreeStructure_ProjectWithoutClientProperty_Null() {
        getTree(KNATIVE_TOOL_WINDOW_ID);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(null);
        assertNull(TreeHelper.getKnTreeStructure(project));
    }

    public void testGetKnTreeStructure_Project_KnTreeStructure() {
        getKnTreeStructure();
        KnTreeStructure resultingStructure = TreeHelper.getKnTreeStructure(project);
        assertNotNull(resultingStructure);
        assertEquals(knTreeStructure, resultingStructure);
    }

    public void testGetKnFunctionsTreeStructure_ProjectIsNull_Null() {
        assertNull(TreeHelper.getKnFunctionsTreeStructure(null));
    }

    public void testGetKnFunctionsTreeStructure_ProjectWithoutClientProperty_Null() {
        getTree(KNATIVE_FUNC_TOOL_WINDOW_ID);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(null);
        assertNull(TreeHelper.getKnFunctionsTreeStructure(project));
    }

    public void testGetKnFunctionsTreeStructure_Project_KnTreeStructure() {
        getKnFunctionsTreeStructure();
        KnTreeStructure resultingStructure = TreeHelper.getKnFunctionsTreeStructure(project);
        assertNotNull(resultingStructure);
        assertEquals(knFunctionsTreeStructure, resultingStructure);
    }

    public void testGetKn_ProjectNotValid_Null() {
        assertNull(TreeHelper.getKn(null));
    }

    public void testGetKn_Project_Kn() {
        when(knTreeStructure.getRootElement()).thenReturn(knRootNode);
        when(knRootNode.getKn()).thenReturn(kn);
        getKnTreeStructure();
        Kn resultingKn = TreeHelper.getKn(project);
        assertNotNull(resultingKn);
        assertEquals(kn, resultingKn);
    }

    public void testRefresh_ProjectIsNull_Nothing() {
        TreeHelper.refresh(null, parentableNode);
        verify(knTreeStructure, never()).fireModified(any());
    }

    public void testRefresh_NodeIsNull_Nothing() {
        getKnTreeStructure();
        TreeHelper.refresh(project, null);
        verify(knTreeStructure, never()).fireModified(any());
    }

    public void testRefresh_ProjectWithoutTreeStructure_Nothing() {
        getTree(KNATIVE_TOOL_WINDOW_ID);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(null);
        TreeHelper.refresh(project, parentableNode);
        verify(knTreeStructure, never()).fireModified(any());
    }

    public void testRefresh_ProjectAndNodeAreValid_Refresh() {
        getKnTreeStructure();
        TreeHelper.refresh(project, parentableNode);
        verify(knTreeStructure).fireModified(any());
    }

    private void getTree(String idToolWindow) {
        when(toolWindowManager.getToolWindow(idToolWindow)).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(content);
        when(content.getComponent()).thenReturn(simpleToolWindowPanel);
        when(simpleToolWindowPanel.getContent()).thenReturn(jbScrollPane);
        when(jbScrollPane.getViewport()).thenReturn(jViewport);
        when(jViewport.getView()).thenReturn(tree);
    }

    private void getKnTreeStructure() {
        getTree(KNATIVE_TOOL_WINDOW_ID);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(knTreeStructure);
    }

    private void getKnFunctionsTreeStructure() {
        getTree(KNATIVE_FUNC_TOOL_WINDOW_ID);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(knFunctionsTreeStructure);
    }
}
