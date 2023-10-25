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

public class MessageNodeTest extends BaseTest {

    public void testConstructor_ParentIsRoot_MessageNode() {
        MessageNode messageNode = new MessageNode(knRootNode, knRootNode, "test");
        assertEquals("test", messageNode.getName());
        assertEquals(knRootNode, messageNode.getRootNode());
        assertEquals(knRootNode, messageNode.getParent());
    }

    public void testConstructor_ParentIsNotRoot_MessageNode() {
        MessageNode messageNode = new MessageNode(knRootNode, knServingNode, "test");
        assertEquals("test", messageNode.getName());
        assertEquals(knRootNode, messageNode.getRootNode());
        assertEquals(knServingNode, messageNode.getParent());
    }
}
