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

package com.redhat.devtools.intellij.knative.actions.func;

import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;

import java.io.IOException;

import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_CRUD;

public class RemoveVolumeAction extends RemoveConfigAction {
    @Override
    public void doRemoveConfig(Kn kncli, String path) throws IOException {
        kncli.removeVolume(path);
    }

    @Override
    public String getSuccessMessage(String namespace, String name) throws IOException {
        return "Removed volume from function " + name + " in namespace " + namespace + ".";
    }

    @Override
    protected TelemetryMessageBuilder.ActionMessage createTelemetry() {
        return TelemetryService.instance().action(NAME_PREFIX_CRUD + "remove volume");
    }

    @Override
    public String[] getSection() {
        return new String[] { "run", "volumes" };
    }
}
