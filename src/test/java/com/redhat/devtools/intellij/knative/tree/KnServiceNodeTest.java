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
import com.intellij.util.Function;
import com.redhat.devtools.intellij.knative.kn.Service;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class KnServiceNodeTest {
    private KnRootNode knRootNode;
    private KnServingNode knServingNode;
    private Service serviceIfTrue, serviceIfFalse;

    @Before
    public void before() {
        Project project = mock(Project.class);
        knRootNode = spy(new KnRootNode(project));
        knServingNode = mock(KnServingNode.class);
        serviceIfTrue = new Service("true", null);
        serviceIfFalse = new Service("false", null);
    }

    @Test
    public void Constructor_KnServiceNode() {
        KnServiceNode knServiceNode = new KnServiceNode(knRootNode, knServingNode, getService());
        assertEquals("false", knServiceNode.getName());
        assertEquals(knRootNode, knServiceNode.getRootNode());
        assertEquals(knServingNode, knServiceNode.getParent());
        assertEquals(serviceIfTrue, knServiceNode.getService(true));
    }

    private Function<Boolean, Service> getService() {
        return (toUpdate) -> {
            if (toUpdate) {
                return serviceIfTrue;
            } else {
                return serviceIfFalse;
            }
        };
    }
}
