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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.DeployModel;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
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

    public static void saveOnCluster(Project project, String yaml) throws IOException {
        DeployModel deployModel = buildDeployModel(yaml);

        Kn knCli = TreeHelper.getKn(project);
        if (knCli == null) {
            throw new IOException("Unable to save the resource to the cluster. Internal error, please retry or restart the IDE.");
        }

        knCli.createCustomResource(deployModel.getCrdContext(), yaml);
        save(knCli, yaml);
    }

    private static void save(Kn knCli, String yaml) throws IOException {
        DeployModel deployModel = buildDeployModel(yaml);

        Map<String, Object> resource = knCli.getCustomResource(deployModel.getName(), deployModel.getCrdContext());
        if (resource == null) {
            knCli.createCustomResource(deployModel.getCrdContext(), yaml);
        } else {
            JsonNode customResource = JSONHelper.MapToJSON(resource);
            ((ObjectNode) customResource).set("spec", deployModel.getSpec());
            knCli.editCustomResource(deployModel.getName(), deployModel.getCrdContext(), customResource.toString());
        }
    }

    private static DeployModel buildDeployModel(String yaml) throws IOException {
        String kind = YAMLHelper.getStringValueFromYAML(yaml, new String[] {"kind"});
        if (Strings.isNullOrEmpty(kind)) {
            throw new IOException("Knative configuration has not a valid format. Kind field is not found.");
        }
        String name = YAMLHelper.getStringValueFromYAML(yaml, new String[] {"metadata", "name"});
        String generateName = YAMLHelper.getStringValueFromYAML(yaml, new String[] {"metadata", "generateName"});
        if (Strings.isNullOrEmpty(name) && Strings.isNullOrEmpty(generateName)) {
            throw new IOException("Knative " + kind + " has not a valid format. Name field is not valid or found.");
        }
        String apiVersion = YAMLHelper.getStringValueFromYAML(yaml, new String[] {"apiVersion"});
        if (Strings.isNullOrEmpty(apiVersion)) {
            throw new IOException("Knative " + kind + " has not a valid format. ApiVersion field is not found.");
        }
        CustomResourceDefinitionContext crdContext = getCRDContext(apiVersion, getPluralByKind(kind));
        if (crdContext == null) {
            throw new IOException("Knative " + kind + " has not a valid format. ApiVersion field contains an invalid value.");
        }
        JsonNode spec = YAMLHelper.getValueFromYAML(yaml, new String[] {"spec"});
        if (spec == null) {
            throw new IOException("Knative " + kind + " has not a valid format. Spec field is not found.");
        }
        return new DeployModel(name, kind, apiVersion, spec, crdContext);
    }

    private static CustomResourceDefinitionContext getCRDContext(String apiVersion, String plural) {
        if (Strings.isNullOrEmpty(apiVersion) || Strings.isNullOrEmpty(plural)) return null;
        String[] groupVersion = apiVersion.split("/");
        if (groupVersion.length != 2) {
            return null;
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

    private static String getPluralByKind(String kind) {
        return kind.toLowerCase() + "s";
    }

    public static boolean isWritable(ParentableNode node) {
        return node instanceof KnServiceNode;
    }
}
