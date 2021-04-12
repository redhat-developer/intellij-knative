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

import com.intellij.icons.AllIcons;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.knative.BaseTest;
import com.redhat.devtools.intellij.knative.kn.KnConstants;
import com.redhat.devtools.intellij.knative.kn.Revision;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class KnNodeComparatorTest extends BaseTest {

    private KnNodeComparator knNodeComparator;
    private KnRevisionDescriptor knRevisionDescriptor, knRevisionDescriptor1, knRevisionDescriptor2;
    private LabelAndIconDescriptor labelAndIconDescriptor;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        knNodeComparator = new KnNodeComparator();

        Map<String, String> labels = new HashMap<>();
        labels.put(KnConstants.CONFIGURATION_GENERATION, "1");
        Revision revision = new Revision("revision", Collections.emptyList(), Collections.emptyMap(), labels);
        KnRevisionNode knRevisionNode = new KnRevisionNode(knRootNode, knServiceNode, revision);
        Map<String, String> labels1 = new HashMap<>();
        labels1.put(KnConstants.CONFIGURATION_GENERATION, "2");
        Revision revision1 = new Revision("revision1", Collections.emptyList(), Collections.emptyMap(), labels1);
        KnRevisionNode knRevisionNode1 = new KnRevisionNode(knRootNode, knServiceNode, revision1);
        knRevisionDescriptor = new KnRevisionDescriptor(project, knRevisionNode, AllIcons.Icons.Ide.NextStep, null);
        knRevisionDescriptor1 = new KnRevisionDescriptor(project, knRevisionNode, AllIcons.Icons.Ide.NextStep, null);
        knRevisionDescriptor2 = new KnRevisionDescriptor(project, knRevisionNode1, AllIcons.Icons.Ide.NextStep, null);
        labelAndIconDescriptor = new LabelAndIconDescriptor(project, knRootNode, "test", AllIcons.Plugins.Disabled, null);
    }

    @Test
    public void Compare_TwoObjectEquals_0() {
        assertEquals(0, knNodeComparator.compare(knRevisionDescriptor, knRevisionDescriptor1));
    }

    @Test
    public void Compare_FirstObjectIsNotKnRevisionDescriptor_0() {
        assertEquals(0, knNodeComparator.compare(labelAndIconDescriptor, knRevisionDescriptor));
    }

    @Test
    public void Compare_SecondObjectIsNotKnRevisionDescriptor_0() {
        assertEquals(0, knNodeComparator.compare(knRevisionDescriptor, labelAndIconDescriptor));
    }

    @Test
    public void Compare_BothObjectsAreKnRevisionDescriptor_Minus1() {
        assertEquals(-1, knNodeComparator.compare(knRevisionDescriptor2, knRevisionDescriptor));
    }

    @Test
    public void Compare_BothObjectsAreKnRevisionDescriptor_1() {
        assertEquals(1, knNodeComparator.compare(knRevisionDescriptor, knRevisionDescriptor2));
    }
}
