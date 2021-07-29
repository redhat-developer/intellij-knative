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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;

public class FunctionDeserializer extends StdNodeBasedDeserializer<Function> {
    protected FunctionDeserializer() {
        super(Function.class);
    }

    @Override
    public Function convert(JsonNode root, DeserializationContext deserializationContext) {
        String name = root.get("name").asText();
        String namespace = root.get("namespace").asText();
        String runtime = root.get("runtime").asText();
        String url = root.has("url") ? root.get("url").asText() : "";
        boolean isReady = root.has("ready") && root.get("ready").asBoolean();
        return new Function(name, namespace, runtime, url, isReady, true);
    }
}