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
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.kubernetes.ClusterHelper;
import com.redhat.devtools.intellij.common.kubernetes.ClusterInfo;
import com.redhat.devtools.intellij.common.utils.CommonTerminalExecutionConsole;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.NetworkUtils;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.ui.createFunc.CreateFuncModel;
import com.redhat.devtools.intellij.knative.utils.model.InvokeModel;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.IS_OPENSHIFT;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.KUBERNETES_VERSION;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.OPENSHIFT_VERSION;


public class KnCli implements Kn {
    private static final Logger LOGGER = LoggerFactory.getLogger(KnCli.class);
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
        reportTelemetry();
    }

    private void reportTelemetry() {
        TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance().action(TelemetryService.NAME_PREFIX_MISC + "login");
        try {
            ClusterInfo info = ClusterHelper.getClusterInfo(client);
            telemetry.property(KUBERNETES_VERSION, info.getKubernetesVersion());
            telemetry.property(IS_OPENSHIFT, Boolean.toString(info.isOpenshift()));
            telemetry.property(OPENSHIFT_VERSION, info.getOpenshiftVersion());
            telemetry.send();
        } catch (RuntimeException e) {
            // do not send telemetry when there is no context ( ie default kube URL as master URL )
            try {
                //workaround to not send null values
                if (e.getMessage() != null) {
                    telemetry.error(e).send();
                } else {
                    telemetry.error(e.toString()).send();
                }
            } catch (RuntimeException ex) {
                LOGGER.warn(ex.getLocalizedMessage(), ex);
            }
        }
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
    public void buildFunc(String path, String registry, String image, ConsoleView terminalExecutionConsole, ProcessListener processListener) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, envVars, terminalExecutionConsole, processListener, getBuildDeployArgs("build", "", path, registry, image, true));
    }

    @Override
    public void deployFunc(String namespace, String path, String registry, String image) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, envVars, getBuildDeployArgs("deploy", namespace, path, registry, image, true));
    }

    private String[] getBuildDeployArgs(String command, String namespace, String path, String registry, String image, boolean verbose) {
        List<String> args = new ArrayList<>(Arrays.asList(funcCommand, command));
        if (image.isEmpty()) {
            args.addAll(Arrays.asList("-r", registry));
        } else {
            args.addAll(Arrays.asList("-i", image));
            if (client.isAdaptable(OpenShiftClient.class)) {
                args.addAll(Arrays.asList("-r", ""));
            }
        }
        if (!namespace.isEmpty()) {
            args.addAll(Arrays.asList("-n", namespace));
        }
        args.addAll(Arrays.asList("-p", path));
        if (verbose) {
            args.addAll(Collections.singletonList("-v"));
        }
        return args.toArray(new String[0]);
    }

    @Override
    public String invokeFunc(InvokeModel model) throws IOException {
        String json = ExecHelper.execute(funcCommand, envVars, getInvokeArgs(model));
        if (json != null && JSON_MAPPER.readTree(json).has("ID")) {
            return JSON_MAPPER.readTree(json).get("ID").asText();
        }
        throw new IOException("Failed to retrieve invoke execution ID. Invocation didn't complete successfully");
    }

    private String[] getInvokeArgs(InvokeModel model) {
        List<String> args = new ArrayList<>(Collections.singletonList("invoke"));
        String target = model.getTarget();
        args.addAll(Arrays.asList("-t", target));

        args.addAll(Arrays.asList("-p", model.getPath()));

        if (target.equals("remote")) {
            args.addAll(Arrays.asList("-n", model.getNamespace()));
        }

        if (model.getFile().isEmpty()) {
            args.addAll(Arrays.asList("--data", model.getData()));
        } else {
            args.addAll(Arrays.asList("--file", model.getFile()));
        }
        args.addAll((Arrays.asList("--content-type", model.getContentType())));

        if (!model.getID().isEmpty()) {
            args.addAll(Arrays.asList("--id", model.getID()));
        }

        if (!model.getFormat().isEmpty()) {
            args.addAll(Arrays.asList("-f", model.getFormat()));
        }

        if (!model.getSource().isEmpty()) {
            args.addAll(Arrays.asList("--source", model.getSource()));
        }

        if (!model.getType().isEmpty()) {
            args.addAll(Arrays.asList("--type", model.getType()));
        }

        return args.toArray(new String[0]);
    }

    @Override
    public void runFunc(String path, CommonTerminalExecutionConsole terminalExecutionConsole) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, envVars, terminalExecutionConsole, funcCommand, "run", "-p", path);
    }

    @Override
    public void addEnv(String path) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, envVars,funcCommand, "config", "envs", "add", "-p", path);
    }

    @Override
    public void removeEnv(String path) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, envVars,funcCommand, "config", "envs", "remove", "-p", path);
    }

    @Override
    public void addVolume(String path) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, envVars,funcCommand, "config", "volumes", "add", "-p", path);
    }

    @Override
    public void removeVolume(String path) throws IOException {
        ExecHelper.executeWithTerminal(project, KNATIVE_TOOL_WINDOW_ID, envVars,funcCommand, "config", "volumes", "remove", "-p", path);
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

    @Override
    public CommonTerminalExecutionConsole createTerminalTabToReuse() {
        return ExecHelper.createTerminalTabForReuse(project, KNATIVE_TOOL_WINDOW_ID);
    }
}
