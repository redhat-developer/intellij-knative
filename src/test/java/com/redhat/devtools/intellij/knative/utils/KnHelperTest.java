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
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class KnHelperTest {

    private static final String YAML_CONTENT = "test";
    private CodeInsightTestFixture myFixture;
    private Project project;
    private KnServiceNode knServiceNode;
    private KnRevisionNode knRevisionNode;
    private ParentableNode parentableNode;
    private KnRootNode knRootNode;
    private Kn kn;

    @Before
    public void setUp() throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder();
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture);
        myFixture.setUp();
        project = myFixture.getProject();
        kn = mock(Kn.class);
        knRootNode = mock(KnRootNode.class);
        parentableNode = mock(ParentableNode.class);
        knServiceNode = mock(KnServiceNode.class);
        knRevisionNode = mock(KnRevisionNode.class);

        when(knRootNode.getKn()).thenReturn(kn);
    }

    @After
    public void tearDown() throws Exception {
        myFixture.tearDown();
    }

    @Test
    public void GetYamlFromNode_NodeIsKnServiceNode_Content() throws IOException {
        when(knServiceNode.getRootNode()).thenReturn(knRootNode);
        when(kn.getServiceYAML(any())).thenReturn(YAML_CONTENT);
        String resultingYaml = KnHelper.getYamlFromNode(knServiceNode);
        assertEquals(YAML_CONTENT, resultingYaml);
    }

    @Test
    public void GetYamlFromNode_NodeIsKnRevisionNode_Content() throws IOException {
        when(knRevisionNode.getRootNode()).thenReturn(knRootNode);
        when(kn.getRevisionYAML(any())).thenReturn(YAML_CONTENT);
        String resultingYaml = KnHelper.getYamlFromNode(knRevisionNode);
        assertEquals(YAML_CONTENT, resultingYaml);
    }

    @Test
    public void GetYamlFromNode_NodeIsUnknownType_EmptyContent() throws IOException {
        when(parentableNode.getRootNode()).thenReturn(knRootNode);
        String resultingYaml = KnHelper.getYamlFromNode(parentableNode);
        assertEquals("", resultingYaml);
    }

    @Test
    public void SaveOnCluster_SavedNotConfirmed_False() {

    }

    @Test
    public void SaveOnCluster_IsCreateIsTrue_IsSaveConfirmedNotCalled() {

    }

    @Test
    public void SaveOnCluster_ProjectIsNullAndSavedConfirmed_Throws() {
        try (MockedStatic<TreeHelper> theMock = mockStatic(TreeHelper.class)) {
            theMock.when(() -> TreeHelper.getKn(any())).thenReturn(null);
            try {
                KnHelper.saveOnCluster(null, "", true);
            } catch (IOException e) {
                assertEquals("Unable to save the resource to the cluster. Internal error, please retry or restart the IDE.", e.getLocalizedMessage());
            }
        }
    }

    @Test
    public void SaveOnCluster_IsCreateFalse_True() {

    }

    @Test
    public void SaveOnCluster_IsCreateTrue_True() throws Exception {
       /* try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
            PowerMockito.doNothing().when(KnHelper.class, "saveNew", kn, YAML_CONTENT);
            boolean result = KnHelper.saveOnCluster(project, "", true);
            assertEquals(true, result);
        }*/
    }
}
