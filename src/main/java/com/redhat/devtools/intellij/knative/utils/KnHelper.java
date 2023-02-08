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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.model.GenericResource;
import com.redhat.devtools.intellij.common.utils.StringHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import io.fabric8.kubernetes.api.Pluralize;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.utils.Serialization;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_CRUD;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.PROP_RESOURCE_CRUD;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.VALUE_ABORTED;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.VALUE_RESOURCE_CRUD_CREATE;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.VALUE_RESOURCE_CRUD_UPDATE;

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
        TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_CRUD + "save to cluster");
        if (!isCreate && !isSaveConfirmed("Do you want to push the changes to the cluster?")) {
            telemetry.result(VALUE_ABORTED)
                    .send();
            return false;
        }

        Kn knCli = TreeHelper.getKn(project);
        if (knCli == null) {
            telemetry.error("kn not found")
                    .send();
            throw new IOException("Unable to save the resource to the cluster. Internal error, please retry or restart the IDE.");
        }

        if (isCreate) {
            saveNew(knCli, yaml);
        } else {
            save(knCli, yaml);
        }
        telemetry.property(PROP_RESOURCE_CRUD, (isCreate ? VALUE_RESOURCE_CRUD_CREATE : VALUE_RESOURCE_CRUD_UPDATE))
                .send();
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
        GenericKubernetesResource resourceWithUpdatedFields = Serialization.unmarshal(yaml, GenericKubernetesResource.class);
        CustomResourceDefinitionContext crdContext = getCRDContext(resourceWithUpdatedFields.getApiVersion(), resourceWithUpdatedFields.getKind());
        GenericKubernetesResource existingResource = knCli.getCustomResource(resourceWithUpdatedFields.getMetadata().getName(), crdContext);
        if (existingResource == null) {
            knCli.createCustomResource(crdContext, resourceWithUpdatedFields);
        } else {
            GenericKubernetesResource resourceUpdated = updateExistingResource(existingResource, resourceWithUpdatedFields);
            knCli.editCustomResource(resourceWithUpdatedFields.getMetadata().getName(), crdContext, resourceUpdated.toString());
        }
    }

    private static GenericKubernetesResource updateExistingResource(GenericKubernetesResource existing, GenericKubernetesResource updated) throws IOException {
        Map<String, String> updatedLabels = updated.getMetadata().getLabels();
        if (updatedLabels != null) {
            existing.getMetadata().setLabels(updatedLabels);
        }
        Map<String, String> annotations = updated.getMetadata().getAnnotations();
        if (annotations != null) {
            existing.getMetadata().setAnnotations(annotations);
        }
        Object spec = updated.getAdditionalProperties().get("spec");
        existing.getAdditionalProperties().put("spec", spec);
        return updated;
    }

    public static void saveNew(Kn knCli, String yaml) throws IOException {
        GenericKubernetesResource resource = Serialization.unmarshal(yaml, GenericKubernetesResource.class);
        CustomResourceDefinitionContext crdContext = getCRDContext(resource.getApiVersion(), resource.getKind());
        knCli.createCustomResource(crdContext, resource);
    }

    private static CustomResourceDefinitionContext getCRDContext(String apiVersion, String kind) throws IOException {
        if (kind == null) {
            return null;
        }
        String plural = Pluralize.toPlural(kind.toLowerCase(Locale.ROOT));
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
}
