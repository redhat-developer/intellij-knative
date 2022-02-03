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

import com.redhat.devtools.intellij.knative.actions.func.AddVolumeAction;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class AddVolumeActionTest extends ActionTest {
    @Test
    public void AddConfig_AddVolumeIsCalled() throws IOException {
        AddVolumeAction action = new AddVolumeAction();
        action.doAddConfig(kn, "path");
        verify(kn, times(1)).addVolume(anyString());
    }
}

