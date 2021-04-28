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
package com.redhat.devtools.intellij.knative.kn;

import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.BaseTest;
import io.fabric8.kubernetes.api.model.RootPaths;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class KnCliTest extends BaseTest {

    private static final String RESOURCES_PATH = "kn/";

    private KubernetesClient kubernetesClient;
    private RootPaths rootPaths;
    private URL masterURL;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException, MalformedURLException {
        kn = mock(KnCli.class, CALLS_REAL_METHODS);
        kubernetesClient = mock(KubernetesClient.class);
        rootPaths = mock(RootPaths.class);

        Field clientField = KnCli.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(kn, kubernetesClient);
        Field commandField = KnCli.class.getDeclaredField("command");
        commandField.setAccessible(true);
        commandField.set(kn, "command");
        Field envField = KnCli.class.getDeclaredField("envVars");
        envField.setAccessible(true);
        envField.set(kn, Collections.emptyMap());

        when(kubernetesClient.rootPaths()).thenReturn(rootPaths);

        masterURL = new URL("http://url.ext");
        when(kubernetesClient.getMasterUrl()).thenReturn(masterURL);
    }

    @Test
    public void isKnativeServingAware_ClientFails_Throws() {
        when(rootPaths.getPaths()).thenThrow(new KubernetesClientException("error"));
        try {
            kn.isKnativeServingAware();
        } catch (IOException e) {
            assertEquals("io.fabric8.kubernetes.client.KubernetesClientException: error", e.getLocalizedMessage());
        }
    }

    @Test
    public void isKnativeServingAware_ClusterHasKnativeServing_True() throws IOException {
        when(rootPaths.getPaths()).thenReturn(Arrays.asList("serving.knative.dev", "type"));
        boolean result = kn.isKnativeServingAware();
        assertTrue(result);
    }

    @Test
    public void isKnativeServingAware_ClusterHasNotKnativeServing_False() throws IOException {
        when(rootPaths.getPaths()).thenReturn(Arrays.asList("sometype", "type2"));
        boolean result = kn.isKnativeServingAware();
        assertFalse(result);
    }

    @Test
    public void isKnativeEventingAware_ClientFails_Throws() {
        when(rootPaths.getPaths()).thenThrow(new KubernetesClientException("error"));
        try {
            kn.isKnativeEventingAware();
        } catch (IOException e) {
            assertEquals("io.fabric8.kubernetes.client.KubernetesClientException: error", e.getLocalizedMessage());
        }
    }

    @Test
    public void isKnativeEventingAware_ClusterHasKnativeEventing_True() throws IOException {
        when(rootPaths.getPaths()).thenReturn(Arrays.asList("eventing.knative.dev", "type"));
        boolean result = kn.isKnativeEventingAware();
        assertTrue(result);
    }

    @Test
    public void isKnativeEventingAware_ClusterHasNotKnativeEventing_False() throws IOException {
        when(rootPaths.getPaths()).thenReturn(Arrays.asList("sometype", "type2"));
        boolean result = kn.isKnativeEventingAware();
        assertFalse(result);
    }

    @Test
    public void GetMasterURL_URL() {
        assertEquals(masterURL, kn.getMasterUrl());
    }

    @Test
    public void GetNamespace_NamespaceIsEmpty_Default() {
        when(kubernetesClient.getNamespace()).thenReturn("");
        assertEquals("default", kn.getNamespace());
    }

    @Test
    public void GetNamespace_NamespaceExists_Namespace() {
        when(kubernetesClient.getNamespace()).thenReturn("namespace");
        assertEquals("namespace", kn.getNamespace());
    }

    @Test
    public void GetServicesList_ClusterHasNoServices_EmptyList() throws IOException {
        ExecHelper.ExecResult execResult = new ExecHelper.ExecResult("No services found.", null, 0);
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.executeWithResult(anyString(), anyMap(), any())).thenReturn(execResult);
            assertEquals(Collections.emptyList(), kn.getServicesList());
        }
    }

    @Test
    public void GetServicesList_ClusterHasServices_ListOfServices() throws IOException {
        String servicesListInJson = load(RESOURCES_PATH + "serviceslist.json");
        ExecHelper.ExecResult execResult = new ExecHelper.ExecResult(servicesListInJson, null, 0);
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.executeWithResult(anyString(), anyMap(), any())).thenReturn(execResult);
            List<Service> serviceList = kn.getServicesList();
            assertEquals(1, serviceList.size());
            assertEquals("test", serviceList.get(0).getName());
        }
    }

    @Test
    public void GetServicesList_ClientFails_Throws() {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.executeWithResult(anyString(), anyMap(), any())).thenThrow(new IOException("error"));
            kn.getServicesList();
        } catch (IOException e) {
            assertEquals("error", e.getLocalizedMessage());
        }
    }

    @Test
    public void GetRevisionsForService_ServiceHasNoRevisions_EmptyList() throws IOException {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn("No revisions found.");
            assertEquals(Collections.emptyList(), kn.getRevisionsForService("test"));
        }
    }

    @Test
    public void GetRevisionsForService_ServiceHasRevisions_ListOfRevisions() throws IOException {
        String servicesListInJson = load(RESOURCES_PATH + "revisionsList.json");
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(servicesListInJson);
            List<Revision> revisionList = kn.getRevisionsForService("test");
            assertTrue(revisionList.size() == 2);
            assertEquals("test-00002", revisionList.get(0).getName());
            assertEquals("test-00001", revisionList.get(1).getName());
        }
    }

    @Test
    public void GetRevisionsForService_ClientFails_Throws() {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenThrow(new IOException("error"));
            kn.getRevisionsForService("test");
        } catch (IOException e) {
            assertEquals("error", e.getLocalizedMessage());
        }
    }

    @Test
    public void GetService_NameIsValid_Service() throws IOException {
        String serviceInJson = load(RESOURCES_PATH + "service.json");
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(serviceInJson);
            Service service = kn.getService("test");
            assertEquals("test", service.getName());
        }
    }

    @Test
    public void GetService_ClientFails_Throws() {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenThrow(new IOException("error"));
            kn.getService("test");
        } catch (IOException e) {
            assertEquals("error", e.getLocalizedMessage());
        }
    }

    @Test
    public void GetServiceYAML_NameIsValid_ServiceYAML() throws IOException {
        String serviceInYaml = load(RESOURCES_PATH + "service.yaml");
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(serviceInYaml);
            assertEquals(serviceInYaml, kn.getServiceYAML("test"));
        }
    }

    @Test
    public void GetServiceYAML_ClientFails_Throws() {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenThrow(new IOException("error"));
            kn.getServiceYAML("test");
        } catch (IOException e) {
            assertEquals("error", e.getLocalizedMessage());
        }
    }

    @Test
    public void GetRevisionYAML_NameIsValid_RevisionYAML() throws IOException {
        String revisionInYaml = load(RESOURCES_PATH + "revision.yaml");
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenReturn(revisionInYaml);
            assertEquals(revisionInYaml, kn.getRevisionYAML("test"));
        }
    }

    @Test
    public void GetRevisionYAML_ClientFails_Throws() {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenThrow(new IOException("error"));
            kn.getRevisionYAML("test");
        } catch (IOException e) {
            assertEquals("error", e.getLocalizedMessage());
        }
    }

    @Test
    public void DeleteServices_OneServiceToBeDeleted_DeletionIsCalled() throws IOException {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            kn.deleteServices(Arrays.asList("one"));
            execHelperMockedStatic.verify(() ->
                    ExecHelper.execute(anyString(), anyMap(), eq("service"), eq("delete"), eq("one"), eq("-n"), eq("default")));
        }
    }

    @Test
    public void DeleteServices_MultipleServicesToBeDeleted_DeletionIsCalled() throws IOException {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            kn.deleteServices(Arrays.asList("one", "two", "three"));
            execHelperMockedStatic.verify(() ->
                    ExecHelper.execute(anyString(), anyMap(), eq("service"), eq("delete"), eq("one"),
                            eq("two"), eq("three"), eq("-n"), eq("default")));
        }
    }

    @Test
    public void DeleteServices_ClientFails_Throws() {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenThrow(new IOException("error"));
            kn.deleteServices(Collections.emptyList());
        } catch (IOException e) {
            assertEquals("error", e.getLocalizedMessage());
        }
    }

    @Test
    public void DeleteRevisions_OneRevisionToBeDeleted_DeletionIsCalled() throws IOException {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            kn.deleteRevisions(Arrays.asList("one"));
            execHelperMockedStatic.verify(() ->
                    ExecHelper.execute(anyString(), anyMap(), eq("revision"), eq("delete"), eq("one"), eq("-n"), eq("default")));
        }
    }

    @Test
    public void DeleteRevisions_MultipleRevisionsToBeDeleted_DeletionIsCalled() throws IOException {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            kn.deleteRevisions(Arrays.asList("one", "two", "three"));
            execHelperMockedStatic.verify(() ->
                    ExecHelper.execute(anyString(), anyMap(), eq("revision"), eq("delete"), eq("one"),
                            eq("two"), eq("three"), eq("-n"), eq("default")));
        }
    }

    @Test
    public void DeleteRevisions_ClientFails_Throws() {
        try (MockedStatic<ExecHelper> execHelperMockedStatic = mockStatic(ExecHelper.class)) {
            execHelperMockedStatic.when(() -> ExecHelper.execute(anyString(), anyMap(), any())).thenThrow(new IOException("error"));
            kn.deleteRevisions(Collections.emptyList());
        } catch (IOException e) {
            assertEquals("error", e.getLocalizedMessage());
        }
    }

}
