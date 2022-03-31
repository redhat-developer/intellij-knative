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

import com.google.common.base.Strings;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.CommonTerminalExecutionConsole;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.utils.FuncUtils;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.intellij.openapi.ui.Messages.CANCEL_BUTTON;
import static com.intellij.openapi.ui.Messages.OK_BUTTON;
import static com.intellij.openapi.ui.Messages.showOkCancelDialog;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_BUILD_DEPLOY;

public class DeployAction extends BuildAction {

    public DeployAction() {
        super();
    }

    @Override
    protected void doExecute(CommonTerminalExecutionConsole terminalExecutionConsole, Kn knCli, String namespace, String localPathFunc, String registry, String image) throws IOException {
        knCli.deployFunc(namespace, localPathFunc, registry, image);
    }

    @Override
    protected boolean isActionConfirmed(String name, String funcNamespace, String activeNamespace) {
        String message = "";
        if (!Strings.isNullOrEmpty(funcNamespace)) {
            if (!funcNamespace.equalsIgnoreCase(activeNamespace)) {
                message = "Function namespace (declared in func.yaml) is different from the current active namespace. \n";
            }
            message += "Deploy function " + name + " to namespace " + funcNamespace + "?";
        } else {
            message += "Deploy function " + name + " to namespace " + activeNamespace + "?";
        }
        int result = showOkCancelDialog(message,
                "Deploy Function " + name,
                OK_BUTTON, CANCEL_BUTTON, null);
        return result == Messages.OK;
    }

    protected TelemetryMessageBuilder.ActionMessage createTelemetry() {
        return TelemetryService.instance().action(NAME_PREFIX_BUILD_DEPLOY + "deploy func");
    }

    protected String getSuccessMessage(String namespace, String name) {
        return "Function " + name + " in namespace " + namespace + " has been successfully deployed";
    }

    @Override
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible) {
            Kn kn = ((KnFunctionNode) selected).getRootNode().getKn();
            return FuncUtils.isKnativeReady(kn);
        }
        return false;
    }


}
