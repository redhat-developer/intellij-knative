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

public class BindingSourceTest {
    @Test
    public void BindingSource_Object() {
        BindingSource bindingSource = new BindingSource("name", "parent", "subject", "sink");
        assertEquals("name", bindingSource.getName());
        assertEquals("parent", bindingSource.getParent());
        assertEquals("subject", bindingSource.getSubject());
        assertEquals("sink", bindingSource.getSink());
    }
}
