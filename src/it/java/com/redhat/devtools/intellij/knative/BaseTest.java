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
package com.redhat.devtools.intellij.knative;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.KnCliFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;

public class BaseTest {

    private CodeInsightTestFixture myFixture;
    protected Project project;
    private TestDialog previousTestDialog;
    protected Kn kn;

    @Before
    public void setUp() throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder();
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture);
        myFixture.setUp();
        project = myFixture.getProject();
        previousTestDialog = Messages.setTestDialog(message -> 0);
        kn = KnCliFactory.getInstance().getKn(project).get();
    }

    @After
    public void tearDown() throws Exception {
        Messages.setTestDialog(previousTestDialog);
        myFixture.tearDown();
    }

    protected String load(String name) throws IOException {
        return IOUtils.toString(BaseTest.class.getResource("/" + name), StandardCharsets.UTF_8);
    }
}
