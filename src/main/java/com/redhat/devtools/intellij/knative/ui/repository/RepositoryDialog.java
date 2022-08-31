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
package com.redhat.devtools.intellij.knative.ui.repository;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.CollectionListModel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListItemEditor;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class RepositoryDialog extends DialogWrapper {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryDialog.class);
    public static final String EMPTY = "empty";
    public static final String PANEL = "panel";
    public final List<RepositoryUtils.RepositoryChange> changes = new ArrayList<>();

    private final ListItemEditor<Repository> itemEditor = new ListItemEditor<Repository>() {

        @NotNull
        @Override
        public Class<Repository> getItemClass() {
            return Repository.class;
        }

        @Override
        public Repository clone(@NotNull Repository item, boolean forInPlaceEditing) {
            if (pendingChanges.stream().noneMatch(repo -> repo.getName().equalsIgnoreCase(item.getName()))){
                updateChanges(item, RepositoryUtils.Operation.CREATE);
                pendingChanges.add(item);
                return item;
            }
            return null;
        }

        @Override
        public boolean isEmpty(@NotNull Repository item) {
            return item.getName().isEmpty() && item.getUrl().isEmpty();
        }

        @NotNull
        @Override
        public String getName(@NotNull Repository item) {
            return item.getName();
        }

        @Override
        public boolean isRemovable(@NotNull Repository item) {
            updateChanges(item, RepositoryUtils.Operation.DELETE);
            pendingChanges.remove(item);
            return true;
        }

        private void updateChanges(Repository item, RepositoryUtils.Operation operation) {
            Optional<RepositoryUtils.RepositoryChange> pendingChange = changes.stream()
                    .filter(repo -> repo.getRepository().equals(item))
                    .findFirst();
            if (pendingChange.isPresent()) {
                changes.remove(pendingChange.get());
            } else {
                changes.add(new RepositoryUtils.RepositoryChange(item, operation));
            }
            updateApplyButtonEnabling();
        }
    };

    private final RepositoryModelEditor editor = new RepositoryModelEditor(itemEditor);

    private JComponent component;
    private JPanel itemPanelWrapper;
    private List<Repository> repositories, pendingChanges;
    private JTextField txtName, txtUrl;
    private Kn kncli;
    private TelemetryMessageBuilder.ActionMessage telemetry;

    public RepositoryDialog(@Nullable Project project, Kn kncli, List<Repository> repositories, TelemetryMessageBuilder.ActionMessage telemetry) {
        super(project, false);
        this.repositories = repositories;
        this.pendingChanges = repositories;
        this.kncli = kncli;
        this.telemetry = telemetry;
        setTitle("Repository");
        setOKButtonText("Apply");
        updateApplyButtonEnabling();
        buildStructure(repositories);
        init();
    }

    public void buildStructure(List<Repository> repositories) {
        final CardLayout cardLayout = new CardLayout();
        editor.getModel().add(repositories);

        // doesn't make any sense (and in any case scheme manager cannot preserve order)
        editor.disableUpDownActions();

        editor.getList().addListSelectionListener(e -> {
            Repository item = editor.getSelected();
            if (item == null) {
                cardLayout.show(itemPanelWrapper, EMPTY);
            }
            else {
                txtName.setText(item.getName());
                txtUrl.setText(item.getUrl());
                cardLayout.show(itemPanelWrapper, PANEL);
            }
        });

        itemPanelWrapper = new JPanel(cardLayout);

        JLabel descLabel = new JLabel("<html>Repositories allow you to add and use new templates when creating a new Function.</html>");
        descLabel.setBorder(JBUI.Borders.empty(0, 25));

        itemPanelWrapper.add(descLabel, EMPTY);
        txtName = new JTextField();
        txtName.setEditable(false);
        txtUrl = new JTextField();
        txtUrl.setEditable(false);
        itemPanelWrapper.add(RepositoryUtils.createPanelRepository(txtName, txtUrl), PANEL);


        Splitter splitter = new Splitter(false, 0.3f);
        splitter.setFirstComponent(editor.createComponent());
        splitter.setSecondComponent(itemPanelWrapper);
        component = splitter;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(component, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(550, 300));
        return wrapper;
    }

    public CollectionListModel<Repository> getModel() {
        return editor.getModel();
    }

    @Override
    protected void doOKAction() {
        List<RepositoryUtils.RepositoryChange> finalChanges = copyChanges();
        changes.clear();
        updateApplyButtonEnabling();
        ExecHelper.submit(() -> {
            for (RepositoryUtils.RepositoryChange pendingChange : finalChanges) {
                try {
                    if (pendingChange.getOperation().equals(RepositoryUtils.Operation.CREATE)) {
                        doAddRepository(pendingChange.getRepository());
                    } else if (pendingChange.getOperation().equals(RepositoryUtils.Operation.DELETE)) {
                        doRemoveRepository(pendingChange.getRepository());
                    }
                } catch (IOException e) {
                    sendErrorNotification(pendingChange.getRepository(), e);
                }
            }
        });
    }

    private void updateApplyButtonEnabling() {
        setOKActionEnabled(changes.size() > 0);
    }

    private List<RepositoryUtils.RepositoryChange> copyChanges() {
        List<RepositoryUtils.RepositoryChange> finalChanges = new ArrayList<>();
        for (RepositoryUtils.RepositoryChange change: changes) {
            finalChanges.add(change.clone());
        }
        return finalChanges;
    }

    private void doAddRepository(Repository repository) throws IOException {
        kncli.addRepo(repository);
        sendAddNotification(repository);
    }

    private void doRemoveRepository(Repository repository) throws IOException {
        kncli.removeRepo(repository);
        sendRemoveNotification(repository);
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
}
