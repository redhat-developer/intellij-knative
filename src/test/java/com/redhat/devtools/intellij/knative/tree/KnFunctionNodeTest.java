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
import com.redhat.devtools.intellij.knative.kn.Function;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class KnFunctionNodeTest extends BaseTest {

    @Test
    public void Constructor_KnFunctionNode() {
        KnFunctionNode knFunctionNode = new KnFunctionNode(knRootNode, knFunctionsNode, getFunction());
        assertEquals("name", knFunctionNode.getFunction().getName());
        assertEquals(knRootNode, knFunctionNode.getRootNode());
        assertEquals(knFunctionsNode, knFunctionNode.getParent());
    }

    private Function getFunction() {
        return new Function("name", "namespace", "runtime", "", false, false);
    }
}
