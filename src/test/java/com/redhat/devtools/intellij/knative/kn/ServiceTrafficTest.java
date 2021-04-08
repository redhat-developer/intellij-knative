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

import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServiceTrafficTest {

    @Test
    public void ServiceTraffic_IsLatestRevision_Object() {
        ServiceTraffic serviceTraffic = new ServiceTraffic("tag", "revision", "config", true, 70,"url");
        assertEquals("tag", serviceTraffic.getTag());
        assertEquals("revision", serviceTraffic.getRevisionName());
        assertEquals("config", serviceTraffic.getConfigurationName());
        assertTrue(serviceTraffic.getLatestRevision());
        assertEquals(70, serviceTraffic.getPercent());
        assertEquals("url", serviceTraffic.getUrl());
    }
}
