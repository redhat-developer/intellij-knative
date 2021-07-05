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
import com.redhat.devtools.intellij.knative.tree.KnRevisionNode;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.KnSourceNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.ui.DeleteDialog;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.swing.tree.TreePath;

public class DeleteAction extends KnAction {
    public DeleteAction() {
        super(true, KnServiceNode.class, KnRevisionNode.class, KnSourceNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath[] path, Object[] selected, Kn kncli) {
        ParentableNode[] elements = Arrays.stream(selected).map(item -> getElement(item)).toArray(ParentableNode[]::new);
        String title, dialogText = "Are you sure you want to delete ";

        if (elements.length == 1) {
            String name = elements[0].getName();
            String kind = elements[0].getClass().getSimpleName().toLowerCase().replace("node", "");
            title = "Delete " + name;
            dialogText += kind + " " + name + " ?";
        } else {
            title = "Delete multiple items";
            dialogText += "the following items?\n";
            for (ParentableNode element: elements) {
                dialogText += element.getName() + "\n";
            }
        }

        DeleteDialog deleteDialog = new DeleteDialog(null, title, dialogText);
        deleteDialog.show();

        if (deleteDialog.isOK()) {
            CompletableFuture.runAsync(() -> executeDelete(anActionEvent.getProject(), kncli, elements));
        }

    }

    public void executeDelete(Project project, Kn kncli, ParentableNode[] elements) {
        Map<Class, List<ParentableNode>> resourcesByClass = groupResourcesByClass(elements);
        for(Class type: resourcesByClass.keySet()) {
            try {
                deleteResources(type, resourcesByClass, kncli);
            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
            }
            TreeHelper.refresh(project, (ParentableNode) resourcesByClass.get(type).get(0).getParent());
        }

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
        } else if (type.equals(KnSourceNode.class)) {
            kncli.deleteEventSources(resourcesByClass.get(type).stream().map(x -> ((KnSourceNode)x).getSource()).collect(Collectors.toList()));
        }
    }
}