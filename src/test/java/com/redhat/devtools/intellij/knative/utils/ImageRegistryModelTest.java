/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.utils;

import com.redhat.devtools.intellij.knative.utils.model.ImageRegistryModel;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ImageRegistryModelTest {

    @Test
    public void IsValid_ImageRegistryModelWithImageSet_True() {
        ImageRegistryModel model = new ImageRegistryModel("image", "");
        assertTrue(model.isValid());
    }

    @Test
    public void IsValid_ImageRegistryModelWithRegistrySet_True() {
        ImageRegistryModel model = new ImageRegistryModel("", "registry");
        assertTrue(model.isValid());
    }

    @Test
    public void IsValid_ImageRegistryModelWithAutoDiscovery_True() {
        ImageRegistryModel model = new ImageRegistryModel();
        model.setAutoDiscovery();
        assertTrue(model.isValid());
    }

    @Test
    public void IsValid_EmptyImageRegistryModel_False() {
        ImageRegistryModel model = new ImageRegistryModel();
        assertFalse(model.isValid());
    }
}
