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
package com.redhat.devtools.intellij.knative.kn;

import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FunctionTest {
    @Test
    public void LocalFunction_Object() {
        Function function = new Function("name", "namespace", "runtime", "url", "image", true, false, "path");
        assertEquals("name", function.getName());
        assertEquals("namespace", function.getNamespace());
        assertEquals("runtime", function.getRuntime());
        assertEquals("url", function.getUrl());
        assertEquals("image", function.getImage());
        assertTrue(function.isReady());
        assertFalse(function.isPushed());
        assertEquals("path", function.getLocalPath());
    }

    @Test
    public void Function_Object() {
        Function function = new Function("name", "namespace", "runtime", "url", false, true);
        assertEquals("name", function.getName());
        assertEquals("namespace", function.getNamespace());
        assertEquals("runtime", function.getRuntime());
        assertEquals("url", function.getUrl());
        assertTrue(function.getImage().isEmpty());
        assertFalse(function.isReady());
        assertTrue(function.isPushed());
        assertTrue(function.getLocalPath().isEmpty());
    }
}
