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
import java.util.Arrays;

public class CreateRepositoryDialog extends DialogWrapper {
    private JPanel myContentPanel;
    private JTextField txtName, txtUrl;
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
        repository.setName(txtName.getText());
        repository.setUrl(txtUrl.getText());
        super.doOKAction();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(myContentPanel, BorderLayout.CENTER);
        return panel;
    }

    private void fillContainer() {
        myContentPanel = new JPanel(new BorderLayout());
        txtName = new JTextField();
        txtUrl = new JTextField();
        myContentPanel.add(RepositoryUtils.createPanelRepository(txtName, txtUrl), BorderLayout.CENTER);
    }
}

