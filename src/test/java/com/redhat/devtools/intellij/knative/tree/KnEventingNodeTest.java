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

import com.redhat.devtools.intellij.knative.BaseTest;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class KnEventingNodeTest extends BaseTest {

    public void testConstructor_KnEventingNode() {
        KnEventingNode knEventingNode = new KnEventingNode(knRootNode, knRootNode);
        assertEquals("Eventing", knEventingNode.getName());
        assertEquals(knRootNode, knEventingNode.getRootNode());
        assertEquals(knRootNode, knEventingNode.getParent());
    }
}
