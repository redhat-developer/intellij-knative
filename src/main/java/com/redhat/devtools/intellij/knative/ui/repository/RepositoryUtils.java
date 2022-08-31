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

import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.knative.ui.UIConstants;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

public class RepositoryUtils {
    public enum Operation {
        CREATE,
        DELETE
    }

    public static JPanel createPanelRepository(JTextField txtName, JTextField txtUrl) {
        return createPanelRepository(txtName, null, txtUrl, null);
    }

    public static JPanel createPanelRepository(JTextField txtName, JLabel lblNameError, JTextField txtUrl, JLabel lblUrlError) {
        JPanel panelRepo = new JPanel();
        panelRepo.setLayout(new BoxLayout(panelRepo, BoxLayout.Y_AXIS));
        panelRepo.setBorder(JBUI.Borders.empty(0, 15));

        panelRepo.add(createRepoFieldPanel("Name:", txtName, lblNameError));
        panelRepo.add(createRepoFieldPanel("Url:", txtUrl, lblUrlError));
        return panelRepo;
    }

    private static JPanel createRepoFieldPanel(String text, JTextField textField, JLabel errorLabel) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        Dimension ROW_DIMENSION = new Dimension(Integer.MAX_VALUE, 40);
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(60, 40));
        panel.add(label, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        panel.setMaximumSize(ROW_DIMENSION);
        wrapper.add(panel);
        if (errorLabel != null) {
            errorLabel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 20));
            errorLabel.setMinimumSize(new Dimension(Integer.MAX_VALUE, 20));
            errorLabel.setForeground(UIConstants.RED);
            errorLabel.setBorder(JBUI.Borders.emptyLeft(60));
            wrapper.add(errorLabel);
        }
        return wrapper;
    }

    public static class RepositoryChange {
        private Repository repository;
        private Operation operation;

        public RepositoryChange(Repository repository, Operation operation) {
            this.repository = repository;
            this.operation = operation;
        }

        public Repository getRepository() {
            return repository;
        }

        public Operation getOperation() {
            return operation;
        }

        public RepositoryChange clone() {
            return new RepositoryChange(getRepository(), getOperation());
        }
    }
}
