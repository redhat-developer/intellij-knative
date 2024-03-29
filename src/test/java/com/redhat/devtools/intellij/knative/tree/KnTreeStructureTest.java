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

    public void setUp() throws Exception {
        super.setUp();
        knTreeStructure = mock(KnTreeStructure.class, org.mockito.Mockito.CALLS_REAL_METHODS);

        Field rootField = AbstractKnTreeStructure.class.getDeclaredField("root");
        rootField.setAccessible(true);
        rootField.set(knTreeStructure, knRootNode);

        when(knRootNode.getKn()).thenReturn(kn);
        when(knServingNode.getRootNode()).thenReturn(knRootNode);
        when(knServiceNode.getRootNode()).thenReturn(knRootNode);
        when(knServiceNode.getParent()).thenReturn(knServingNode);
        when(knEventingSourcesNode.getRootNode()).thenReturn(knRootNode);
    }

    public void testGetChildElements_ElementIsRootWithNoKnativeInstalled_EmptyArray() throws IOException {
        Object[] nodes = getChildElements(false, false);

        assertTrue(nodes.length == 0);
    }

    public void testGetChildElements_ElementIsRootWithOnlyKnativeServing_ArrayWithServing() throws IOException {
        Object[] nodes = getChildElements(true, false);

        assertTrue(nodes.length == 1);
        assertNotNull(nodes[0]);
        assertTrue(nodes[0] instanceof KnServingNode);
    }

    public void testGetChildElements_ElementIsRootWithOnlyKnativeEventing_ArrayWithEventing() throws IOException {
        Object[] nodes = getChildElements(false, true);

        assertTrue(nodes.length == 1);
        assertNotNull(nodes[0]);
        assertTrue(nodes[0] instanceof KnEventingNode);
    }

    public void testGetChildElements_ElementIsRootWithKnativeServingAndEventing_ArrayWithServingAndEventing() throws IOException {
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

    public void testGetChildElements_ElementIsServingNodeWithNoChildren_HasMessageNode() throws IOException {
        when(kn.getServicesList()).thenReturn(Collections.emptyList());
        Object[] serviceNodes = knTreeStructure.getChildElements(knServingNode);

        assertEquals(0, serviceNodes.length);
    }

    public void testGetChildElements_ElementIsServingNodeWithChildren_ArrayOfServices() throws IOException {
        Service service1 = mock(Service.class);
        Service service2 = mock(Service.class);
        Service service3 = mock(Service.class);
        List<Service> services = new ArrayList<>(Arrays.asList(service1, service2, service3));

        when(kn.getServicesList()).thenReturn(services);

        Object[] serviceNodes = knTreeStructure.getChildElements(knServingNode);

        assertTrue(serviceNodes.length == 3);
    }

    public void testGetChildElements_ElementIsServiceNodeWithNoChildren_EmptyArray() throws IOException {
        when(kn.getRevisionsForService(anyString())).thenReturn(Collections.emptyList());
        Object[] revisionNodes = knTreeStructure.getChildElements(knServiceNode);

        assertTrue(revisionNodes.length == 0);
    }

    public void testGetChildElements_ElementIsServiceNodeWithChildren_ArrayOfRevisions() throws IOException {
        Revision revision1 = mock(Revision.class);
        Revision revision2 = mock(Revision.class);
        Revision revision3 = mock(Revision.class);
        List<Revision> revisions = new ArrayList<>(Arrays.asList(revision1, revision2, revision3));

        when(kn.getRevisionsForService(any())).thenReturn(revisions);

        Object[] revisionNodes = knTreeStructure.getChildElements(knServiceNode);

        assertTrue(revisionNodes.length == 3);
    }

    public void testGetChildElements_ElementIsKnEventingNode_ContainsAllEventingNodes() throws IOException {
        Object[] eventingNodes = knTreeStructure.getChildElements(knEventingNode);
        assertEquals(5, eventingNodes.length);
    }

    public void testGetChildElements_ElementIsEventingSourcesNodeWithNoChildren_EmptyArray() throws IOException {
        when(kn.getSources()).thenReturn(Collections.emptyList());
        Object[] eventingSources = knTreeStructure.getChildElements(knEventingSourcesNode);

        assertEquals(0, eventingSources.length);
    }

    public void testGetChildElements_ElementIsEventingSourcesNodeWithChildren_ArrayOfSources() throws IOException {
        Source source1 = mock(Source.class);
        Source source2 = mock(Source.class);
        Source source3 = mock(Source.class);
        List<Source> sources = new ArrayList<>(Arrays.asList(source1, source2, source3));

        when(kn.getSources()).thenReturn(sources);

        Object[] revisionNodes = knTreeStructure.getChildElements(knEventingSourcesNode);

        assertEquals(3, revisionNodes.length);
    }

    public void testGetChildElements_ElementIsSourceNodeNodeWithNoChildren_EmptyArray() throws IOException {
        Source source = mock(Source.class);
        when(knSourceNode.getSource()).thenReturn(source);
        when(source.getSinkSource()).thenReturn(null);

        Object[] eventingSources = knTreeStructure.getChildElements(knSourceNode);

        assertEquals(0, eventingSources.length);
    }

    public void testGetChildElements_ElementIsKnSourceNodeWithChildren_HasSource() throws IOException {
        Source source = mock(Source.class);
        PingSource pingSource = mock(PingSource.class);
        when(knSourceNode.getSource()).thenReturn(source);
        when(source.getSinkSource()).thenReturn(pingSource);

        Object[] eventingSources = knTreeStructure.getChildElements(knSourceNode);

        assertEquals(1, eventingSources.length);
    }

    public void testGetParentElement_UnknownNode_Null() {
        assertNull(knTreeStructure.getParentElement(knRootNode));
    }

    public void testGetParentElement_ParentableNode_Parent() {
        assertEquals(knServingNode, knTreeStructure.getParentElement(knServiceNode));
    }

    public void testGetDescriptor_ElementIsUnknownType_Null() {
        try {
            knTreeStructure.createDescriptor(new Object(), null);
        } catch (RuntimeException e){
            assertEquals( "Can't find NodeDescriptor for java.lang.Object", e.getMessage() );
        }
    }

    public void testGetDescriptor_ElementIsKnRoot_LabelAndIconDescriptor() {
        assertNodeDescriptor(knRootNode, KnRootNodeDescriptor.class);
    }

    public void testGetDescriptor_ElementIsKnServing_LabelAndIconDescriptor() {
        assertNodeDescriptor(knServingNode, LabelAndIconDescriptor.class);
    }

    public void testGetDescriptor_ElementIsKnEventing_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingNode, LabelAndIconDescriptor.class);
    }

    public void testGetDescriptor_ElementIsKnServiceNode_KnServiceDescriptor() {
        assertNodeDescriptor(knServiceNode, KnServiceDescriptor.class);
    }

    public void testGetDescriptor_ElementIsKnRevisionNode_KnRevisionDescriptor() {
        assertNodeDescriptor(knRevisionNode, KnRevisionDescriptor.class);
    }

    public void testGetDescriptor_ElementIsKnEventingBrokerNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingBrokerNode, LabelAndIconDescriptor.class);
    }

    public void testGetDescriptor_ElementIsKnEventingChannelsNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingChannelsNode, LabelAndIconDescriptor.class);
    }

    public void testGetDescriptor_ElementIsKnEventingSourcesNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingSourcesNode, LabelAndIconDescriptor.class);
    }

    public void testGetDescriptor_ElementIsKnEventingSubscriptionsNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingSubscriptionsNode, LabelAndIconDescriptor.class);
    }

    public void testGetDescriptor_ElementIsKnEventingTriggersNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knEventingTriggersNode, LabelAndIconDescriptor.class);
    }

    public void testGetDescriptor_ElementIsKnSourceNode_LabelAndIconDescriptor() {
        assertNodeDescriptor(knSourceNode, LabelAndIconDescriptor.class);
    }

    public void testGetDescriptor_ElementIsKnSinkNode_KnSinkDescriptor() {
        assertNodeDescriptor(knSinkNode, KnSinkDescriptor.class);
    }


    private void assertNodeDescriptor(Object element, Class nodeDescriptorType) {
        NodeDescriptor nodeDescriptor = knTreeStructure.createDescriptor(element, null);
        assertTrue(nodeDescriptor.getClass().equals(nodeDescriptorType));
        assertEquals(element, nodeDescriptor.getElement());
    }
}
