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
package com.redhat.devtools.intellij.knative.model;

import com.redhat.devtools.intellij.knative.BaseTest;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CreateDialogModelTest extends BaseTest {

    private Runnable runnable;
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.runnable = mock(Runnable.class);
    }

    @Test
    public void CreateDialogModel_WithoutServiceAndServiceAccounts_Object() {
        CreateDialogModel model = new CreateDialogModel(project, "title", "namespace", runnable);
        assertEquals(project, model.getProject());
        assertEquals("title", model.getTitle());
        assertEquals("namespace", model.getNamespace());
        assertEquals(runnable, model.getRefreshFunction());
        assertEquals(Collections.emptyList(), model.getServiceAccounts());
        assertEquals(Collections.emptyList(), model.getServices());
    }

    @Test
    public void CreateDialogModel_Object() {
        CreateDialogModel model = new CreateDialogModel(project, "title", "namespace", runnable, Arrays.asList("service"), Arrays.asList("sa"));
        assertEquals(project, model.getProject());
        assertEquals("title", model.getTitle());
        assertEquals("namespace", model.getNamespace());
        assertEquals(runnable, model.getRefreshFunction());
        assertTrue(model.getServiceAccounts().size() == 1);
        assertEquals("service", model.getServices().get(0));
    }
}
