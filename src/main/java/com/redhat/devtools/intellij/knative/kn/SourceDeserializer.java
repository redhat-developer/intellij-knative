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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SourceDeserializer extends StdNodeBasedDeserializer<Source> {
    protected SourceDeserializer() {
        super(Source.class);
    }

    @Override
    public Source convert(JsonNode root, DeserializationContext deserializationContext) throws IOException {
        String name = root.get("metadata").get("name").asText();
        String kind = root.get("kind").asText();
        BaseSource sinkSource = getSinkSource(root, name, kind);
        return new Source(name, kind, sinkSource);
    }

    @Nullable
    private BaseSource getSinkSource(JsonNode root, String name, String kind) {
        BaseSource result;
        JsonNode spec = root.get("spec");
        if (spec == null) {
            return null;
        }
        switch (kind) {
            case "ApiServerSource":
                result = createAPIServer(name, spec);
                break;
            case "SinkBinding":
                result = createBindingSource(name, spec);
                break;
            case "PingSource":
                result = createPingSource(name, spec);
                break;
            default:
                result = createGenericSource(name, spec, kind);
        }

        return result;
    }

    private GenericSource createGenericSource(String name, JsonNode spec, String kind) {
        String sink = getString(spec);
        return new GenericSource(name, "Sources", kind, sink);
    }

    private PingSource createPingSource(String name, JsonNode spec) {
        String sink = getString(spec);
        String schedule = spec.has("schedule") ? spec.get("schedule").asText() : "";
        String data = spec.has("jsonData") ? spec.get("jsonData").asText() : "";

        return new PingSource(name, "Sources", schedule, data, sink);
    }

    private BindingSource createBindingSource(String name, JsonNode spec) {
        String sink = getString(spec);

        String subject = "";
        if (spec.has("subject") && spec.get("subject").has("name")) {
            subject = spec.get("subject").get("name").asText();
        }
        return new BindingSource(name, "Sources", subject, sink);
    }

    private APIServerSource createAPIServer(String name, JsonNode spec) {
        String controller = "";
        if (spec.has("resources") && spec.get("resources").get(0).has("controllerSelector")) {
            controller = spec.get("resources").get(0).get("controllerSelector").get("name").asText();
        }

        String sink = getString(spec);
        return new APIServerSource(name, "Sources", controller, sink);
    }

    @Nullable
    private String getString(JsonNode spec) {
        String sink = null;
        if (spec.has("sink")) {
            if (spec.get("sink").has("ref")) {
                sink = spec.get("sink").get("ref").get("name").asText();
            }
            if (spec.get("sink").has("uri")) {
                sink = spec.get("sink").get("uri").asText();
            }
        }
        return sink;
    }

}
