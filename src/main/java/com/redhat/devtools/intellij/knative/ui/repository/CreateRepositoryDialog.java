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

import com.intellij.openapi.ui.DialogWrapper;
import com.redhat.devtools.intellij.knative.ui.DeleteDialog;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Supplier;

import static com.redhat.devtools.intellij.knative.ui.UIConstants.RED_BORDER_SHOW_ERROR;

public class CreateRepositoryDialog extends DialogWrapper {
    private JPanel myContentPanel;
    private JTextField txtName, txtUrl;
    private JLabel lblNameError, lblUrlError;
    private Repository repository;
    private Supplier<List<Repository>> existingRepos;

    public CreateRepositoryDialog(Repository repository, Supplier<List<Repository>> existingRepos) {
        super(null, null, false, DialogWrapper.IdeModalityType.IDE);
        this.repository = repository;
        this.existingRepos = existingRepos;
        setTitle("New Repository");
        setOKButtonText("Create");
        fillContainer();
        init();
    }

    public static void main(String[] args) {
        DeleteDialog dialog = new DeleteDialog(null, "", "", "");
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    @Override
    protected void doOKAction() {
        String name = txtName.getText().trim();
        String url = txtUrl.getText().trim();
        boolean validName = RepositoryUtils.isValidRepositoryName(name, "", existingRepos.get());
        boolean validUrl = RepositoryUtils.isValidRepositoryUrl(url);

        if (validName && validUrl) {
            repository.setName(name);
            repository.setUrl(url);
            super.doOKAction();
        } else {
            JTextField placeholder = new JTextField();
            txtName.setBorder(validName ? placeholder.getBorder() : RED_BORDER_SHOW_ERROR);
            lblNameError.setVisible(!validName);
            txtUrl.setBorder(validUrl ? placeholder.getBorder() : RED_BORDER_SHOW_ERROR);
            lblUrlError.setVisible(!validUrl);
            myContentPanel.invalidate();
        }

    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(myContentPanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(450, 200));
        return panel;
    }

    private void fillContainer() {
        myContentPanel = new JPanel(new BorderLayout());
        txtName = new JTextField();
        txtUrl = new JTextField();
        lblNameError = new JLabel("Name cannot be empty and it must be unique. Verify it has not yet been used.");
        lblNameError.setVisible(false);
        lblUrlError = new JLabel("Url format not valid. Only file uri scheme is supported.");
        lblUrlError.setVisible(false);
        myContentPanel.add(RepositoryUtils.createPanelRepository(txtName, lblNameError, txtUrl, lblUrlError), BorderLayout.CENTER);
    }
}

