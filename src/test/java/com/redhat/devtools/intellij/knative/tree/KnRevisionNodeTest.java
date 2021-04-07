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
package com.redhat.devtools.intellij.knative.tree;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.knative.kn.Revision;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class KnRevisionNodeTest {
    private KnRootNode knRootNode;
    private KnServiceNode knServiceNode;

    @Before
    public void before() {
        Project project = mock(Project.class);
        knRootNode = spy(new KnRootNode(project));
        knServiceNode = mock(KnServiceNode.class);
    }

    @Test
    public void Constructor_KnRevisionNode() {
        Revision revision = new Revision("revision", Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
        KnRevisionNode knRevisionNode = new KnRevisionNode(knRootNode, knServiceNode, revision);
        assertEquals("revisions", knRevisionNode.getName());
        assertEquals(knRootNode, knRevisionNode.getRootNode());
        assertEquals(knServiceNode, knRevisionNode.getParent());
        assertEquals(revision, knRevisionNode.getRevision());
    }
}
