/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.tree;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.redhat.devtools.intellij.knative.BaseTest;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class KnSinkDescriptorTest extends BaseTest {

    @Mock
    private NodeDescriptor<?> parentDescriptor;

    public void testUpdate_with_missing_sink_should_add_error_message() {
        when(knSinkNode.getName()).thenReturn(null);
        KnSinkDescriptor descriptor = new KnSinkDescriptor(project, knSinkNode, parentDescriptor);
        PresentationData presentationData = new PresentationData();
        descriptor.update(presentationData);

        assertEquals("Sink Not Found", presentationData.getColoredText().get(0).getText());
    }

    public void testUpdate_should_use_sink_name() {
        when(knSinkNode.getName()).thenReturn("Foo");
        KnSinkDescriptor descriptor = new KnSinkDescriptor(project, knSinkNode, parentDescriptor);
        PresentationData presentationData = new PresentationData();
        descriptor.update(presentationData);

        assertEquals("Foo", presentationData.getPresentableText());
    }
}
