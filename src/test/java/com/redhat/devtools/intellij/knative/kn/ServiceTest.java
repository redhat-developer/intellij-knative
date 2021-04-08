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
import static org.mockito.Mockito.mock;

public class ServiceTest {
    @Test
    public void Service_Object() {
        ServiceStatus serviceStatus = mock(ServiceStatus.class);
        Service service = new Service("name", serviceStatus);
        assertEquals("name", service.getName());
        assertEquals(serviceStatus, service.getStatus());
    }
}
