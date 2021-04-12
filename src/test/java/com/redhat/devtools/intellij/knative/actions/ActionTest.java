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
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.knative.FixtureBaseTest;
import com.redhat.devtools.intellij.knative.kn.Service;
import com.redhat.devtools.intellij.knative.kn.ServiceStatus;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.junit.Before;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ActionTest extends FixtureBaseTest {

    protected Tree tree;
    protected TreeSelectionModel model;
    protected TreePath path, path1;
    protected Service service;
    protected ServiceStatus serviceStatus;
    protected Presentation presentation;
    protected TreeAction treeAction;


    @Before
    public void setUp() throws Exception {
        super.setUp();

        tree = mock(Tree.class);
        model = mock(TreeSelectionModel.class);
        path = mock(TreePath.class);
        path1 = mock(TreePath.class);
        service = mock(Service.class);
        serviceStatus = mock(ServiceStatus.class);
        treeAction = mock(TreeAction.class);
        presentation = new Presentation();

        when(path.getLastPathComponent()).thenReturn(knServiceNode);
        when(path1.getLastPathComponent()).thenReturn(knRevisionNode);
        when(model.getSelectionPath()).thenReturn(path);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path});
        when(tree.getSelectionModel()).thenReturn(model);
        when(serviceStatus.getUrl()).thenReturn("url");
    }

}
