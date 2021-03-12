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
package com.redhat.devtools.intellij.knative.utils;

import com.fasterxml.jackson.databind.JsonNode;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

class DeployModel {
    private String name, apiVersion, kind;
    private JsonNode spec;
    private CustomResourceDefinitionContext crdContext;

    public DeployModel(String name, String kind, String apiVersion, JsonNode spec, CustomResourceDefinitionContext crdContext) {
        this.name = name;
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.spec = spec;
        this.crdContext = crdContext;
    }

    public String getName() {
        return name;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public JsonNode getSpec() {
        return spec;
    }

    public CustomResourceDefinitionContext getCrdContext() {
        return crdContext;
    }
}