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
package com.redhat.devtools.intellij.knative.ui.createFunc;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;

import static com.redhat.devtools.intellij.knative.Constants.GO_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.NODE_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.PYTHON_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.QUARKUS_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.RUST_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.SPRINGBOOT_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.TYPESCRIPT_RUNTIME;

public class CreateFunctionChooserStepUI {

    private JBScrollPane scroll;
    private JComboBox cmbRuntime, cmbTemplate;

    public CreateFunctionChooserStepUI(){
        create();
    }

    public JBScrollPane getComponent() {
        return this.scroll;
    }

    public String getRuntime() {
        return cmbRuntime.getSelectedItem().toString();
    }

    public String getTemplate() {
        return cmbTemplate.getSelectedItem().toString();
    }

    private JScrollPane create() {
        Box verticalBox = Box.createVerticalBox();

        JPanel runtimeLabel = createLabelInFlowPanel("Runtime", "Function runtime language/framework");
        verticalBox.add(runtimeLabel);

        cmbRuntime = createComboBox(Arrays.asList(NODE_RUNTIME, GO_RUNTIME, PYTHON_RUNTIME, QUARKUS_RUNTIME, RUST_RUNTIME, SPRINGBOOT_RUNTIME, TYPESCRIPT_RUNTIME));
        verticalBox.add(cmbRuntime);

        JPanel imageLabel = createLabelInFlowPanel("Template", "Function template.");
        verticalBox.add(imageLabel);

        cmbTemplate = createComboBox(Arrays.asList("http", "events"));
        verticalBox.add(cmbTemplate);

        verticalBox.add(new JPanel(new BorderLayout())); // hack to push components to the top

        scroll = new JBScrollPane(verticalBox);
        scroll.setBorder(JBUI.Borders.empty());

        return scroll;
    }

    private JComboBox createComboBox(List<String> options) {
        JComboBox comboBox = new ComboBox();
        options.forEach(comboBox::addItem);
        return comboBox;
    }

    private JPanel createLabelInFlowPanel(String name, String tooltip) {
        JLabel label = new JLabel(name);
        label.getFont().deriveFont(Font.BOLD);
        addTooltip(label, tooltip);
        return createComponentInFlowPanel(label);
    }

    private JPanel createComponentInFlowPanel(JComponent component) {
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(component);
        return flowPanel;
    }

    private void addTooltip(@NotNull JComponent component, String textToDisplay) {
        if (!textToDisplay.isEmpty()) {
            component.setToolTipText(textToDisplay);
        }
    }

}
