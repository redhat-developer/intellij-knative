/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.actions;

import com.redhat.devtools.intellij.knative.actions.func.RemoveVolumeAction;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class RemoveVolumeActionTest extends ActionTest {
    @Test
    public void DoRemoveConfig_RemoveEnvIsCalled() throws IOException {
        RemoveVolumeAction action = new RemoveVolumeAction();
        action.doRemoveConfig(kn, "path");
        verify(kn, times(1)).removeVolume(anyString());
    }

    @Test
    public void GetSection_ReturnsEnv() throws IOException {
        RemoveVolumeAction action = new RemoveVolumeAction();
        String[] section = action.getSection();
        assertEquals(2, section.length);
        assertEquals("run", section[0]);
        assertEquals("volumes", section[1]);
    }
}
