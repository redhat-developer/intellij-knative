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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import javax.swing.JViewport;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TreeHelperTest {
    private CodeInsightTestFixture myFixture;
    private Project project;
    private ToolWindowManager toolWindowManager;
    private ToolWindow toolWindow;
    private ContentManager contentManager;
    private Content content;
    private SimpleToolWindowPanel simpleToolWindowPanel;
    private JBScrollPane jbScrollPane;
    private JViewport jViewport;
    private Tree tree;
    private KnTreeStructure knTreeStructure;
    private ParentableNode parentableNode;
    private MockedStatic<ToolWindowManager> toolWindowManagerMockedStatic;

    @Before
    public void setUp() throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder();
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture);
        myFixture.setUp();
        project = myFixture.getProject();
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
        knTreeStructure = mock(KnTreeStructure.class);
        parentableNode = mock(ParentableNode.class);
    }

    @After
    public void tearDown() throws Exception {
        toolWindowManagerMockedStatic.close();
        myFixture.tearDown();
    }

    @Test
    public void TrimErrorMessage_ErrorMessageLongerThan130_ErrorMessageTrimmed() {
        String longErrorMessage = StringUtils.repeat("a", 140);
        String resultingErrorMessage = longErrorMessage.substring(0, 130) + "...";
        assertEquals(resultingErrorMessage, TreeHelper.trimErrorMessage(longErrorMessage));
    }

    @Test
    public void TrimErrorMessage_ErrorMessageShorterThan130_ErrorMessage() {
        String errorMessage = "aaaa";
        assertEquals(errorMessage, TreeHelper.trimErrorMessage(errorMessage));
    }

    @Test
    public void GetTree_ProjectIsNull_Null() {
        assertNull(TreeHelper.getTree(null));
    }

    @Test
    public void GetTree_ProjectWithoutWindow_Null() {
        assertNull(TreeHelper.getTree(project));
    }

    @Test
    public void GetTree_ProjectWithNoContent_Null() {
        when(toolWindowManager.getToolWindow("Knative")).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(null);
        assertNull(TreeHelper.getTree(project));
        verify(toolWindowManager).getToolWindow(any());
        verify(toolWindow).getContentManager();
        verify(contentManager).getContent(0);
    }

    @Test
    public void GetTree_ProjectWithoutToolPanel_Null() {
        when(toolWindowManager.getToolWindow("Knative")).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(content);
        when(content.getComponent()).thenReturn(null);
        assertNull(TreeHelper.getTree(project));
        verify(toolWindowManager).getToolWindow(any());
        verify(toolWindow).getContentManager();
        verify(contentManager).getContent(0);
        verify(content).getComponent();
    }

    @Test
    public void GetTree_ProjectWithoutScrollPanel_Null() {
        when(toolWindowManager.getToolWindow("Knative")).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(content);
        when(content.getComponent()).thenReturn(simpleToolWindowPanel);
        when(simpleToolWindowPanel.getContent()).thenReturn(null);
        assertNull(TreeHelper.getTree(project));
        verify(toolWindowManager).getToolWindow(any());
        verify(toolWindow).getContentManager();
        verify(contentManager).getContent(0);
        verify(content).getComponent();
        verify(simpleToolWindowPanel).getContent();
    }

    @Test
    public void GetTree_ProjectWithoutViewPort_Null() {
        when(toolWindowManager.getToolWindow("Knative")).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(content);
        when(content.getComponent()).thenReturn(simpleToolWindowPanel);
        when(simpleToolWindowPanel.getContent()).thenReturn(jbScrollPane);
        when(jbScrollPane.getViewport()).thenReturn(null);
        assertNull(TreeHelper.getTree(project));
        verify(toolWindowManager).getToolWindow(any());
        verify(toolWindow).getContentManager();
        verify(contentManager).getContent(0);
        verify(content).getComponent();
        verify(simpleToolWindowPanel).getContent();
        verify(jbScrollPane).getViewport();
    }

    @Test
    public void GetTree_ProjectWithoutView_Null() {
        when(toolWindowManager.getToolWindow("Knative")).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(content);
        when(content.getComponent()).thenReturn(simpleToolWindowPanel);
        when(simpleToolWindowPanel.getContent()).thenReturn(jbScrollPane);
        when(jbScrollPane.getViewport()).thenReturn(jViewport);
        when(jViewport.getView()).thenReturn(null);
        assertNull(TreeHelper.getTree(project));
        verify(toolWindowManager).getToolWindow(any());
        verify(toolWindow).getContentManager();
        verify(contentManager).getContent(0);
        verify(content).getComponent();
        verify(simpleToolWindowPanel).getContent();
        verify(jbScrollPane).getViewport();
        verify(jViewport).getView();
    }

    @Test
    public void GetTree_Project_Tree() {
        GetTree();
        Tree resultingTree = TreeHelper.getTree(project);
        assertNotNull(resultingTree);
        assertEquals(tree, resultingTree);
    }

    @Test
    public void GetKnTreeStructure_ProjectIsNull_Null() {
        assertNull(TreeHelper.getKnTreeStructure(null));
    }

    @Test
    public void GetKnTreeStructure_ProjectWithoutClientProperty_Null() {
        GetTree();
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(null);
        assertNull(TreeHelper.getKnTreeStructure(project));
    }

    @Test
    public void GetKnTreeStructure_Project_KnTreeStructure() {
        GetKnTreeStructure();
        KnTreeStructure resultingStructure = TreeHelper.getKnTreeStructure(project);
        assertNotNull(resultingStructure);
        assertEquals(knTreeStructure, resultingStructure);
    }

    @Test
    public void GetKn_ProjectNotValid_Null() {
        assertNull(TreeHelper.getKn(null));
    }

    @Test
    public void GetKn_Project_Kn() {
        Kn kn = mock(Kn.class);
        KnRootNode knRootNode = mock(KnRootNode.class);
        when(knTreeStructure.getRootElement()).thenReturn(knRootNode);
        when(knRootNode.getKn()).thenReturn(kn);
        GetKnTreeStructure();
        Kn resultingKn = TreeHelper.getKn(project);
        assertNotNull(resultingKn);
        assertEquals(kn, resultingKn);
    }

    @Test
    public void Refresh_ProjectIsNull_Nothing() {
        TreeHelper.refresh(null, parentableNode);
        verify(knTreeStructure, never()).fireModified(any());
    }

    @Test
    public void Refresh_NodeIsNull_Nothing() {
        GetKnTreeStructure();
        TreeHelper.refresh(project, null);
        verify(knTreeStructure, never()).fireModified(any());
    }

    @Test
    public void Refresh_ProjectWithoutTreeStructure_Nothing() {
        GetTree();
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(null);
        TreeHelper.refresh(project, parentableNode);
        verify(knTreeStructure, never()).fireModified(any());
    }

    @Test
    public void Refresh_ProjectAndNodeAreValid_Refresh() {
        GetKnTreeStructure();
        TreeHelper.refresh(project, parentableNode);
        verify(knTreeStructure).fireModified(any());
    }

    private void GetTree() {
        when(toolWindowManager.getToolWindow("Knative")).thenReturn(toolWindow);
        when(toolWindow.getContentManager()).thenReturn(contentManager);
        when(contentManager.getContent(0)).thenReturn(content);
        when(content.getComponent()).thenReturn(simpleToolWindowPanel);
        when(simpleToolWindowPanel.getContent()).thenReturn(jbScrollPane);
        when(jbScrollPane.getViewport()).thenReturn(jViewport);
        when(jViewport.getView()).thenReturn(tree);
    }

    private void GetKnTreeStructure() {
        GetTree();
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(knTreeStructure);
    }
}
