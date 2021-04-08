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
import static org.junit.Assert.assertNull;

public class StatusConditionTest {

    @Test
    public void StatusCondition_ReasonAndMessageNull_Object() {
        StatusCondition statusCondition = new StatusCondition("time", "status", "type");
        assertEquals("time", statusCondition.getLastTransitionTime());
        assertEquals("status", statusCondition.getStatus());
        assertEquals("type", statusCondition.getType());
        assertNull(statusCondition.getReason());
        assertNull(statusCondition.getMessage());
    }

    @Test
    public void StatusCondition_ValidReasonAndMessage_Object() {
        StatusCondition statusCondition = new StatusCondition("time", "status", "type", "reason", "message");
        assertEquals("time", statusCondition.getLastTransitionTime());
        assertEquals("status", statusCondition.getStatus());
        assertEquals("type", statusCondition.getType());
        assertEquals("reason", statusCondition.getReason());
        assertEquals("message", statusCondition.getMessage());
    }
}
