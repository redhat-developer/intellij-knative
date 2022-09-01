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
package com.redhat.devtools.intellij.knative.ui.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.redhat.devtools.intellij.knative.ui.repository.RepositoryUtils.NATIVE_NAME;

public class Repository {

    private String name, url;
    private Map<String, String> attributes;

    public Repository() {
        this("", "");
    }

    public Repository(String name) {
        this(name, "");
    }

    public Repository(String name, String url) {
        this.name = name;
        this.url = url;
        this.attributes = new HashMap<>();
        this.attributes.put(NATIVE_NAME, name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Repository that = (Repository) o;
        return Objects.equals(name, that.name) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url);
    }

    public Repository clone() {
        return new Repository(getName(), getUrl());
    }
}
