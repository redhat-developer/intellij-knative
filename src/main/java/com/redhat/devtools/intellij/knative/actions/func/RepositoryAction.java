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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.CollectionListModel;
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

import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_MISC;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

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
            RepositoryDialog dialog = UIHelper.executeInUI(() -> {
                RepositoryDialog repositoryDialog = new RepositoryDialog(anActionEvent.getProject(), repositories);
                repositoryDialog.show();
                return repositoryDialog;
            });

            if (dialog.isOK()) {
                CollectionListModel<Repository> model = dialog.getModel();
                updateRepos(knCli, repositories, model.getItems());
            }
        });
    }

    private void updateRepos(Kn kncli, List<Repository> originalRepos, List<Repository> updatedRepos) {
        List<Repository> toBeDeleted = findReposOnlyBelongToFirstCollection(updatedRepos, originalRepos);
        List<Repository> toBeCreated = findReposOnlyBelongToFirstCollection(originalRepos, updatedRepos);

        for (Repository repo: toBeDeleted) {
            doRemoveAction(kncli, repo);
        }

        for (Repository newRepo: toBeCreated) {
            doAddAction(kncli, newRepo);
        }
    }

    private void doAddAction(Kn kncli, Repository repository) {
        try {
            kncli.addRepo(repository);
            sendAddNotification(repository);
        } catch (IOException e) {
            sendErrorNotification(repository, e);
        }
    }

    private void doRemoveAction(Kn kncli, Repository repository) {
        try {
            kncli.removeRepo(repository);
            sendRemoveNotification(repository);
        } catch (IOException e) {
            sendErrorNotification(repository, e);
        }
    }

    private void sendErrorNotification(Repository repository, IOException e) {
        Notification notification = new Notification(NOTIFICATION_ID,
                "Error",
                e.getLocalizedMessage(),
                NotificationType.ERROR);
        Notifications.Bus.notify(notification);
        logger.warn(e.getLocalizedMessage(), e);
        telemetry
                .error(anonymizeResource(repository.getName(), "", e.getLocalizedMessage()))
                .send();
    }

    private void sendAddNotification(Repository repository) {
        sendSuccessfulNotification("Added Successfully", repository.getName() + " has been saved!", repository.getName());
    }

    private void sendRemoveNotification(Repository repository) {
        sendSuccessfulNotification("Removed Successfully", repository.getName() + " has been removed!", repository.getName());
    }

    private void sendSuccessfulNotification(String title, String content, String repoName) {
        Notification notification = new Notification(NOTIFICATION_ID, title, content, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
        telemetry
                .result(anonymizeResource(repoName, "", content))
                .send();
    }

    private List<Repository> findReposOnlyBelongToFirstCollection(List<Repository> firstCollection, List<Repository> secondCollection) {
        List<Repository> toBeCreated = new ArrayList<>();
        for (Repository repo: secondCollection) {
            boolean isNew = firstCollection.stream().noneMatch(oRepo ->
                    oRepo.getUrl().equalsIgnoreCase(repo.getUrl())
                        && oRepo.getName().equalsIgnoreCase(repo.getName()));
            if (isNew) {
                toBeCreated.add(repo);
            }
        }
        return toBeCreated;
    }
}
