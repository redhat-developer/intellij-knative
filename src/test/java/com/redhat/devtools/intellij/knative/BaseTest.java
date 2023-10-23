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
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.KnCli;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnEventingBrokerNode;
import com.redhat.devtools.intellij.knative.tree.KnEventingChannelsNode;
import com.redhat.devtools.intellij.knative.tree.KnEventingNode;
import com.redhat.devtools.intellij.knative.tree.KnEventingSourcesNode;
import com.redhat.devtools.intellij.knative.tree.KnEventingSubscriptionsNode;
import com.redhat.devtools.intellij.knative.tree.KnEventingTriggersNode;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.KnFunctionsTreeStructure;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.KnServingNode;
import com.redhat.devtools.intellij.knative.tree.KnSinkNode;
import com.redhat.devtools.intellij.knative.tree.KnSourceNode;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.apache.commons.io.IOUtils;
import org.junit.Before;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;

public abstract class BaseTest extends BasePlatformTestCase {

    protected Project project;
    protected Kn kn;
    protected ParentableNode<?> parentableNode;
    protected KnRootNode knRootNode;
    protected KnServingNode knServingNode;
    protected KnServiceNode knServiceNode;
    protected KnEventingNode knEventingNode;
    protected KnRevisionNode knRevisionNode;
    protected KnFunctionNode knFunctionNode;
    protected KnTreeStructure knTreeStructure;
    protected KnFunctionsTreeStructure knFunctionsTreeStructure;
    protected KnEventingBrokerNode knEventingBrokerNode;
    protected KnEventingChannelsNode knEventingChannelsNode;
    protected KnEventingSourcesNode knEventingSourcesNode;
    protected KnEventingSubscriptionsNode knEventingSubscriptionsNode;
    protected KnEventingTriggersNode knEventingTriggersNode;
    protected KnSourceNode knSourceNode;
    protected KnSinkNode knSinkNode;
    protected TelemetryService telemetryService;
    protected TelemetryMessageBuilder telemetryMessageBuilder;
    protected TelemetryMessageBuilder.ActionMessage actionMessage;
    protected Function function;

    public void setUp() throws Exception {
        super.setUp();
        project = mock(Project.class);
        kn = mock(KnCli.class);
        parentableNode = mock(ParentableNode.class);
        knRootNode = mock(KnRootNode.class);
        knServingNode = mock(KnServingNode.class);
        knServiceNode = mock(KnServiceNode.class);
        knEventingNode = mock(KnEventingNode.class);
        knRevisionNode = mock(KnRevisionNode.class);
        knTreeStructure = mock(KnTreeStructure.class);
        knFunctionsTreeStructure = mock(KnFunctionsTreeStructure.class);
        knEventingBrokerNode = mock(KnEventingBrokerNode.class);
        knEventingChannelsNode = mock(KnEventingChannelsNode.class);
        knEventingSourcesNode = mock(KnEventingSourcesNode.class);
        knEventingSubscriptionsNode = mock(KnEventingSubscriptionsNode.class);
        knEventingTriggersNode = mock(KnEventingTriggersNode.class);
        knSourceNode = mock(KnSourceNode.class);
        knSinkNode = mock(KnSinkNode.class);
        knFunctionNode = mock(KnFunctionNode.class);
        telemetryMessageBuilder = mock(TelemetryMessageBuilder.class);
        actionMessage = mock(TelemetryMessageBuilder.ActionMessage.class);
        function = mock(Function.class);
    }

    protected String load(String name) throws IOException {
        return IOUtils.toString(getUrl(name), StandardCharsets.UTF_8);
    }

    protected Path getPath(String name) throws IOException {
        try {
            return Paths.get(getUrl(name).toURI());
        } catch (URISyntaxException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }
    private URL getUrl(String name) throws IOException {
        URL url = BaseTest.class.getResource("/" + name);
        if (url == null) {
            throw new IOException("File " + name + " not found");
        }
        return url;
    }
}
