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

import java.util.ArrayList;
import java.util.List;

public class ServiceDeserializer extends StdNodeBasedDeserializer<Service> {

    public ServiceDeserializer() {
        super(Service.class);
    }

    @Override
    public Service convert(JsonNode root, DeserializationContext deserializationContext) {
        String name = root.get("metadata").get("name").asText();
        JsonNode jsonStatus = root.get("status");

        ServiceStatus status = convertToStatus(jsonStatus);

        return new Service(name, status);
    }

    private ServiceStatus convertToStatus(JsonNode statusNode) {
        String url = statusNode.get("url").asText();
        int observedGeneration = statusNode.get("observedGeneration").asInt();
        String latestReadyRevisionName = statusNode.get("latestReadyRevisionName").asText();
        String latestCreatedRevisionName = statusNode.get("latestCreatedRevisionName").asText();
        String addressUrl = statusNode.get("address").get("url").asText();

        List<ServiceTraffic> traffic = convertToTraffic(statusNode.get("traffic"));
        List<ServiceCondition> conditions = convertToConditions(statusNode.get("conditions"));

        return new ServiceStatus(url, observedGeneration, latestReadyRevisionName, latestCreatedRevisionName, addressUrl, traffic, conditions);
    }

    private List<ServiceTraffic> convertToTraffic(JsonNode trafficNode) {
        if (trafficNode == null || !trafficNode.isArray()) {
            return null;
        }
        List<ServiceTraffic> traffic = new ArrayList<>();
        for (JsonNode jsonNode : trafficNode) {
            String tag = jsonNode.get("tag") != null ? jsonNode.get("tag").asText() : null;
            String revisionName = jsonNode.get("revisionName") != null ? jsonNode.get("revisionName").asText() : null;
            String configurationName = jsonNode.get("configurationName") != null ? jsonNode.get("configurationName").asText() : null;
            String latestRevision = jsonNode.get("latestRevision") != null ? jsonNode.get("latestRevision").asText() : null;
            int percent = jsonNode.get("percent") != null ? jsonNode.get("percent").intValue() : -1;
            String url = jsonNode.get("url") != null ? jsonNode.get("url").asText() : null;

            traffic.add(new ServiceTraffic(tag, revisionName, configurationName, latestRevision, percent, url));
        }

        return traffic;
    }

    private List<ServiceCondition> convertToConditions(JsonNode conditionsNode) {
        if (conditionsNode == null || !conditionsNode.isArray()) {
            return null;
        }

        List<ServiceCondition> result = new ArrayList<>();
        for (JsonNode jsonNode : conditionsNode) {
            String lastTransitionTime = jsonNode.get("lastTransitionTime").asText();
            String status = jsonNode.get("status").asText();
            String type = jsonNode.get("type").asText();
            result.add(new ServiceCondition(lastTransitionTime, status, type));
        }

        return result;
    }
}
