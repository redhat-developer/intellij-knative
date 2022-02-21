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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.FuncUtils;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;

import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public abstract class AddConfigAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(AddConfigAction.class);

    protected TelemetryMessageBuilder.ActionMessage telemetry;

    public AddConfigAction() {
        super(KnFunctionNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        telemetry = createTelemetry();
        ParentableNode node = getElement(selected);
        String name = node.getName();
        String namespace = knCli.getNamespace();
        String localPathFunc = ((KnFunctionNode) node).getFunction().getLocalPath();
        if (localPathFunc.isEmpty()) {
            telemetry
                    .result(anonymizeResource(name, namespace, "Function " + name + "is not opened locally"))
                    .send();
            return;
        }

        ExecHelper.submit(() -> {
            try {
                doAddConfig(knCli, localPathFunc);
                telemetry
                        .result(anonymizeResource(name, namespace, getSuccessMessage(namespace, name)))
                        .send();
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
                telemetry
                        .error(anonymizeResource(name, namespace, e.getLocalizedMessage()))
                        .send();
            }
        });
    }

    public abstract void doAddConfig(Kn kncli, String path) throws IOException;

    public abstract String getSuccessMessage(String namespace, String name) throws IOException;

    protected abstract TelemetryMessageBuilder.ActionMessage createTelemetry();

    @Override
    public boolean isVisible(Object selected) {
        // addConfig action is enabled only if the user works with a func opened locally and have access to a cluster
        // this is because addConfig action can work with secrets and configmaps
        if (selected instanceof KnFunctionNode) {
            Kn kn = ((KnFunctionNode) selected).getRootNode().getKn();
            return !((KnFunctionNode) selected).getFunction().getLocalPath().isEmpty()
                    && FuncUtils.isKnativeReady(kn);
        }
        return false;
    }
}
