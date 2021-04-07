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

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.Revision;
import com.redhat.devtools.intellij.knative.kn.Service;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class KnTreeStructureTest {

    private KnTreeStructure knTreeStructure;
    private KnRootNode knRootNode;
    private Project project;
    private Kn kn;
    private KnServingNode knServingNode;
    private KnServiceNode knServiceNode;
    private KnEventingNode knEventingNode;
    private KnRevisionNode knRevisionNode;

    @Before
    public void before() throws Exception {
        project = mock(Project.class);
        knTreeStructure = mock(KnTreeStructure.class, org.mockito.Mockito.CALLS_REAL_METHODS);
        knRootNode = spy(new KnRootNode(project));
        knServingNode = mock(KnServingNode.class);
        knServiceNode = mock(KnServiceNode.class);
        knEventingNode = mock(KnEventingNode.class);
        knRevisionNode = mock(KnRevisionNode.class);

        kn = mock(Kn.class);

        Field rootField = KnTreeStructure.class.getDeclaredField("root");
        rootField.setAccessible(true);
        rootField.set(knTreeStructure, knRootNode);

        when(knRootNode.getKn()).thenReturn(kn);
        when(knServingNode.getRootNode()).thenReturn(knRootNode);
        when(knServiceNode.getRootNode()).thenReturn(knRootNode);
        when(knServiceNode.getParent()).thenReturn(knServingNode);
    }

    @Test
    public void GetChildElements_ElementIsRootWithNoKnative_EmptyArray() throws IOException {
        when(kn.isKnativeServingAware()).thenReturn(false);
        when(kn.isKnativeEventingAware()).thenReturn(false);
        Object[] nodes = knTreeStructure.getChildElements(knRootNode);

        assertTrue(nodes.length == 0);
    }

    @Test
    public void GetChildElements_ElementIsRootWithKnativeServing_ArrayWithServing() throws IOException {
        when(kn.isKnativeServingAware()).thenReturn(true);
        when(kn.isKnativeEventingAware()).thenReturn(false);
        Object[] nodes = knTreeStructure.getChildElements(knRootNode);

        assertTrue(nodes.length == 1);
        assertNotNull(nodes[0]);
        assertTrue(nodes[0] instanceof KnServingNode);
    }

    @Test
    public void GetChildElements_ElementIsRootWithKnativeEventing_ArrayWithEventing() throws IOException {
        when(kn.isKnativeServingAware()).thenReturn(false);
        when(kn.isKnativeEventingAware()).thenReturn(true);
        Object[] nodes = knTreeStructure.getChildElements(knRootNode);

        assertTrue(nodes.length == 1);
        assertNotNull(nodes[0]);
        assertTrue(nodes[0] instanceof KnEventingNode);
    }

    @Test
    public void GetChildElements_ElementIsRootWithKnativeServingAndEventing_ArrayWithServingAndEventing() throws IOException {
        when(kn.isKnativeServingAware()).thenReturn(true);
        when(kn.isKnativeEventingAware()).thenReturn(true);
        Object[] nodes = knTreeStructure.getChildElements(knRootNode);

        assertTrue(nodes.length == 2);
        assertNotNull(nodes[0]);
        assertTrue(nodes[0] instanceof KnServingNode);
        assertNotNull(nodes[1]);
        assertTrue(nodes[1] instanceof KnEventingNode);
    }

    @Test
    public void GetChildElements_ElementIsServingNodeWithNoChildren_EmptyArray() throws IOException {
        when(kn.getServicesList()).thenReturn(Collections.emptyList());
        Object[] serviceNodes = knTreeStructure.getChildElements(knServingNode);

        assertTrue(serviceNodes.length == 0);
    }

    @Test
    public void GetChildElements_ElementIsServingNodeWithChildren_ArrayOfServices() throws IOException {
        Service service1 = mock(Service.class);
        Service service2 = mock(Service.class);
        Service service3 = mock(Service.class);
        List<Service> services = new ArrayList<>(Arrays.asList(service1, service2, service3));

        when(kn.getServicesList()).thenReturn(services);

        Object[] serviceNodes = knTreeStructure.getChildElements(knServingNode);

        assertTrue(serviceNodes.length == 3);
    }

    @Test
    public void GetChildElements_ElementIsServiceNodeWithNoChildren_EmptyArray() throws IOException {
        when(kn.getRevisionsForService(anyString())).thenReturn(Collections.emptyList());
        Object[] revisionNodes = knTreeStructure.getChildElements(knServiceNode);

        assertTrue(revisionNodes.length == 0);
    }

    @Test
    public void GetChildElements_ElementIsServiceNodeWithChildren_ArrayOfRevisions() throws IOException {
        Revision revision1 = mock(Revision.class);
        Revision revision2 = mock(Revision.class);
        Revision revision3 = mock(Revision.class);
        List<Revision> revisions = new ArrayList<>(Arrays.asList(revision1, revision2, revision3));

        when(kn.getRevisionsForService(any())).thenReturn(revisions);

        Object[] revisionNodes = knTreeStructure.getChildElements(knServiceNode);

        assertTrue(revisionNodes.length == 3);
    }

    @Test
    public void GetParentElement_UnknownNode_Null() {
        assertNull(knTreeStructure.getParentElement(knRootNode));
    }

    @Test
    public void GetParentElement_ParentableNode_Parent() {
        assertEquals(knServingNode, knTreeStructure.getParentElement(knServiceNode));
    }

    @Test
    public void GetDescriptor_ElementIsUnknownType_Null() {
        assertNull(knTreeStructure.createDescriptor(new Object(), null));
    }

    @Test
    public void GetDescriptor_ElementIsKnRoot_LabelAndIconDescriptor() {
        NodeDescriptor nodeDescriptor = knTreeStructure.createDescriptor(knRootNode, null);
        assertTrue(nodeDescriptor instanceof LabelAndIconDescriptor);
        assertEquals(knRootNode, nodeDescriptor.getElement());
    }

    @Test
    public void GetDescriptor_ElementIsKnServing_LabelAndIconDescriptor() {
        NodeDescriptor nodeDescriptor = knTreeStructure.createDescriptor(knServingNode, null);
        assertTrue(nodeDescriptor instanceof LabelAndIconDescriptor);
        assertEquals(knServingNode, nodeDescriptor.getElement());
    }

    @Test
    public void GetDescriptor_ElementIsKnEventing_LabelAndIconDescriptor() {
        NodeDescriptor nodeDescriptor = knTreeStructure.createDescriptor(knEventingNode, null);
        assertTrue(nodeDescriptor instanceof LabelAndIconDescriptor);
        assertEquals(knEventingNode, nodeDescriptor.getElement());
    }

    @Test
    public void GetDescriptor_ElementIsKnServiceNode_KnServiceDescriptor() {
        NodeDescriptor nodeDescriptor = knTreeStructure.createDescriptor(knServiceNode, null);
        assertTrue(nodeDescriptor instanceof KnServiceDescriptor);
        assertEquals(knServiceNode, nodeDescriptor.getElement());
    }

    @Test
    public void GetDescriptor_ElementIsKnRevisionNode_KnRevisionDescriptor() {
        NodeDescriptor nodeDescriptor = knTreeStructure.createDescriptor(knRevisionNode, null);
        assertTrue(nodeDescriptor instanceof KnRevisionDescriptor);
        assertEquals(knRevisionNode, nodeDescriptor.getElement());
    }
}
