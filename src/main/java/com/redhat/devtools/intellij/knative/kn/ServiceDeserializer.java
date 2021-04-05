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

import java.util.ArrayList;
import java.util.Collections;
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

    @Nullable
    private ServiceStatus convertToStatus(JsonNode statusNode) {
        if (statusNode == null || statusNode.isEmpty()) {
            return null;
        }
        String url = statusNode.has("url") ? statusNode.get("url").asText() : "";
        int observedGeneration = statusNode.has("observedGeneration") ? statusNode.get("observedGeneration").asInt() : 1;
        String latestReadyRevisionName = statusNode.has("latestReadyRevisionName") ? statusNode.get("latestReadyRevisionName").asText() : "";
        String latestCreatedRevisionName = statusNode.has("latestCreatedRevisionName") ? statusNode.get("latestCreatedRevisionName").asText() : "";

        String addressUrl = "";
        if (statusNode.has("address") && statusNode.get("address").has("url")) {
            addressUrl = statusNode.get("address").get("url").asText();
        }

        List<ServiceTraffic> traffic = convertToTraffic(statusNode.get("traffic"));
        List<StatusCondition> conditions = DeserializerUtil.getConvertToConditions(statusNode.get("conditions"));

        return new ServiceStatus(url, observedGeneration, latestReadyRevisionName, latestCreatedRevisionName, addressUrl, traffic, conditions);
    }

    private List<ServiceTraffic> convertToTraffic(JsonNode trafficNode) {
        if (trafficNode == null || !trafficNode.isArray()) {
            return Collections.emptyList();
        }
        List<ServiceTraffic> traffic = new ArrayList<>();
        for (JsonNode jsonNode : trafficNode) {
            String tag = jsonNode.has("tag") ? jsonNode.get("tag").asText() : "";
            String revisionName = jsonNode.has("revisionName") ? jsonNode.get("revisionName").asText() : "";
            String configurationName = jsonNode.has("configurationName") ? jsonNode.get("configurationName").asText() : "";
            boolean latestRevision = jsonNode.has("latestRevision") && jsonNode.get("latestRevision").asBoolean();
            int percent = jsonNode.has("percent") ? jsonNode.get("percent").intValue() : -1;
            String url = jsonNode.has("url") ? jsonNode.get("url").asText() : "";

            traffic.add(new ServiceTraffic(tag, revisionName, configurationName, latestRevision, percent, url));
        }

        return traffic;
    }

}
