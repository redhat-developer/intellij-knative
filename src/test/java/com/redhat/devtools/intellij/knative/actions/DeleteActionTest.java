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

import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DeleteActionTest extends ActionTest {

    private static final String SERVICE = "service";
    private static final String REVISION = "revision";

    public void testExecuteDelete_OneKnServiceNodeSelected_DeleteOneService() throws IOException {
        Map<String, Integer> typePerTimesCalled = new HashMap<>();
        typePerTimesCalled.put(SERVICE, 1);
        executeDeleteAction(new ParentableNode[] {knServiceNode}, typePerTimesCalled);
    }

    public void testExecuteDelete_OneKnRevisionNodeSelected_DeleteOneRevision() throws IOException {
        Map<String, Integer> typePerTimesCalled = new HashMap<>();
        typePerTimesCalled.put(REVISION, 1);
        executeDeleteAction(new ParentableNode[] {knRevisionNode}, typePerTimesCalled);
    }

    public void testExecuteDelete_TwoDifferentKnNodeSelected_CalledTwoDelete() throws IOException {
        Map<String, Integer> typePerTimesCalled = new HashMap<>();
        typePerTimesCalled.put(SERVICE, 1);
        typePerTimesCalled.put(REVISION, 1);
        executeDeleteAction(new ParentableNode[] {knServiceNode, knRevisionNode}, typePerTimesCalled);
    }

    public void testExecuteDelete_ThreeDifferentKnNodeSelected_CalledThreeDelete() throws IOException {
        Map<String, Integer> typePerTimesCalled = new HashMap<>();
        typePerTimesCalled.put(SERVICE, 1);
        typePerTimesCalled.put(REVISION, 1);
        executeDeleteAction(new ParentableNode[] {knServiceNode, knRevisionNode}, typePerTimesCalled);
    }

    private void executeDeleteAction(ParentableNode[] fakeSelectedNodesToBeDeleted, Map<String, Integer> typePerTimesCalled) throws IOException {
        DeleteAction action = new DeleteAction();
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getKnFunctionsTreeStructure(any())).thenReturn(null);
            action.executeDelete(project, kn, fakeSelectedNodesToBeDeleted);
            verify(kn, times(typePerTimesCalled.getOrDefault(SERVICE, 0))).deleteServices(any());
            verify(kn, times(typePerTimesCalled.getOrDefault(REVISION, 0))).deleteRevisions(any());
        }
    }
}
