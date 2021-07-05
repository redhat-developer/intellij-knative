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
import java.io.IOException;
import org.junit.Test;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DeleteActionTest extends ActionTest {

    @Test
    public void ExecuteDelete_OneKnServiceNodeSelected_DeleteOneService() throws IOException {
        executeDeleteAction(new ParentableNode[] {knServiceNode}, 1, 0, 0);
    }

    @Test
    public void ExecuteDelete_OneKnRevisionNodeSelected_DeleteOneRevision() throws IOException {
        executeDeleteAction(new ParentableNode[] {knRevisionNode}, 0 ,1, 0);
    }

    @Test
    public void ExecuteDelete_OneKnEventSourceNodeSelected_DeleteOneRevision() throws IOException {
        executeDeleteAction(new ParentableNode[] {knSourceNode}, 0 ,0, 1);
    }

    @Test
    public void ExecuteDelete_TwoDifferentKnNodeSelected_CalledTwoDelete() throws IOException {
        executeDeleteAction(new ParentableNode[] {knServiceNode, knRevisionNode}, 1, 1, 0);
    }

    private void executeDeleteAction(ParentableNode[] fakeSelectedNodesToBeDeleted, int timesDeleteServices, int timesDeleteRevisions, int timesDeleteSources) throws IOException {
        DeleteAction action = new DeleteAction();
        action.executeDelete(project, kn, fakeSelectedNodesToBeDeleted);
        verify(kn, times(timesDeleteServices)).deleteServices(any());
        verify(kn, times(timesDeleteRevisions)).deleteRevisions(any());
        verify(kn, times(timesDeleteSources)).deleteEventSources(any());


    }
}
