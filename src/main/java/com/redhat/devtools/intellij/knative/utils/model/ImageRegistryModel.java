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
package com.redhat.devtools.intellij.knative.utils.model;

public class ImageRegistryModel {
    private String image, registry;
    private boolean autoDiscovery;

    public ImageRegistryModel(String image, String registry) {
        this.image = image;
        this.registry = registry;
        this.autoDiscovery = false;
    }

    public ImageRegistryModel() {
        this("", "");
    }

    public void setAutoDiscovery() {
        this.image = "";
        this.registry = "";
        this.autoDiscovery = true;
    }

    public String getImage() {
        return image;
    }

    public String getRegistry() {
        return registry;
    }

    public boolean isAutoDiscovery() {
        return autoDiscovery;
    }

    public boolean isValid() {
        return !image.isEmpty()
                || !registry.isEmpty()
                || isAutoDiscovery();
    }
}
