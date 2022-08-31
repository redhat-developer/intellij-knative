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
import com.intellij.ui.components.JBScrollPane;
import com.redhat.devtools.intellij.knative.ui.DeleteDialog;
import org.jetbrains.annotations.Nullable;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import static com.redhat.devtools.intellij.knative.ui.UIConstants.RED_BORDER_SHOW_ERROR;

public class CreateRepositoryDialog extends DialogWrapper {
    private JPanel myContentPanel;
    private JTextField txtName, txtUrl;
    private JLabel lblNameError, lblUrlError;
    private Repository repository;

    public CreateRepositoryDialog(Repository repository) {
        super(null, null, false, DialogWrapper.IdeModalityType.IDE);
        this.repository = repository;
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
        String name = txtName.getText();
        String url = txtUrl.getText();
        boolean validName = isValidName(name);
        boolean validUrl = isValidUrl(url);

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

    private boolean isValidName(String name) {
        return !name.trim().isEmpty();
    }

    private boolean isValidUrl(String url) {
        try {
            URL u = new URL(url);
            u.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
        return true;
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
        lblNameError = new JLabel("Name cannot be empty");
        lblNameError.setVisible(false);
        lblUrlError = new JLabel("Url format not valid. Only file uri scheme is supported.");
        lblUrlError.setVisible(false);
        myContentPanel.add(RepositoryUtils.createPanelRepository(txtName, lblNameError, txtUrl, lblUrlError), BorderLayout.CENTER);
    }
}

