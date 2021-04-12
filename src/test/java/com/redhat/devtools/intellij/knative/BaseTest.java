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
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.KnCli;
import com.redhat.devtools.intellij.knative.listener.KnSaveInEditorListener;
import com.redhat.devtools.intellij.knative.tree.KnEventingNode;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.KnServingNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Before;


import static org.mockito.Mockito.mock;

public class BaseTest {

    protected Project project;
    protected Kn kn;
    protected ParentableNode parentableNode;
    protected KnRootNode knRootNode;
    protected KnServingNode knServingNode;
    protected KnServiceNode knServiceNode;
    protected KnEventingNode knEventingNode;
    protected KnRevisionNode knRevisionNode;
    protected KnTreeStructure knTreeStructure;

    @Before
    public void setUp() throws Exception {
        project = mock(Project.class);
        kn = mock(KnCli.class);
        parentableNode = mock(ParentableNode.class);
        knRootNode = mock(KnRootNode.class);
        knServingNode = mock(KnServingNode.class);
        knServiceNode = mock(KnServiceNode.class);
        knEventingNode = mock(KnEventingNode.class);
        knRevisionNode = mock(KnRevisionNode.class);
        knTreeStructure = mock(KnTreeStructure.class);
    }

    protected String load(String name) throws IOException {
        return IOUtils.toString(BaseTest.class.getResource("/" + name), StandardCharsets.UTF_8);
    }
}