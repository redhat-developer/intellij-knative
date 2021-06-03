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

public class GenericSourceTest {
    @Test
    public void GenericSource_Object() {
        GenericSource genericSource = new GenericSource("name", "parent", "sourceType", "sink");
        assertEquals("name", genericSource.getName());
        assertEquals("parent", genericSource.getParent());
        assertEquals("sourceType", genericSource.getSourceType());
        assertEquals("sink", genericSource.getSink());
    }
}
