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

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.FixtureBaseTest;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class KnHelperTest extends FixtureBaseTest {

    private static final String RESOURCE_PATH = "utils/knhelper/";
    private static final String YAML_CONTENT = "test";

    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(knRootNode.getKn()).thenReturn(kn);
    }

    @Test
    public void GetYamlFromNode_NodeIsKnServiceNode_Content() throws IOException {
        when(knServiceNode.getRootNode()).thenReturn(knRootNode);
        when(kn.getServiceYAML(any())).thenReturn(YAML_CONTENT);
        assertIsExpectedYAML(knServiceNode, YAML_CONTENT);
    }

    @Test
    public void GetYamlFromNode_NodeIsKnRevisionNode_Content() throws IOException {
        when(knRevisionNode.getRootNode()).thenReturn(knRootNode);
        when(kn.getRevisionYAML(any())).thenReturn(YAML_CONTENT);
        assertIsExpectedYAML(knRevisionNode, YAML_CONTENT);
    }

    @Test
    public void GetYamlFromNode_NodeIsUnknownType_EmptyContent() throws IOException {
        when(parentableNode.getRootNode()).thenReturn(knRootNode);
        assertIsExpectedYAML(parentableNode, "");
    }

    private void assertIsExpectedYAML(ParentableNode node, String expectedYaml) throws IOException {
        String resultingYaml = KnHelper.getYamlFromNode(node);
        assertEquals(expectedYaml, resultingYaml);
    }

    @Test
    public void SaveOnCluster_IsCreateIsTrue_IsSaveConfirmedNotCalled() {
        try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
            KnHelper.saveOnCluster(project, "test", true);
            uiHelperMockedStatic.verify(() -> UIHelper.executeInUI(any(Runnable.class)), times(0));
        } catch (IOException e) { }
    }

    @Test
    public void SaveOnCluster_ProjectIsNullAndSavedConfirmed_Throws() {
        assertIsCorrectErrorWhenInvalidYaml(null, null, "", true, "Unable to save the resource to the cluster. Internal error, please retry or restart the IDE.");
    }

    @Test
    public void SaveOnCluster_InvalidYamlMissingKind_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingkind_service.yaml");
        assertIsCorrectErrorWhenInvalidYaml(kn, project, yaml, true, "Knative configuration has not a valid format. Kind field is not found.");
    }

    @Test
    public void SaveOnCluster_InvalidYamlMissingName_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingname_service.yaml");
        assertIsCorrectErrorWhenInvalidYaml(kn, project, yaml, true, "Knative service has not a valid format. Name field is not valid or found.");
    }

    @Test
    public void SaveOnCluster_InvalidYamlMissingApiVersion_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingapiversion_service.yaml");
        assertIsCorrectErrorWhenInvalidYaml(kn, project, yaml, true, "Knative service has not a valid format. ApiVersion field is not found.");
    }

    @Test
    public void SaveOnCluster_InvalidYamlMissingCRD_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingcrd_service.yaml");
        assertIsCorrectErrorWhenInvalidYaml(kn, project, yaml, true, "Knative service has not a valid format. ApiVersion field contains an invalid value.");
    }

    @Test
    public void SaveOnCluster_InvalidYamlMissingSpec_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingspec_service.yaml");
        assertIsCorrectErrorWhenInvalidYaml(kn, project, yaml, true, "Knative service has not a valid format. Spec field is not found.");
    }

    private void assertIsCorrectErrorWhenInvalidYaml(Kn kn, Project project, String yaml, boolean isCreate, String expectedError) {
        try (MockedStatic<TreeHelper> theMock = mockStatic(TreeHelper.class)) {
            theMock.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            try {
                KnHelper.saveOnCluster(project, yaml, isCreate);
            } catch (IOException e) {
                assertEquals(expectedError, e.getLocalizedMessage());
            }
        }
    }

    @Test
    public void SaveOnCluster_SaveNewSucceed_True() throws IOException {
        String yaml = load(RESOURCE_PATH + "service.yaml");
        doAnswer((a) -> true).when(kn).createCustomResource(any(CustomResourceDefinitionContext.class), anyString());
        try (MockedStatic<TreeHelper> theMock = mockStatic(TreeHelper.class)) {
            theMock.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            try {
                boolean result = KnHelper.saveOnCluster(project, yaml, true);
                assertTrue(result);
            } catch (IOException e) {
            }
        }
    }

    @Test
    public void GetPluralByKind_WordEndingWithS_PluralWithES() {
        assertEquals("buses", KnHelper.getPluralByKind("bus"));
    }

    @Test
    public void GetPluralByKind_WordEndingWithY_PluralWithIES() {
        assertEquals("cities", KnHelper.getPluralByKind("city"));
    }

    @Test
    public void GetPluralByKind_WordNotEndingWithSorY_PluralWithS() {
        assertEquals("services", KnHelper.getPluralByKind("service"));
    }

    @Test
    public void IsWritable_NodeIsService_True() {
       assertTrue(KnHelper.isWritable(knServiceNode));
    }

    @Test
    public void IsWritable_NodeIsNotAService_False() {
        assertFalse(KnHelper.isWritable(knRevisionNode));
    }
}
