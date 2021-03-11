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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DeserializerUtil {

    public static List<StatusCondition> getConvertToConditions(JsonNode conditionsNode) {

        if (conditionsNode == null || !conditionsNode.isArray()) {
            return Collections.emptyList();
        }

        List<StatusCondition> result = new ArrayList<>();
        for (JsonNode jsonNode : conditionsNode) {
            String lastTransitionTime = jsonNode.get("lastTransitionTime").asText();
            String status = jsonNode.get("status").asText();
            String type = jsonNode.get("type").asText();
            String reason = null;
            if (jsonNode.has("reason")) {
                reason = jsonNode.get("reason").asText();
            }

            String message = null;
            if (jsonNode.has("message")) {
                message = jsonNode.get("message").asText();
            }

            result.add(new StatusCondition(lastTransitionTime, status, type, reason, message));
        }

        return result;
    }

    public static Map<String, String> getStringMap(JsonNode jsonNode) {
        if (jsonNode == null || !jsonNode.isObject()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            result.put(entry.getKey(), entry.getValue().textValue());
        }
        return result;
    }
}
