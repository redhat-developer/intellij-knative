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

import com.fasterxml.jackson.databind.JsonNode;
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

public abstract class RemoveConfigAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(RemoveConfigAction.class);

    protected TelemetryMessageBuilder.ActionMessage telemetry;

    public RemoveConfigAction() {
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
            this.telemetry
                    .result(anonymizeResource(name, namespace, "Function " + name + "is not opened locally"))
                    .send();
            return;
        }

        ExecHelper.submit(() -> {
            try {
                doRemoveConfig(knCli, localPathFunc);
                this.telemetry
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

    public abstract void doRemoveConfig(Kn kncli, String path) throws IOException;

    public abstract String getSuccessMessage(String namespace, String name) throws IOException;

    protected abstract TelemetryMessageBuilder.ActionMessage createTelemetry();

    @Override
    public boolean isVisible(Object selected) {
        // removeConfig action is enabled only if the user works with a func opened locally and atleast an env has been set
        if (selected instanceof KnFunctionNode) {
            Kn kn = ((KnFunctionNode) selected).getRootNode().getKn();
            String localPath = ((KnFunctionNode) selected).getFunction().getLocalPath();
            JsonNode envsSection;
            try {
                envsSection = FuncUtils.getFuncSection(kn, localPath, getSection());
            } catch (IOException e) {
                return false;
            }
            return !localPath.isEmpty() && envsSection != null && !envsSection.isEmpty();
        }
        return false;
    }

    public abstract String[] getSection();
}
