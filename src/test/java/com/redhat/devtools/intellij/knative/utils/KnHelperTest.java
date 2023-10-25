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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.BaseTest;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KnHelperTest extends BaseTest {

    private static final String RESOURCE_PATH = "utils/knhelper/";
    private static final String YAML_CONTENT = "test";

    public void setUp() throws Exception {
        super.setUp();

        when(knRootNode.getKn()).thenReturn(kn);
    }

    public void testGetYamlFromNode_NodeIsKnServiceNode_Content() throws IOException {
        when(knServiceNode.getRootNode()).thenReturn(knRootNode);
        when(kn.getServiceYAML(any())).thenReturn(YAML_CONTENT);
        assertIsExpectedYAML(knServiceNode, YAML_CONTENT);
    }

    public void testGetYamlFromNode_NodeIsKnRevisionNode_Content() throws IOException {
        when(knRevisionNode.getRootNode()).thenReturn(knRootNode);
        when(kn.getRevisionYAML(any())).thenReturn(YAML_CONTENT);
        assertIsExpectedYAML(knRevisionNode, YAML_CONTENT);
    }

    public void testGetYamlFromNode_NodeIsUnknownType_EmptyContent() throws IOException {
        when(parentableNode.getRootNode()).thenReturn(knRootNode);
        assertIsExpectedYAML(parentableNode, "");
    }

    private void assertIsExpectedYAML(ParentableNode node, String expectedYaml) throws IOException {
        String resultingYaml = KnHelper.getYamlFromNode(node);
        assertEquals(expectedYaml, resultingYaml);
    }


    public void testSaveOnCluster_IsCreateIsTrue_IsSaveConfirmedNotCalled() {
        try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
            KnHelper.saveOnCluster(project, "test", true);
            uiHelperMockedStatic.verify(() -> UIHelper.executeInUI(any(Runnable.class)), times(0));
        } catch (IOException ignored) { } // fails with "Unable to save the resource to the cluster. Internal error, please retry or restart the IDE."
    }

    public void testSaveOnCluster_ProjectIsNullAndSavedConfirmed_Throws() {
        assertIsCorrectErrorWhenInvalidYaml(null, null, "", true, "Unable to save the resource to the cluster. Internal error, please retry or restart the IDE.");
    }

    public void testSaveOnCluster_InvalidYamlMissingKind_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingkind_service.yaml");
        assertIsCorrectErrorWhenInvalidYaml(kn, project, yaml, true, "Resource configuration not valid. Resource kind is missing or invalid.");
    }

    public void testSaveOnCluster_InvalidYamlMissingName_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingname_service.yaml");
        assertIsCorrectErrorWhenInvalidYaml(kn, project, yaml, true, "Resource configuration not valid. Resource name is missing or invalid.");
    }

    public void testSaveOnCluster_InvalidYamlMissingApiVersion_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingapiversion_service.yaml");
        assertIsCorrectErrorWhenInvalidYaml(kn, project, yaml, true, "Resource configuration not valid. ApiVersion is missing or invalid.");
    }

    public void testSaveOnCluster_InvalidYamlMissingCRD_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingcrd_service.yaml");
        assertIsCorrectErrorWhenInvalidYaml(kn, project, yaml, true, "Knative service has not a valid format. ApiVersion field contains an invalid value.");
    }

    public void testSaveOnCluster_InvalidYamlMissingSpec_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingspec_service.yaml");
        assertIsCorrectErrorWhenInvalidYaml(kn, project, yaml, true, "Resource configuration not valid. Spec field is missing or invalid.");
    }

    private void assertIsCorrectErrorWhenInvalidYaml(Kn kn, Project project, String yaml, boolean isCreate, String expectedError) {
        try (MockedStatic<TreeHelper> theMock = mockStatic(TreeHelper.class)) {
            theMock.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            try {
                KnHelper.saveOnCluster(project, yaml, isCreate);
            } catch (IOException e) {
                assertTrue(e.getLocalizedMessage().contains(expectedError));
            }
        }
    }

    public void testSaveOnCluster_SaveNewSucceed_True() throws IOException {
        String yaml = load(RESOURCE_PATH + "service.yaml");
        doAnswer((a) -> true).when(kn).createCustomResource(any(CustomResourceDefinitionContext.class), anyString());
        try (MockedStatic<TreeHelper> theMock = mockStatic(TreeHelper.class)) {
            theMock.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            try {
                boolean result = KnHelper.saveOnCluster(project, yaml, true);
                verify(kn, times(1)).createCustomResource(any(), any(GenericKubernetesResource.class));
                assertTrue(result);
            } catch (IOException e) {
            }
        }
    }

    public void testSaveOnCluster_IsCreateIsFalseButResourceIsNew_CreateMethodCalled() throws IOException {
        String yaml = load(RESOURCE_PATH + "service.yaml");
        doAnswer((a) -> true).when(kn).createCustomResource(any(CustomResourceDefinitionContext.class), anyString());
        when(kn.getCustomResource(anyString(), any())).thenReturn(null);
        try (MockedStatic<TreeHelper> theMock = mockStatic(TreeHelper.class)) {
            try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
                uiHelperMockedStatic.when(() -> UIHelper.executeInUI(any(Supplier.class))).thenReturn(Messages.OK);
                theMock.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
                try {
                    KnHelper.saveOnCluster(project, yaml, false);
                    verify(kn, times(1)).createCustomResource(any(), any(GenericKubernetesResource.class));
                } catch (IOException e) {
                }
            }
        }
    }

    public void testSaveOnCluster_IsCreateIsFalseAndResourceAlreadyExists_UpdateMethodCalled() throws IOException {
        ObjectMapper YAML_MAPPER = new ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
        String yaml = load(RESOURCE_PATH + "service.yaml");
        doAnswer((a) -> true).when(kn).createCustomResource(any(CustomResourceDefinitionContext.class), anyString());
        GenericKubernetesResource resource = Serialization.unmarshal(yaml, GenericKubernetesResource.class);
        when(kn.getCustomResource(anyString(), any())).thenReturn(resource);
        try (MockedStatic<TreeHelper> theMock = mockStatic(TreeHelper.class)) {
            try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
                uiHelperMockedStatic.when(() -> UIHelper.executeInUI(any(Supplier.class))).thenReturn(Messages.OK);
                theMock.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
                try {
                    KnHelper.saveOnCluster(project, yaml, false);
                    verify(kn, times(1)).editCustomResource(anyString(), any(), anyString());
                } catch (IOException e) {
                }
            }
        }
    }

    public void testIsWritable_NodeIsService_True() {
       assertTrue(KnHelper.isWritable(knServiceNode));
    }

    public void testIsWritable_NodeIsNotAService_False() {
        assertFalse(KnHelper.isWritable(knRevisionNode));
    }
}
