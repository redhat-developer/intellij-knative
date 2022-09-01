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
import com.intellij.ui.DocumentAdapter;
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
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.knative.ui.UIConstants.RED_BORDER_SHOW_ERROR;
import static com.redhat.devtools.intellij.knative.ui.repository.RepositoryUtils.NATIVE_NAME;
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
            if (forInPlaceEditing) {
                pendingChanges.removeIf(repo -> repo.getAttribute(NATIVE_NAME).equalsIgnoreCase(item.getAttribute(NATIVE_NAME)));
                // the repository has being renamed. If it was an old existing one, we perform a rename
                // if it was a new addition (still pending - not saved on cluster) we create it
                updateChanges(item, RepositoryUtils.Operation.RENAME);
                pendingChanges.add(item);
                return item;
            }
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

        /**
         * Update the list of changes to be made by keeping it cleaned.
         * E.g. two renamed actions have been performed without having saved them? Only on rename action is in the list with the latest modification
         * @param item item to add to the changes list
         * @param operation the operation to be performed (CREATE, RENAME, DELETE)
         */
        private void updateChanges(Repository item, RepositoryUtils.Operation operation) {
            Optional<RepositoryUtils.RepositoryChange> pendingChange = changes.stream()
                    .filter(repo -> repo.getRepository().equals(item))
                    .findFirst();
            if (operation.equals(RepositoryUtils.Operation.CREATE)) {
                changes.add(new RepositoryUtils.RepositoryChange(item, operation));
            } else if (operation.equals(RepositoryUtils.Operation.DELETE)) {
                if (pendingChange.isPresent()) {
                    changes.remove(pendingChange.get());
                } else {
                    changes.add(new RepositoryUtils.RepositoryChange(item, operation));
                }
            } else if (operation.equals(RepositoryUtils.Operation.RENAME)) {
                Optional<RepositoryUtils.RepositoryChange> pendingCreationRenameChange = changes.stream()
                        .filter(repo -> repo.getRepository().equals(item)
                                && (repo.getOperation().equals(RepositoryUtils.Operation.CREATE)
                                    || repo.getOperation().equals(RepositoryUtils.Operation.RENAME)))
                        .findFirst();
                if (pendingCreationRenameChange.isPresent()) {
                    changes.remove(pendingCreationRenameChange.get());
                    changes.add(new RepositoryUtils.RepositoryChange(item, pendingCreationRenameChange.get().getOperation()));
                } else {
                    changes.add(new RepositoryUtils.RepositoryChange(item, operation));
                }
            }
            updateApplyButtonEnabling();
        }
    };

    private JComponent component;
    private JPanel itemPanelWrapper;
    private List<Repository> repositories, pendingChanges;
    private JTextField txtName, txtUrl;
    private JLabel lblNameError, lblUrlError;
    private Kn kncli;
    private TelemetryMessageBuilder.ActionMessage telemetry;

    private final RepositoryModelEditor editor = new RepositoryModelEditor(itemEditor, new Supplier<List<Repository>>() {
        @Override
        public List<Repository> get() {
            return pendingChanges;
        }
    });

    public RepositoryDialog(@Nullable Project project, Kn kncli, List<Repository> repositories, TelemetryMessageBuilder.ActionMessage telemetry) {
        super(project, false);
        this.repositories = cloneList(repositories);
        this.pendingChanges = repositories;
        this.kncli = kncli;
        this.telemetry = telemetry;
        setTitle("Repository");
        setOKButtonText("Apply");
        updateApplyButtonEnabling();
        buildStructure(repositories);
        init();
    }

    private List<Repository> cloneList(List<Repository> repositories) {
        List<Repository> finalRepos = new ArrayList<>();
        repositories.forEach(repo -> finalRepos.add(repo.clone()));
        return finalRepos;
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
        txtUrl = new JTextField();
        txtUrl.setEditable(false);
        lblNameError = new JLabel("Name cannot be empty and it must be unique.");
        lblNameError.setVisible(false);
        lblUrlError = new JLabel("Url format not valid. Only file uri scheme is supported.");
        lblUrlError.setVisible(false);
        JPanel out = RepositoryUtils.createPanelRepository(txtName, lblNameError, txtUrl, lblUrlError);
        itemPanelWrapper.add(out, PANEL);

        txtName.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                Repository repo = editor.getSelected();
                if (repo == null) {
                    return;
                }
                String name = txtName.getText().trim();
                if (name.equals(repo.getName())) {
                    setError(false);
                    return;
                }
                boolean validName = RepositoryUtils.isValidRepositoryName(name, repo.getAttribute(NATIVE_NAME), pendingChanges);

                if (validName) {
                    repo.setName(name);
                    itemEditor.clone(repo, true);
                }
                setError(!validName);
            }

            private void setError(boolean isError) {
                JTextField placeholder = new JTextField();
                txtName.setBorder(!isError ? placeholder.getBorder() : RED_BORDER_SHOW_ERROR);
                lblNameError.setVisible(isError);
                component.invalidate();
            }
        });

        Splitter splitter = new Splitter(false, 0.3f);

        splitter.setFirstComponent(editor.createComponent());
        splitter.getFirstComponent().setMinimumSize(new Dimension(150, Integer.MAX_VALUE));
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
                    } else if (pendingChange.getOperation().equals(RepositoryUtils.Operation.RENAME)) {
                        doRenameRepository(pendingChange.getRepository());
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

    private void doRenameRepository(Repository repository) throws IOException {
        kncli.renameRepo(repository);
        sendRenameNotification(repository);
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
        sendSuccessfulNotification("Repository Added", repository.getName() + " has been saved!", repository.getName());
    }

    private void sendRenameNotification(Repository repository) {
        sendSuccessfulNotification("Repository Renamed",
                repository.getAttribute(NATIVE_NAME) + " has been renamed to " + repository.getName(),
                repository.getName());
    }

    private void sendRemoveNotification(Repository repository) {
        sendSuccessfulNotification("Repository Removed", repository.getName() + " has been removed!", repository.getName());
    }

    private void sendSuccessfulNotification(String title, String content, String repoName) {
        Notification notification = new Notification(NOTIFICATION_ID, title, content, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
        telemetry
                .result(anonymizeResource(repoName, "", content))
                .send();
    }
}
