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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.NetworkUtils;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KnCli implements Kn {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());
    private final Project project;
    private final KubernetesClient client;
    private final String command;
    private Map<String, String> envVars;

    public KnCli(Project project, String command) {
        this.command = command;
        this.project = project;
        this.client = new DefaultKubernetesClient(new ConfigBuilder().build());
        try {
            this.envVars = NetworkUtils.buildEnvironmentVariables(client.getMasterUrl().toString());
        } catch (URISyntaxException e) {
            this.envVars = Collections.emptyMap();
        }
    }

    @Override
    public boolean isKnativeServingAware() throws IOException {
        try {
            return client.rootPaths().getPaths().stream().anyMatch(path -> path.endsWith("serving.knative.dev"));
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isKnativeEventingAware() throws IOException {
        try {
            return client.rootPaths().getPaths().stream().anyMatch(path -> path.endsWith("eventing.knative.dev"));
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public URL getMasterUrl() {
        return client.getMasterUrl();
    }

    @Override
    public String getNamespace() {
        String namespace = client.getNamespace();
        if (Strings.isNullOrEmpty(namespace)) {
            namespace = "default";
        }
        return namespace;
    }

    @Override
    public List<Service> getServicesList() throws IOException {
        String json = ExecHelper.execute(command, envVars, "service", "list", "-o", "json");
        return getCustomCollection(json, Service.class);
    }

    @Override
    public List<Revision> getRevisionsForService(String serviceName) throws IOException {
        String json = ExecHelper.execute(command, envVars, "revision", "list", "-o", "json", "-s", serviceName);
        return getCustomCollection(json, Revision.class);
    }

    @Override
    public String getServiceYAML(String name) throws IOException {
        return ExecHelper.execute(command, envVars, "service", "describe", name, "-o", "yaml", "-n", getNamespace());
    }

    private <T> List<T> getCustomCollection(String json, Class<T> customClass) throws IOException {
        if (!JSON_MAPPER.readTree(json).has("items")) return Collections.emptyList();
        if (JSON_MAPPER.readTree(json).get("items").isNull()) return Collections.emptyList();

        JavaType customClassCollection = JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, customClass);
        return JSON_MAPPER.readValue(JSON_MAPPER.readTree(json).get("items").toString(), customClassCollection);
    }
}
