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
package com.redhat.devtools.intellij.knative.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.ui.DeleteDialog;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.swing.tree.TreePath;

import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_CRUD;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_MISC;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class DeleteAction extends KnAction {
    private TelemetryMessageBuilder.ActionMessage telemetry;
    public DeleteAction() {
        super(true, KnServiceNode.class, KnRevisionNode.class, KnFunctionNode.class);
    }

    public DeleteAction(boolean acceptMultipleItems, Class... filters) {
        super(acceptMultipleItems, filters);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected, Kn kncli) {
        telemetry = TelemetryService.instance().action(NAME_PREFIX_CRUD + " " + getActionName(false));
        ParentableNode[] elements = Arrays.stream(selected).map(item -> getElement(item)).toArray(ParentableNode[]::new);
        String title, dialogText = "Are you sure you want to " + getActionName(false) + " ";

        if (elements.length == 1) {
            String name = elements[0].getName();
            String kind = elements[0].getClass().getSimpleName().toLowerCase().replace("node", "");
            title = getActionName(true) + " " + name;
            dialogText += kind + " " + name + " ?";
        } else {
            title = getActionName(true) + " multiple items";
            dialogText += "the following items?\n";
            for (ParentableNode element: elements) {
                dialogText += element.getName() + "\n";
            }
        }

        DeleteDialog deleteDialog = new DeleteDialog(null, title, dialogText, getActionName(true));
        deleteDialog.show();

        if (deleteDialog.isOK()) {
            CompletableFuture.runAsync(() -> executeDelete(anActionEvent.getProject(), kncli, elements));
        }
    }

    protected String getActionName(boolean firstLetterCapital) {
        String d = firstLetterCapital ? "D" : "d";
        return d + "elete";
    }

    public void executeDelete(Project project, Kn kncli, ParentableNode[] elements) {
        Map<Class, List<ParentableNode>> resourcesByClass = groupResourcesByClass(elements);
        String namespace = kncli.getNamespace();
        for(Class type: resourcesByClass.keySet()) {
            try {
                doDelete(project, kncli, type, resourcesByClass);
                telemetry.property(TelemetryService.PROP_RESOURCE_KIND, type.getSimpleName())
                        .success()
                        .send();
            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
                telemetry
                        .error(anonymizeResource(null, namespace, e.getMessage()))
                        .send();
            }
        }
    }

    protected void doDelete(Project project, Kn kncli, Class type, Map<Class, List<ParentableNode>> resourcesByClass) throws IOException {
        deleteResources(type, resourcesByClass, kncli);
        TreeHelper.refresh(project, (ParentableNode) resourcesByClass.get(type).get(0).getParent());
    }

    private Map<Class, List<ParentableNode>> groupResourcesByClass(ParentableNode[] elements) {
        Map<Class, List<ParentableNode>> resourcesByClass = new HashMap<>();
        Arrays.stream(elements).forEach(element ->
                resourcesByClass.computeIfAbsent(element.getClass(), value -> new ArrayList<>())
                        .add(element));
        return resourcesByClass;
    }

    private void deleteResources(Class type, Map<Class, List<ParentableNode>> resourcesByClass, Kn kncli) throws IOException {
        List<String> resources = resourcesByClass.get(type).stream().map(x -> x.getName()).collect(Collectors.toList());
        if (type.equals(KnServiceNode.class)) {
            kncli.deleteServices(resources);
        } else if (type.equals(KnRevisionNode.class)) {
            kncli.deleteRevisions(resources);
        }
    }
}
