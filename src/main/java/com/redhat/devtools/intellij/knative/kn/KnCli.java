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
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class KnCli implements Kn {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());
    private final Project project;
    private KubernetesClient client;
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
        ExecHelper.ExecResult execResult = ExecHelper.executeWithResult(command, envVars, "service", "list", "-o", "json");
        if (execResult.getStdOut().startsWith("No services found.")) {
            return Collections.emptyList();
        }
        return getCustomCollection(execResult.getStdOut(), Service.class);
    }

    @Override
    public List<Revision> getRevisionsForService(String serviceName) throws IOException {
        String json = ExecHelper.execute(command, envVars, "revision", "list", "-o", "json", "-s", serviceName);
        if (json.startsWith("No revisions found.")) {
            return Collections.emptyList();
        }
        return getCustomCollection(json, Revision.class);
    }

    @Override
    public Service getService(String name) throws IOException {
        String json = ExecHelper.execute(command, envVars, "service", "describe", name, "-o", "json", "-n", getNamespace());
        JavaType customClassCollection = JSON_MAPPER.getTypeFactory().constructType(Service.class);
        return JSON_MAPPER.readValue(json, customClassCollection);
    }

    @Override
    public List<String> getServiceAccounts() {
        return client.serviceAccounts().inNamespace(getNamespace()).list().getItems().stream().map(serviceAccount -> serviceAccount.getMetadata().getName()).collect(Collectors.toList());
    }

    @Override
    public String getServiceYAML(String name) throws IOException {
        return ExecHelper.execute(command, envVars, "service", "describe", name, "-o", "yaml", "-n", getNamespace());
    }

    @Override
    public String getRevisionYAML(String name) throws IOException {
        return ExecHelper.execute(command, envVars, "revision", "describe", name, "-o", "yaml", "-n", getNamespace());
    }

    @Override
    public void deleteServices(List<String> services) throws IOException {
        ExecHelper.execute(command, envVars, getDeleteArgs("service", services));
    }

    @Override
    public void deleteRevisions(List<String> revisions) throws IOException {
        ExecHelper.execute(command, envVars, getDeleteArgs("revision", revisions));
    }

    private String[] getDeleteArgs(String kind, List<String> resourcesToDelete) {
        List<String> args = new ArrayList<>(Arrays.asList(kind, "delete"));
        args.addAll(resourcesToDelete);
        args.addAll(Arrays.asList("-n", getNamespace()));
        return args.toArray(new String[0]);
    }

    @Override
    public Map<String, Object> getCustomResource(String name, CustomResourceDefinitionContext crdContext) {
        try {
            if (crdContext.getScope().equalsIgnoreCase("Namespaced")) {
                return new TreeMap<>(client.customResource(crdContext).get(getNamespace(), name));
            }

            return new TreeMap<>(client.customResource(crdContext).get(name));
        } catch(KubernetesClientException e) {
            // call failed bc resource doesn't exist - 404
            return null;
        }
    }

    @Override
    public void editCustomResource(String name, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException {
        try {
            if (crdContext.getScope().equalsIgnoreCase("Namespaced")) {
                client.customResource(crdContext).edit(getNamespace(), name, objectAsString);
            } else {
                client.customResource(crdContext).edit(name, objectAsString);
            }
        } catch(KubernetesClientException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    @Override
    public void createCustomResource(CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException {
        try {
            if (crdContext.getScope().equalsIgnoreCase("Namespaced")) {
                client.customResource(crdContext).create(getNamespace(), objectAsString);
            } else {
                client.customResource(crdContext).create(objectAsString);
            }
        } catch (KubernetesClientException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    @Override
    public List<Source> getSources() throws IOException {
        ExecHelper.ExecResult result = ExecHelper.executeWithResult(command, envVars, "source", "list", "-o", "json");

        if (result.getStdOut().startsWith("No sources found.")) {
            return Collections.emptyList();
        }
        return getCustomCollection(result.getStdOut(), Source.class);
    }

    private <T> List<T> getCustomCollection(String json, Class<T> customClass) throws IOException {
        if (!JSON_MAPPER.readTree(json).has("items")) return Collections.emptyList();
        if (JSON_MAPPER.readTree(json).get("items").isNull()) return Collections.emptyList();

        JavaType customClassCollection = JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, customClass);
        return JSON_MAPPER.readValue(JSON_MAPPER.readTree(json).get("items").toString(), customClassCollection);
    }

    @Override
    public void tagRevision(String service, String revision, String tag) throws IOException {
        ExecHelper.execute(command, envVars, "service", "update", service, "--tag", revision + "=" + tag);
    }
}
