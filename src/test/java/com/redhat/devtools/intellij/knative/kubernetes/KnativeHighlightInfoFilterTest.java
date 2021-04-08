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
package com.redhat.devtools.intellij.knative.kubernetes;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.redhat.devtools.intellij.knative.BaseTest;
import java.io.IOException;
import org.jetbrains.yaml.YAMLFileType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KnativeHighlightInfoFilterTest extends BaseTest {
    private static final String RESOURCE_PATH = "kubernetes/";
    private KnativeHighlightInfoFilter knativeHighlightInfoFilter;
    private CodeInsightTestFixture myFixture;

    @Before
    public void setUp() throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder();
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture);
        myFixture.setUp();
        knativeHighlightInfoFilter = new KnativeHighlightInfoFilter();
    }

    @After
    public void tearDown() throws Exception {
        myFixture.tearDown();
    }

    @Test
    public void IsCustomFile_PsiFileIsKnative_True() throws IOException {
        String yaml = load(RESOURCE_PATH + "service.yaml");
        PsiFile psiFile = myFixture.configureByText(YAMLFileType.YML, yaml);
        ApplicationManager.getApplication().runReadAction(() -> {
            boolean result = knativeHighlightInfoFilter.isCustomFile(psiFile);
            assertTrue(result);
        });
    }

    @Test
    public void IsCustomFile_PsiFileIsUnknown_False() throws IOException {
        String yaml = load(RESOURCE_PATH + "tekton.yaml");
        PsiFile psiFile = myFixture.configureByText(YAMLFileType.YML, yaml);
        ApplicationManager.getApplication().runReadAction(() -> {
            boolean result = knativeHighlightInfoFilter.isCustomFile(psiFile);
            assertFalse(result);
        });
    }
}
