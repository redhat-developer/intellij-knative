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
package com.redhat.devtools.intellij.knative.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Dimension;

import static com.redhat.devtools.intellij.knative.ui.UIConstants.ROW_DIMENSION;


public abstract class BaseDialog extends DialogWrapper {

    protected BaseDialog(@Nullable Project project, boolean canBeParent) {
        super(project, canBeParent);
    }

    protected JPanel addComponentToContent(JPanel parent, JComponent componentLeft, JComponent componentCenter, JComponent componentRight, int top) {
        JPanel panel = createFilledPanel(componentLeft, componentCenter, componentRight);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(JBUI.Borders.empty(top, 0, 5, 0));
        parent.add(panel);
        return panel;
    }

    protected JPanel createFilledPanel(JComponent componentLeft, JComponent componentCenter, JComponent componentRight) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        if (componentLeft != null) {
            componentLeft.setBorder(JBUI.Borders.emptyLeft(10));
            componentLeft.setMinimumSize(new Dimension(120, 40));
            componentLeft.setPreferredSize(new Dimension(120, 40));
            componentLeft.setMaximumSize(new Dimension(120, 40));
            panel.add(componentLeft);
        }
        if (componentCenter != null) {
            componentCenter.setMaximumSize(ROW_DIMENSION);
            panel.add(componentCenter);
        }
        if (componentRight != null) {
            componentRight.setMaximumSize(new Dimension(150, 40));
            panel.add(componentRight);
        }
        return panel;
    }

    protected JLabel createLabel(String text, String tooltip, Border border) {
        JLabel label = new JLabel(text);
        if (!tooltip.isEmpty()) {
            label.setToolTipText(tooltip);
        }
        if (border != null) {
            label.setBorder(border);
        }
        return label;
    }
}
