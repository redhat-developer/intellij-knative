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

import com.redhat.devtools.intellij.knative.ui.createFunc.CreateFuncModel;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CreateFuncModelTest {

    @Test
    public void Constructor_CreateFuncModel() {
        CreateFuncModel createFuncModel = new CreateFuncModel("name", "path", "runtime", "template", true);
        assertEquals("name", createFuncModel.getName());
        assertEquals("path", createFuncModel.getPath());
        assertEquals("runtime", createFuncModel.getRuntime());
        assertEquals("template", createFuncModel.getTemplate());
        assertTrue(createFuncModel.isImportInProject());
    }
}
