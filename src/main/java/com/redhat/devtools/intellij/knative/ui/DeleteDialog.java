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
package com.redhat.devtools.intellij.knative.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.jetbrains.annotations.Nullable;

public class DeleteDialog extends DialogWrapper{
    private JPanel myContentPanel;

    public DeleteDialog(Component parent, String title, String mainDeleteText) {
        super(null, parent, false, DialogWrapper.IdeModalityType.IDE);
        this.myContentPanel = new JPanel(new BorderLayout());

        setTitle(title);
        fillContainer(mainDeleteText);
        setOKButtonText("Delete");
        init();
    }

    public static void main(String[] args) {
        DeleteDialog dialog = new DeleteDialog(null, "", "");
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(myContentPanel, BorderLayout.CENTER);
        return panel;
    }

    private void fillContainer(String mainDeleteText) {
        String[] itemsToDelete = mainDeleteText.split("\n");
        JLabel deleteText = new JLabel(itemsToDelete[0]);
        deleteText.setBorder(new EmptyBorder(10, 0, 10, 10));
        myContentPanel.add(deleteText, BorderLayout.NORTH);

        if (itemsToDelete.length > 1) {
            Box box = Box.createVerticalBox();
            Arrays.stream(itemsToDelete).skip(1).forEach(item -> {
                if (!item.isEmpty()) {
                    JLabel currentStepLabel = new JLabel(item);
                    currentStepLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
                    box.add(currentStepLabel);
                }
            });
            JScrollPane scroll = new JBScrollPane(box);
            scroll.setBorder(new EmptyBorder(0, 0, 10, 0));
            myContentPanel.add(scroll, BorderLayout.CENTER);
            if(itemsToDelete.length > 16) {
                myContentPanel.setPreferredSize(new Dimension(450, 400));
            }
        }
    }
}
