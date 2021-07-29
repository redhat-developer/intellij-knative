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
package com.redhat.devtools.intellij.knative.ui.createFunc;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreateFuncDialog extends DialogWrapper {

    private Project project;
    private JPanel contentPanel;
    private JTextField txtName, txtChoose;
    private JComboBox cmbRuntime, cmbTemplate;
    private JCheckBox chkOpenInProject;

    public CreateFuncDialog(String title, Project project) {
        super(project, true);
        this.project = project;
        setTitle(title);
        setOKActionEnabled(false);
        buildStructure();
        init();
    }

    public static void main(String[] args) {
        CreateFuncDialog dialog = new CreateFuncDialog("", null);
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    public CreateFuncModel getModel() {
        return new CreateFuncModel(txtName.getText(),
                Paths.get(txtChoose.getText(), txtName.getText()).toString(),
                cmbRuntime.getSelectedItem().toString(),
                cmbTemplate.getSelectedItem().toString(),
                chkOpenInProject.isSelected());
    }

    private void buildStructure() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setPreferredSize(new Dimension(600, 400));
        contentPanel.add(createBasicTabPanel());
    }

    private JScrollPane createBasicTabPanel() {
        Box verticalBox = Box.createVerticalBox();

        JPanel nameLabel = createLabelInFlowPanel("Name", "Name of service to be created");
        verticalBox.add(nameLabel);

        txtName = createJTextField();
        verticalBox.add(txtName);

        JPanel contextLabel = createLabelInFlowPanel("Location", "Pick the folder where to create the new function");
        verticalBox.add(contextLabel);

        JPanel chooser = createChooser();
        verticalBox.add(chooser);

        chkOpenInProject = new JBCheckBox();
        chkOpenInProject.setText("Open within the current project.");
        chkOpenInProject.setBorder(JBUI.Borders.emptyTop(10));
        chkOpenInProject.setSelected(true);
        verticalBox.add(createComponentInFlowPanel(chkOpenInProject));

        JPanel runtimeLabel = createLabelInFlowPanel("Runtime", "Function runtime language/framework");
        verticalBox.add(runtimeLabel);

        cmbRuntime = createComboBox(Arrays.asList("node", "go", "python", "quarkus", "rust", "springboot", "typescript"));
        verticalBox.add(cmbRuntime);

        JPanel imageLabel = createLabelInFlowPanel("Template", "Function template.");
        verticalBox.add(imageLabel);

        cmbTemplate = createComboBox(Arrays.asList("http", "events"));
        verticalBox.add(cmbTemplate);

        verticalBox.add(new JPanel(new BorderLayout())); // hack to push components to the top

        JBScrollPane scroll = new JBScrollPane(verticalBox);
        scroll.setBorder(JBUI.Borders.empty());

        return scroll;
    }

    private JPanel createChooser() {
        JButton btnChoose = new JButton("Choose");
        txtChoose = createJTextField();
        txtChoose.setEnabled(false);

        btnChoose.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this.contentPanel) == JFileChooser.APPROVE_OPTION) {
                txtChoose.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel panelChooser = new JPanel(new BorderLayout());
        panelChooser.setMaximumSize(new Dimension(999999, 33));

        panelChooser.add(txtChoose, BorderLayout.CENTER);
        panelChooser.add(btnChoose, BorderLayout.EAST);
        return panelChooser;
    }

    private JComboBox createComboBox(List<String> options) {
        JComboBox comboBox = new ComboBox();
        options.forEach(comboBox::addItem);
        return comboBox;
    }

    private JTextField createJTextField() {
        JTextField txtField = new JTextField("");
        txtField.setMaximumSize(new Dimension(999999, 33));
        txtField.getDocument().addDocumentListener(createListener());
        return txtField;
    }

    private DocumentListener createListener() {
        return new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                setOKActionEnabled(!txtName.getText().isEmpty() && !txtChoose.getText().isEmpty());
            }
        };
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
