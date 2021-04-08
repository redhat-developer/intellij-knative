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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.actions.TreeAction;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.Service;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import com.redhat.devtools.intellij.knative.ui.DeleteDialog;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.IOException;
import java.util.List;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteActionTest {
    private Presentation presentation;
    private Tree tree;
    private KnTreeStructure knTreeStructure;
    private TreeAction treeAction;
    private TreePath path, path1;
    private KnServiceNode knServiceNode, knServiceNode1;
    private KnRevisionNode knRevisionNode;
    private Service service;
    private CodeInsightTestFixture myFixture;
    private Kn kn;
    private TreeSelectionModel model;

    @Before
    public void setUp() throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder();
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture);
        myFixture.setUp();
        tree = mock(Tree.class);
        path = mock(TreePath.class);
        path1 = mock(TreePath.class);
        knServiceNode = mock(KnServiceNode.class);
        knServiceNode1 = mock(KnServiceNode.class);
        kn = mock(Kn.class);
        knRevisionNode = mock(KnRevisionNode.class);
        service = mock(Service.class);
        model = mock(TreeSelectionModel.class);
        when(tree.getSelectionModel()).thenReturn(model);
        when(path.getLastPathComponent()).thenReturn(knServiceNode);
        when(path1.getLastPathComponent()).thenReturn(knRevisionNode);
        when(model.getSelectionPath()).thenReturn(path);

        presentation = new Presentation();

        knTreeStructure = mock(KnTreeStructure.class);
        treeAction = mock(TreeAction.class);
        when(knServiceNode.getName()).thenReturn("service");
        when(knRevisionNode.getName()).thenReturn("revision");
    }

    @After
    public void tearDown() throws Exception {
        myFixture.tearDown();
    }

    @Test
    public void ActionPerformed_OneKnServiceNodeSelectedButDeleteCancelled_Nothing() throws IOException {
        AnAction action = new DeleteAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path});
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            try (MockedConstruction<DeleteDialog> ignored = mockConstruction(DeleteDialog.class,
                    (mock, context) -> {
                        when(mock.isOK()).thenReturn(false);
                    })) {
                action.actionPerformed(anActionEvent);
                verify(kn, never()).deleteServices(any());
                verify(kn, never()).deleteRevisions(any());
            }
        }
    }

    @Test
    public void ActionPerformed_OneKnServiceNodeSelected_DeleteOneService() throws IOException {
        AnAction action = new DeleteAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path});
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            try (MockedConstruction<DeleteDialog> ignored = mockConstruction(DeleteDialog.class,
                    (mock, context) -> {
                        when(mock.isOK()).thenReturn(true);
                    })) {
                action.actionPerformed(anActionEvent);
                verify(kn).deleteServices(any());
                verify(kn, never()).deleteRevisions(any());
            }
        }
    }

    @Test
    public void ActionPerformed_OneKnRevisionNodeSelected_DeleteOneRevision() throws IOException {
        AnAction action = new DeleteAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path1});
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            try (MockedConstruction<DeleteDialog> ignored = mockConstruction(DeleteDialog.class,
                    (mock, context) -> {
                        when(mock.isOK()).thenReturn(true);
                    })) {
                action.actionPerformed(anActionEvent);
                verify(kn, never()).deleteServices(any());
                verify(kn).deleteRevisions(any());
            }
        }
    }

    @Test
    public void ActionPerformed_TwoDifferentKnNodeSelected_DeleteCalledTwice() throws IOException {
        AnAction action = new DeleteAction();
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path, path1});
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            try (MockedConstruction<DeleteDialog> ignored = mockConstruction(DeleteDialog.class,
                    (mock, context) -> {
                        when(mock.isOK()).thenReturn(true);
                    })) {
                action.actionPerformed(anActionEvent);
                verify(kn, times(1)).deleteServices(any());
                verify(kn, times(1)).deleteRevisions(any());
            }
        }
    }
}
