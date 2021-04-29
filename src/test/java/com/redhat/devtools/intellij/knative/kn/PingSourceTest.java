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

public class PingSourceTest {
    @Test
    public void BindingSource_Object() {
        PingSource pingSource = new PingSource("name", "parent", "schedule", "data", "sink");
        assertEquals("name", pingSource.getName());
        assertEquals("parent", pingSource.getParent());
        assertEquals("schedule", pingSource.getSchedule());
        assertEquals("data", pingSource.getData());
        assertEquals("sink", pingSource.getSink());
    }
}
