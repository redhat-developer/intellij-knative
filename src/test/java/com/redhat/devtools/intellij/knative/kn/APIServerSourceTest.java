/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.kn;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class APIServerSourceTest {
    @Test
    public void APIServerSource_Object() {
        APIServerSource serverSource = new APIServerSource("name", "parent", "resource", "sink");
        assertEquals("name", serverSource.getName());
        assertEquals("parent", serverSource.getParent());
        assertEquals("resource", serverSource.getResource());
        assertEquals("sink", serverSource.getSink());
    }
}
