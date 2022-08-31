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
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.utils.MimeTypes;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        try {
            Kn kn = FunctionBuilderUtils.getKn();
            if (kn == null) {
                verticalBox.add(
                        new JLabel(
                                "An error occurred while connecting to the cluster. " +
                                    "Please verify your cluster is active and working")
                );
                throw new IOException("");
            }

            Map<String, List<String>> templates = kn.getFuncTemplates();

            JPanel runtimeLabel = createLabelInFlowPanel("Runtime", "Function runtime language/framework");
            verticalBox.add(runtimeLabel);
            cmbRuntime = createComboBox(new ArrayList<>(templates.keySet()));
            verticalBox.add(cmbRuntime);

            JPanel imageLabel = createLabelInFlowPanel("Template", "Function template.");
            verticalBox.add(imageLabel);

            List<String> initialTemplateList = cmbRuntime.getSelectedItem() == null || cmbRuntime.getSelectedItem().toString().isEmpty()
                    ? Collections.emptyList()
                    : templates.get(cmbRuntime.getSelectedItem().toString());
            cmbTemplate = createComboBox(initialTemplateList);
            cmbRuntime.addItemListener(itemEvent -> {
                // when combo box value change
                if (itemEvent.getStateChange() == 1) {
                    String runtimeSelected = (String) itemEvent.getItem();
                    fillComboBox(cmbTemplate, templates.get(runtimeSelected));
                    scroll.invalidate();
                }
            });
            verticalBox.add(cmbTemplate);


        } catch (IOException e) {
            verticalBox.add(
                    new JLabel(
                            "An error occurred while connecting to the cluster. " +
                                    "Please verify your cluster is active and working")
            );
        }



        verticalBox.add(new JPanel(new BorderLayout())); // hack to push components to the top

        scroll = new JBScrollPane(verticalBox);
        scroll.setBorder(JBUI.Borders.empty());

        return scroll;
    }

    private JComboBox createComboBox(List<String> options) {
        JComboBox comboBox = new ComboBox();
        fillComboBox(comboBox, options);
        return comboBox;
    }

    private void fillComboBox(JComboBox comboBox, List<String> options) {
        comboBox.removeAllItems();
        options.forEach(comboBox::addItem);
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
