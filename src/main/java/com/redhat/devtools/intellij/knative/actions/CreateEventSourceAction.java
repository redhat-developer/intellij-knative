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
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.model.CreateDialogModel;
import com.redhat.devtools.intellij.knative.tree.KnEventingSourcesNode;
import com.redhat.devtools.intellij.knative.tree.KnServingNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.ui.CreateEventSourceDialog;
import com.redhat.devtools.intellij.knative.ui.CreateServiceDialog;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.tree.TreePath;

public class CreateEventSourceAction extends KnAction {
    public CreateEventSourceAction() {
        super(KnEventingSourcesNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        ParentableNode node = getElement(selected);
        String namespace = node.getRootNode().getKn().getNamespace();
        CreateDialogModel model = new CreateDialogModel(anActionEvent.getProject(),
                "New Event Source",
                namespace,
                () -> TreeHelper.getKnTreeStructure(getEventProject(anActionEvent)).fireModified(getElement(selected)),
                getServices(knCli),
                knCli.getServiceAccounts());
        ExecHelper.submit(() -> {
            UIHelper.executeInUI(() -> {
                CreateEventSourceDialog createDialog = new CreateEventSourceDialog(model);
                createDialog.setModal(false);
                createDialog.show();
            });
        });
    }

    private List<String> getServices(Kn knCli) {
        try {
            return knCli.getServicesList().stream().map(service -> service.getName()).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
