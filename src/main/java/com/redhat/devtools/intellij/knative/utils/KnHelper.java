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
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.model.GenericResource;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.common.utils.StringHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import java.io.IOException;
import java.util.Map;

public class KnHelper {

    public static String getYamlFromNode(ParentableNode node) throws IOException {
        Kn knCli = node.getRootNode().getKn();
        String content = "";
        if (node instanceof KnServiceNode) {
            content = knCli.getServiceYAML(node.getName());
        } else if (node instanceof KnRevisionNode) {
            content = knCli.getRevisionYAML(node.getName());
        }
        return content;
    }

    public static boolean saveOnCluster(Project project, String yaml, boolean isCreate) throws IOException {
        if (!isCreate && !isSaveConfirmed("Do you want to push the changes to the cluster?")) {
            return false;
        }

        Kn knCli = TreeHelper.getKn(project);
        if (knCli == null) {
            throw new IOException("Unable to save the resource to the cluster. Internal error, please retry or restart the IDE.");
        }

        if (isCreate) {
            saveNew(knCli, yaml);
        } else {
            save(knCli, yaml);
        }
        return true;
    }

    private static boolean isSaveConfirmed(String confirmationMessage) {
        int resultDialog = UIHelper.executeInUI(() ->
                Messages.showYesNoDialog(
                        confirmationMessage,
                        "Save to cluster",
                        null
                ));

        return resultDialog == Messages.OK;
    }

    private static void save(Kn knCli, String yaml) throws IOException {
        GenericResource resourceWithUpdatedFields = generateResourceFromYAML(yaml);
        CustomResourceDefinitionContext crdContext = getCRDContext(resourceWithUpdatedFields.getApiVersion(), resourceWithUpdatedFields.getKind());

        Map<String, Object> existingResource = knCli.getCustomResource(resourceWithUpdatedFields.getName(), crdContext);
        if (existingResource == null) {
            knCli.createCustomResource(crdContext, yaml);
        } else {
            JsonNode resourceUpdated = updateExistingResource(existingResource, resourceWithUpdatedFields);
            knCli.editCustomResource(resourceWithUpdatedFields.getName(), crdContext, resourceUpdated.toString());
        }
    }

    private static JsonNode updateExistingResource(Map<String, Object> existingResourceAsMap, GenericResource resourceWithUpdatedFields) throws IOException {
        JsonNode resource = JSONHelper.MapToJSON(existingResourceAsMap);
        JsonNode labels = resourceWithUpdatedFields.getMetadata().get("labels");
        ((ObjectNode) resource.get("metadata")).remove("labels");
        if (labels != null) {
            ((ObjectNode) resource.get("metadata")).set("labels", labels);
        }
        JsonNode annotations = resourceWithUpdatedFields.getMetadata().get("annotations");
        ((ObjectNode) resource.get("metadata")).remove("annotations");
        if (annotations != null) {
            ((ObjectNode) resource.get("metadata")).set("annotations", annotations);
        }
        ((ObjectNode) resource).set("spec", resourceWithUpdatedFields.getSpec());
        return resource;
    }

    public static void saveNew(Kn knCli, String yaml) throws IOException {
        GenericResource resource = generateResourceFromYAML(yaml);
        CustomResourceDefinitionContext crdContext = getCRDContext(resource.getApiVersion(), resource.getKind());
        knCli.createCustomResource(crdContext, yaml);
    }

    private static CustomResourceDefinitionContext getCRDContext(String apiVersion, String kind) throws IOException {
        String plural = StringHelper.getPlural(kind);
        if (Strings.isNullOrEmpty(apiVersion) || Strings.isNullOrEmpty(plural)) return null;
        String[] groupVersion = apiVersion.split("/");
        if (groupVersion.length != 2) {
            throw new IOException("Knative " + kind + " has not a valid format. ApiVersion field contains an invalid value.");
        }
        String group = groupVersion[0];
        String version = groupVersion.length > 1 ? groupVersion[1]: "v1";
        return new CustomResourceDefinitionContext.Builder()
                .withName(plural + "." + group)
                .withGroup(group)
                .withScope("Namespaced")
                .withVersion(version)
                .withPlural(plural)
                .build();
    }

    public static boolean isWritable(ParentableNode node) {
        return node instanceof KnServiceNode;
    }

    public static GenericResource generateResourceFromYAML(String yaml) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(yaml, GenericResource.class);
    }
}
