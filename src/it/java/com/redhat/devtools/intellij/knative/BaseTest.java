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
import com.intellij.openapi.ui.TestDialog;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.KnCliFactory;
import com.redhat.devtools.intellij.common.utils.MessagesHelper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

public class BaseTest extends BasePlatformTestCase {

    protected Project project;
    private TestDialog previousTestDialog;
    private Kn kn;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        previousTestDialog = MessagesHelper.setTestDialog(message -> 0);
        kn = KnCliFactory.getInstance().getKn(project).get();
    }

    @Override
    public void tearDown() throws Exception {
        MessagesHelper.setTestDialog(previousTestDialog);
        super.tearDown();
    }

    protected String load(String name) throws IOException {
        return IOUtils.toString(BaseTest.class.getResource("/" + name), StandardCharsets.UTF_8);
    }

    protected Kn getKn(){
        return this.kn;
    }
}
