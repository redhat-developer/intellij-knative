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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(using = FunctionDeserializer.class)
public class Function {
    private final String name;
    private final String namespace;
    private final String runtime;
    private String image;
    private String url;
    private boolean isReady;
    private boolean isPushed;
    private boolean isBuilding;
    private String localPath;

    public Function(String name, String namespace, String runtime, String url, String image, boolean isReady, boolean isPushed, String localPath) {
        this.name = name;
        this.namespace = namespace;
        this.runtime = runtime;
        this.url = url;
        this.image = image;
        this.isReady = isReady;
        this.isPushed = isPushed;
        this.isBuilding = false;
        this.localPath = localPath;
    }

    public Function(String name, String namespace, String runtime, String url, boolean isReady, boolean isPushed) {
        this(name, namespace, runtime, url, "", isReady, isPushed, "");
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getUrl() {
        return url;
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean isPushed() { return isPushed; }

    public void setPushed(boolean pushed) {
        isPushed = pushed;
    }

    public String getLocalPath() { return localPath; }

    public void setLocalPath(String localPath) { this.localPath = localPath; }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isBuilding() {
        return isBuilding;
    }

    public void setBuilding(boolean building) {
        isBuilding = building;
    }
}
