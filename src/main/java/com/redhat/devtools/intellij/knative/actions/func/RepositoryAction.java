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
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.ui.repository.Repository;
import com.redhat.devtools.intellij.knative.ui.repository.RepositoryDialog;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_MISC;

public class RepositoryAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryAction.class);

    private TelemetryMessageBuilder.ActionMessage telemetry;

    public RepositoryAction() {
        super(KnRootNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        telemetry = TelemetryService.instance().action(NAME_PREFIX_MISC + "repo func");
        ExecHelper.submit(() -> {
            List<Repository> repositories = new ArrayList<>();
            try {
                repositories.addAll(knCli.getRepos());
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
                telemetry
                        .error(e.getLocalizedMessage())
                        .send();
            }
            UIHelper.executeInUI(() -> {
                RepositoryDialog repositoryDialog = new RepositoryDialog(
                        anActionEvent.getProject(),
                        knCli,
                        repositories,
                        telemetry);
                repositoryDialog.show();
            });
        });
    }
}
