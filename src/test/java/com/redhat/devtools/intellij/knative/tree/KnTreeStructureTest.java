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
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.knative.BaseTest;
import com.redhat.devtools.intellij.knative.kn.PingSource;
import com.redhat.devtools.intellij.knative.kn.Revision;
import com.redhat.devtools.intellij.knative.kn.Service;
import com.redhat.devtools.intellij.knative.kn.Source;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KnTreeStructureTest extends BaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        knTreeStructure = mock(KnTreeStructure.class, org.mockito.Mockito.CALLS_REAL_METHODS);

        Field rootField = KnTreeStructure.class.getDeclaredField("root");
        rootField.setAccessible(true);
        rootField.set(knTreeStructure, knRootNode);

        when(knRootNode.getKn()).thenReturn(kn);
        when(knServingNode.getRootNode()).thenReturn(knRootNode);
        when(knServiceNode.getRootNode()).thenReturn(knRootNode);
        when(knServiceNode.getParent()).thenReturn(knServingNode);
        when(knEventingSourcesNode.getRootNode()).thenReturn(knRootNode);
    }

    @Test
    public void GetChildElements_ElementIsRootWithNoKnativeInstalled_EmptyArray() throws IOException {
        Object[] nodes = getChildElements(false, false);

        assertTrue(nodes.length == 0);
    }

    @Test
    public void GetChildElements_ElementIsRootWithOnlyKnativeServing_ArrayWithServing() throws IOException {
        Object[] nodes = getChildElements(true, false);

        assertTrue(nodes.length == 1);
        assertNotNull(nodes[0]);
        assertTrue(nodes[0] instanceof KnServingNode);
    }

    @Test
    public void GetChildElements_ElementIsRootWithOnlyKnativeEventing_ArrayWithEventing() throws IOException {
        Object[] nodes = getChildElements(false, true);

        assertTrue(nodes.length == 1);
        assertNotNull(nodes[0]);
        assertTrue(nodes[0] instanceof KnEventingNode);
    }

    @Test
    public void GetChildElements_ElementIsRootWithKnativeServingAndEventing_ArrayWithServingAndEventing() throws IOException {
        Object[] nodes = getChildElements(true, true);

        assertTrue(nodes.length == 2);
        assertNotNull(nodes[0]);
        assertTrue(nodes[0] instanceof KnServingNode);
        assertNotNull(nodes[1]);
        assertTrue(nodes[1] instanceof KnEventingNode);
    }

    private Object[] getChildElements(boolean hasKnativeServing, boolean hasKnativeEventing) throws IOException {
        when(kn.isKnativeServingAware()).thenReturn(hasKnativeServing);
        when(kn.isKnativeEventingAware()).thenReturn(hasKnativeEventing);
        return knTreeStructure.getChildElements(knRootNode);
    }

    @Test
    public void GetChildElements_ElementIsServingNodeWithNoChildren_HasMessageNode() throws IOException {
        when(kn.getServicesList()).thenReturn(Collections.emptyList());
        Object[] serviceNodes = knTreeStructure.getChildElements(knServingNode);

        assertEquals(0, serviceNodes.length);
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
    public void GetChildElements_ElementIsKnEventingNode_ContainsAllEventingNodes() throws IOException {
        Object[] eventingNodes = knTreeStructure.getChildElements(knEventingNode);
        assertEquals(5, eventingNodes.length);
    }

    @Test
    public void GetChildElements_ElementIsEventingSourcesNodeWithNoChildren_EmptyArray() throws IOException {
        when(kn.getSources()).thenReturn(Collections.emptyList());
        Object[] eventingSources = knTreeStructure.getChildElements(knEventingSourcesNode);

        assertEquals(0, eventingSources.length);
    }

    @Test
    public void GetChildElements_ElementIsEventingSourcesNodeWithChildren_ArrayOfSources() throws IOException {
        Source source1 = mock(Source.class);
        Source source2 = mock(Source.class);
        Source source3 = mock(Source.class);
        List<Source> sources = new ArrayList<>(Arrays.asList(source1, source2, source3));

        when(kn.getSources()).thenReturn(sources);

        Object[] revisionNodes = knTreeStructure.getChildElements(knEventingSourcesNode);

        assertEquals(3, revisionNodes.length);
    }

    @Test
    public void GetChildElements_ElementIsSourceNodeNodeWithNoChildren_EmptyArray() throws IOException {
        Source source = mock(Source.class);
        when(knSourceNode.getSource()).thenReturn(source);
        when(source.getSinkSource()).thenReturn(null);

        Object[] eventingSources = knTreeStructure.getChildElements(knSourceNode);

        assertEquals(0, eventingSources.length);
    }

    @Test
    public void GetChildElements_ElementIsKnSourceNodeWithChildren_HasSource() throws IOException {
        Source source = mock(Source.class);
        PingSource pingSource = mock(PingSource.class);
        when(knSourceNode.getSource()).thenReturn(source);
        when(source.getSinkSource()).thenReturn(pingSource);

        Object[] eventingSources = knTreeStructure.getChildElements(knSourceNode);

        assertEquals(1, eventingSources.length);
    }

    @Test
    public void GetParentElement_UnknownNode_Null() {
        assertNull(knTreeStructure.getParentElement(knRootNode));
    }

    @Test
    public void GetParentElement_ParentableNode_Parent() {
        assertEquals(knServingNode, knTreeStructure.getParentElement(knServiceNode));
    }

    @Test(expected = RuntimeException.class)
    public void GetDescriptor_ElementIsUnknownType_Null() {
        knTreeStructure.createDescriptor(new Object(), null);
    }

    @Test
    public void GetDescriptor_ElementIsKnRoot_LabelAndIconDescriptor() {
        assertNodeDescriptor(knRootNode, LabelAndIconDescriptor.class);
    }

    @Test
    public void GetDescriptor_ElementIsKnServing_LabelAndIconDescriptor() {
        assertNodeDescriptor(knServingNode, LabelAndIconDescriptor.class);
    }

    @Test
    public void GetDescriptor_ElementIsKnEventing_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingNode, LabelAndIconDescriptor.class);
    }

    @Test
    public void GetDescriptor_ElementIsKnServiceNode_KnServiceDescriptor() {
        assertNodeDescriptor(knServiceNode, KnServiceDescriptor.class);
    }

    @Test
    public void GetDescriptor_ElementIsKnRevisionNode_KnRevisionDescriptor() {
        assertNodeDescriptor(knRevisionNode, KnRevisionDescriptor.class);
    }

    @Test
    public void GetDescriptor_ElementIsKnEventingBrokerNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingBrokerNode, LabelAndIconDescriptor.class);
    }

    @Test
    public void GetDescriptor_ElementIsKnEventingChannelsNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingChannelsNode, LabelAndIconDescriptor.class);
    }

    @Test
    public void GetDescriptor_ElementIsKnEventingSourcesNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingSourcesNode, LabelAndIconDescriptor.class);
    }

    @Test
    public void GetDescriptor_ElementIsKnEventingSubscriptionsNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingSubscriptionsNode, LabelAndIconDescriptor.class);
    }

    @Test
    public void GetDescriptor_ElementIsKnEventingTriggersNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingTriggersNode, LabelAndIconDescriptor.class);
    }

    @Test
    public void GetDescriptor_ElementIsKnSourceNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knSourceNode, LabelAndIconDescriptor.class);
    }

    @Test
    public void GetDescriptor_ElementIsKnSinkNode_KnSinkDescriptor() {
        assertNodeDescriptor(knSinkNode, KnSinkDescriptor.class);
    }


    private void assertNodeDescriptor(Object element, Class nodeDescriptorType) {
        NodeDescriptor nodeDescriptor = knTreeStructure.createDescriptor(element, null);
        assertTrue(nodeDescriptor.getClass().equals(nodeDescriptorType));
        assertEquals(element, nodeDescriptor.getElement());
    }
}
