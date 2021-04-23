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

import com.redhat.devtools.intellij.knative.kn.Service;
import com.redhat.devtools.intellij.knative.utils.KnHelper;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

public class KnServiceTest extends BaseTest {
    @Test
    public void CreateServiceAndDelete() throws IOException {
        final String serviceName = "test1";
        String serviceAsYAML = load("service.yaml").replace("foo", serviceName);

        KnHelper.saveNew(kn, serviceAsYAML);
        //KnHelper.saveOnCluster(project, serviceAsYAML, true);
        // verify service has been created
        List<Service> services = kn.getServicesList();
        assertTrue(services.stream().anyMatch(service -> service.getName().equals(serviceName)));

        // clean up and verify cleaning succeed
        kn.deleteServices(services
                            .stream()
                            .filter(service -> service.getName().equals(serviceName))
                            .map(service -> service.getName())
                            .collect(Collectors.toList())
        );
        services = kn.getServicesList();
        assertFalse(services.stream().anyMatch(service -> service.getName().equals(serviceName)));
    }
}
