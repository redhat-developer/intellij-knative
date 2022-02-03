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

import java.io.IOException;

public class RemoveEnvAction extends RemoveConfigAction {
    @Override
    public void doRemoveConfig(Kn kncli, String path) throws IOException {
        kncli.removeEnv(path);
    }

    @Override
    public String[] getSection() {
        return new String[] { "envs" };
    }
}
