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
package com.redhat.devtools.intellij.knative.actions.func;

import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnFunctionLocalNode;
import java.io.IOException;


import static com.intellij.openapi.ui.Messages.CANCEL_BUTTON;
import static com.intellij.openapi.ui.Messages.OK_BUTTON;
import static com.intellij.openapi.ui.Messages.showOkCancelDialog;

public class DeployAction extends BuildAction {

    public DeployAction() {
        super();
    }

    @Override
    protected void doExecute(Kn knCli, String namespace, String localPathFunc, String registry, String image) throws IOException {
        knCli.deployFunc(namespace, localPathFunc, registry, image);
    }

    @Override
    protected boolean isActionConfirmed(String name) {
        int result = showOkCancelDialog("Are you sure you want to deploy function " + name,
                "Deploy Function " + name,
                OK_BUTTON, CANCEL_BUTTON, null);
        return result == Messages.OK;
    }

    @Override
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible) {
            Kn kn = ((KnFunctionLocalNode) selected).getRootNode().getKn();
            return isKnativeReady(kn);
        }
        return false;
    }

    private boolean isKnativeReady(Kn kn) {
        try {
            return kn.isKnativeServingAware() && kn.isKnativeEventingAware();
        } catch (IOException e) {
            return false;
        }
    }
}
