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
import com.redhat.devtools.intellij.knative.ui.createFunc.CreateFuncModel;
import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.redhat.devtools.intellij.knative.Constants.KNATIVE_TOOL_WINDOW_ID;


public class KnCli implements Kn {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper(new JsonFactory());
    private final Project project;
    private KubernetesClient client;
    private final String knCommand, funcCommand;
    private Map<String, String> envVars;
    private boolean hasKnativeServing, hasKnativeEventing;

    public KnCli(Project project, String knCommand, String funcCommand) {
        this.knCommand = knCommand;
        this.funcCommand = funcCommand;
        this.project = project;
        this.client = new DefaultKubernetesClient(new ConfigBuilder().build());
        try {
            this.envVars = NetworkUtils.buildEnvironmentVariables(client.getMasterUrl().toString());
        } catch (URISyntaxException e) {
            this.envVars = Collections.emptyMap();
        }
        this.hasKnativeServing = false;
        this.hasKnativeEventing = false;
    }

    @Override
    public boolean isKnativeServingAware() throws IOException {
        // to speed up a bit the process we only call the cluster if we didn't find knative serving in last call
        if (!hasKnativeServing) {
            try {
                hasKnativeServing = client.rootPaths().getPaths().stream().anyMatch(path -> path.endsWith("serving.knative.dev"));
            } catch (KubernetesClientException e) {
                throw new IOException(e);
            }
        }
        return hasKnativeServing;
    }

    @Override
    public boolean isKnativeEventingAware() throws IOException {
        // to speed up a bit the process we only call the cluster if we didn't find knative eventing in last call
        if (!hasKnativeEventing) {
            try {
                hasKnativeEventing = client.rootPaths().getPaths().stream().anyMatch(path -> path.endsWith("eventing.knative.dev"));
            } catch (KubernetesClientException e) {
                throw new IOException(e);
            }
        }
        return hasKnativeEventing;
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
        ExecHelper.ExecResult execResult = ExecHelper.executeWithResult(knCommand, envVars, "service", "list", "-o", "json");
        if (execResult.getStdOut().startsWith("No services found.")) {
            return Collections.emptyList();
        }
        return getCustomCollectionFromItemsField(execResult.getStdOut(), Service.class);
    }

    @Override
    public List<Revision> getRevisionsForService(String serviceName) throws IOException {
        String json = ExecHelper.execute(knCommand, envVars, "revision", "list", "-o", "json", "-s", serviceName);
        if (json.startsWith("No revisions found.")) {
            return Collections.emptyList();
        }
        return getCustomCollectionFromItemsField(json, Revision.class);
    }

    @Override
    public List<Function> getFunctions() throws IOException {
        String json = ExecHelper.execute(funcCommand, envVars, "list", "-n", getNamespace(), "-o", "json");
        if (json.startsWith("No functions found")) {
            return Collections.emptyList();
        }
        return getCustomCollection(json, Function.class);
    }

    @Override
    public Service getService(String name) throws IOException {
        String json = ExecHelper.execute(knCommand, envVars, "service", "describe", name, "-o", "json", "-n", getNamespace());
        JavaType customClassCollection = JSON_MAPPER.getTypeFactory().constructType(Service.class);
        return JSON_MAPPER.readValue(json, customClassCollection);
    }

    @Override
    public String getServiceYAML(String name) throws IOException {
        return ExecHelper.execute(knCommand, envVars, "service", "describe", name, "-o", "yaml", "-n", getNamespace());
    }

    @Override
    public String getRevisionYAML(String name) throws IOException {
        return ExecHelper.execute(knCommand, envVars, "revision", "describe", name, "-o", "yaml", "-n", getNamespace());
    }

    @Override
    public void deleteServices(List<String> services) throws IOException {
        ExecHelper.execute(knCommand, envVars, getDeleteArgs("service", services));
    }

    @Override
    public void deleteRevisions(List<String> revisions) throws IOException {
        ExecHelper.execute(knCommand, envVars, getDeleteArgs("revision", revisions));
    }

    @Override
    public void deleteFunctions(List<String> functions) throws IOException {
        for (String function: functions) {
            ExecHelper.execute(funcCommand, envVars, "delete", function, "-n", getNamespace());
        }
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
        ExecHelper.ExecResult result = ExecHelper.executeWithResult(knCommand, envVars, "source", "list", "-o", "json");

        if (result.getStdOut().startsWith("No sources found.")) {
            return Collections.emptyList();
        }
        return getCustomCollectionFromItemsField(result.getStdOut(), Source.class);
    }

    private <T> List<T> getCustomCollectionFromItemsField(String json, Class<T> customClass) throws IOException {
        if (!JSON_MAPPER.readTree(json).has("items")) return Collections.emptyList();
        if (JSON_MAPPER.readTree(json).get("items").isNull()) return Collections.emptyList();

        return getCollection(JSON_MAPPER.readTree(json).get("items").toString(), customClass);
    }

    private <T> List<T> getCustomCollection(String json, Class<T> customClass) throws IOException {
        return getCollection(JSON_MAPPER.readTree(json).toString(), customClass);
    }

    private <T> List<T> getCollection(String json, Class<T> customClass) throws IOException {
        JavaType customClassCollection = JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, customClass);
        return JSON_MAPPER.readValue(json, customClassCollection);
    }

    @Override
    public void tagRevision(String service, String revision, String tag) throws IOException {
        ExecHelper.execute(knCommand, envVars, "service", "update", service, "--tag", revision + "=" + tag);
    }

    @Override
    public File getFuncFile(Path root) throws IOException {
        File file = root.resolve("func.yaml").toFile();
        if (!file.exists()) {
            throw new IOException("No func.yaml file found");
        }
        return file;
    }

    @Override
    public URL getFuncFileURL(Path root) throws IOException {
        File file = getFuncFile(root);
        return file.toURI().toURL();
    }

    @Override
    public void createFunc(CreateFuncModel model) throws IOException {
        ExecHelper.execute(funcCommand, envVars, "create", model.getPath(), "-l", model.getRuntime(), "-t", model.getTemplate());
    }

    @Override
    public void buildFunc(String path, String registry, String image) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, getBuildDeployArgs("build", "", path, registry, image));
    }

    @Override
    public void deployFunc(String namespace, String path, String registry, String image) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, getBuildDeployArgs("deploy", namespace, path, registry, image));
    }

    private String[] getBuildDeployArgs(String command, String namespace, String path, String registry, String image) {
        List<String> args = new ArrayList<>(Arrays.asList(funcCommand, command));
        if (image.isEmpty()) {
            args.addAll(Arrays.asList("-r", registry));
        } else {
            args.addAll(Arrays.asList("-i", image));
        }
        if (!namespace.isEmpty()) {
            args.addAll(Arrays.asList("-n", namespace));
        }
        args.addAll(Arrays.asList("-p", path));
        return args.toArray(new String[0]);
    }

    @Override
    public void runFunc(String path) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, funcCommand, "run", "-p", path);
    }

    @Override
    public void addEnv(String path) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, funcCommand, "config", "envs", "add", "-p", path);
    }

    @Override
    public void removeEnv(String path) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, funcCommand, "config", "envs", "remove", "-p", path);
    }

    @Override
    public void addVolume(String path) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, funcCommand, "config", "volumes", "add", "-p", path);
    }

    @Override
    public void removeVolume(String path) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, funcCommand, "config", "volumes", "remove", "-p", path);
    }

    @Override
    public Watch watchServiceWithLabel(String key, String value, Watcher<io.fabric8.knative.serving.v1.Service> watcher) throws IOException {
        try {
            return client.adapt(KnativeClient.class).services().inNamespace(getNamespace()).withLabel(key, value)
                    .watch(watcher);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
