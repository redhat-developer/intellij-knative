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
import com.redhat.devtools.intellij.knative.kn.Kn;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MessageNodeTest {

    private KnRootNode knRootNode;
    private KnServingNode knServingNode;

    @Before
    public void before() {
        Project project = mock(Project.class);
        knRootNode = spy(new KnRootNode(project));
        knServingNode = mock(KnServingNode.class);
    }

    @Test
    public void Constructor_ParentIsRoot_MessageNode() {
        MessageNode messageNode = new MessageNode(knRootNode, knRootNode, "test");
        assertEquals("test", messageNode.getName());
        assertEquals(knRootNode, messageNode.getRootNode());
        assertEquals(knRootNode, messageNode.getParent());
    }

    @Test
    public void Constructor_ParentIsNotRoot_MessageNode() {
        MessageNode messageNode = new MessageNode(knRootNode, knServingNode, "test");
        assertEquals("test", messageNode.getName());
        assertEquals(knRootNode, messageNode.getRootNode());
        assertEquals(knServingNode, messageNode.getParent());
    }
}
