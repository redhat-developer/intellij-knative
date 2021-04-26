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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

public class YAMLBuilder {
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());

    public static JsonNode yamlToJsonNode(String yaml) throws IOException {
        return YAML_MAPPER.readTree(yaml);
    }

    public static JsonNode addLabelToResource(String yaml, String labelKey, String labelValue) throws IOException {
        ObjectNode resource = (ObjectNode) yamlToJsonNode(yaml);
        ObjectNode metadata;
        if (!resource.has("metadata")) {
            metadata = YAML_MAPPER.createObjectNode();
        } else {
            metadata = (ObjectNode) resource.get("metadata");
        }

        if (!metadata.has("labels")) {
            ObjectNode newLabel = YAML_MAPPER.createObjectNode();
            newLabel.put(labelKey, labelValue);
            metadata.set("labels", newLabel);
        } else if (!metadata.get("labels").has(labelKey)) {
            ((ObjectNode)metadata.get("labels")).put(labelKey, labelValue);
        }

        resource.set("metadata", metadata);
        return resource;
    }

    public static JsonNode removeLabelFromResource(String yaml, String labelKey) throws IOException {
        ObjectNode resource = (ObjectNode) yamlToJsonNode(yaml);
        ObjectNode metadata;
        if (!resource.has("metadata")) {
            metadata = YAML_MAPPER.createObjectNode();
        } else {
            metadata = (ObjectNode) resource.get("metadata");
        }

        if (metadata.has("labels") && metadata.get("labels").has(labelKey)) {
            ((ObjectNode) metadata.get("labels")).remove(labelKey);
            if (metadata.get("labels").isEmpty()) {
                metadata.remove("labels");
            }
        }

        resource.set("metadata", metadata);
        return resource;
    }
}
