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
package com.redhat.devtools.intellij.knative.tree;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.KnCliFactory;
import com.redhat.devtools.intellij.knative.utils.KnHelper;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KnRootTest {

    private Project project;
    private Kn kn;

    @Before
    public void before() {
        project = mock(Project.class);
        kn = mock(Kn.class);
    }

    @Test
    public void Constructor_KnRootNode() {
        KnRootNode knRootNode = new KnRootNode(project);
        assertNull(knRootNode.getKn());
        assertEquals(project, knRootNode.getProject());

        KnCliFactory knCliFactory = mock(KnCliFactory.class);
        CompletableFuture completableFuture = new CompletableFuture<>();
        completableFuture.complete(kn);
        when(knCliFactory.getKn(any())).thenReturn(completableFuture);
        try (MockedStatic<KnCliFactory> knCliFactoryMockedStatic = mockStatic(KnCliFactory.class)) {
            knCliFactoryMockedStatic.when(() -> KnCliFactory.getInstance()).thenReturn(knCliFactory);
            knRootNode.initializeKn();
            assertEquals(kn, knRootNode.getKn());
            verify(knCliFactory, never()).resetKn();
            knRootNode.load();
            verify(knCliFactory).resetKn();
        }
    }
}
