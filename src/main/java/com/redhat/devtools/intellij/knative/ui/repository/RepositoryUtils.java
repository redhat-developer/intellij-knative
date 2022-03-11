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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class RepositoryUtils {
    public static JPanel createPanelRepository(JTextField txtName, JTextField txtUrl) {
        JPanel panelRepo = new JPanel();
        panelRepo.setLayout(new BoxLayout(panelRepo, BoxLayout.Y_AXIS));
        panelRepo.setBorder(JBUI.Borders.empty(0, 15));

        panelRepo.add(createRepoFieldPanel("Name:", txtName));
        panelRepo.add(createRepoFieldPanel("Url:", txtUrl));
        return panelRepo;
    }

    private static JPanel createRepoFieldPanel(String text, JTextField textField) {
        Dimension ROW_DIMENSION = new Dimension(Integer.MAX_VALUE, 40);
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(60, 40));
        panel.add(label, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        panel.setMaximumSize(ROW_DIMENSION);
        return panel;
    }

}
