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
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class KnServingNodeTest {
    private KnRootNode knRootNode;

    @Before
    public void before() {
        Project project = mock(Project.class);
        knRootNode = spy(new KnRootNode(project));
    }

    @Test
    public void Constructor_KnServingNode() {
        KnServingNode knServingNode = new KnServingNode(knRootNode, knRootNode);
        assertEquals("Serving", knServingNode.getName());
        assertEquals(knRootNode, knServingNode.getRootNode());
        assertEquals(knRootNode, knServingNode.getParent());
    }
}
