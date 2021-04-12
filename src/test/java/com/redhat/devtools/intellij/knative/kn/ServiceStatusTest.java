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

import java.util.Collections;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class ServiceStatusTest {
    @Test
    public void ServiceStatus_Object() {
        ServiceStatus serviceStatus = new ServiceStatus("url", 1, "latestReadyRevisionName", "latestCreatedRevisionName", "addressUrl", Collections.emptyList(), Collections.emptyList());
        assertEquals("url", serviceStatus.getUrl());
        assertEquals(1, serviceStatus.getObservedGeneration());
        assertEquals("latestReadyRevisionName", serviceStatus.getLatestReadyRevisionName());
        assertEquals("latestCreatedRevisionName", serviceStatus.getLatestCreatedRevisionName());
        assertEquals("addressUrl", serviceStatus.getAddressUrl());
        assertEquals(Collections.emptyList(), serviceStatus.getTraffic());
        assertEquals(Collections.emptyList(), serviceStatus.getConditions());
    }
}
