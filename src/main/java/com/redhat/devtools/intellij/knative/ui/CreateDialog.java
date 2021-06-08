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

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Divider;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.mac.TouchbarDataKeys;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.model.CreateDialogModel;
import com.redhat.devtools.intellij.knative.utils.KnHelper;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CreateDialog extends DialogWrapper {
    private final Logger logger = LoggerFactory.getLogger(CreateDialog.class);
    protected CreateDialogModel model;
    protected JBTabbedPane contentPanel;
    protected JPanel footerPanel, logPanel;
    protected JButton cancelButton, saveButton;
    protected JTextArea txtAreaEventLog;
    protected PsiAwareTextEditorImpl editor;
    protected OnePixelSplitter splitterPanel;

    protected CreateDialog(CreateDialogModel model) {
        super(model.getProject(), true);
        this.model = model;
        setTitle(model.getTitle());
    }

    protected void init() {
        buildStructure();
        super.init();
    }

    private void buildStructure() {
        contentPanel= new JBTabbedPane();
        contentPanel.addTab("Basic", null, createBasicTabPanel(), "Basic");
        contentPanel.addTab("Editor", null, createEditorTabPanel(), "Editor");

        createLogPanel();

        cancelButton = new JButton(CommonBundle.getCancelButtonText());
        saveButton = new JButton("Create");
        saveButton.setEnabled(false);

        footerPanel = new JPanel(new BorderLayout());
    }

    private JPanel createLogPanel() {
        logPanel = new JPanel(new BorderLayout());
        logPanel.add(new JLabel("Event Log"), BorderLayout.NORTH);

        txtAreaEventLog = new JTextArea();
        txtAreaEventLog.setLineWrap(true);
        txtAreaEventLog.setWrapStyleWord(true);
        txtAreaEventLog.setForeground(JBColor.RED);
        txtAreaEventLog.setFont(editor.getEditor().getColorsScheme().getFont(EditorFontType.CONSOLE_PLAIN));
        txtAreaEventLog.setEditable(false);
        logPanel.add(new JBScrollPane(txtAreaEventLog), BorderLayout.CENTER);
        logPanel.setVisible(false);
        return logPanel;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(600, 350));
        splitterPanel = new OnePixelSplitter(true, 1.00F) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(JBColor.namedColor("Plugins.SearchField.borderColor", new JBColor(0xC5C5C5, 0x515151)));
                return divider;
            }
        };
        splitterPanel.setFirstComponent(contentPanel);
        splitterPanel.setSecondComponent(logPanel);
        panel.add(splitterPanel, BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected JComponent createSouthPanel() {
        footerPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel buttonPanel = new JPanel();

        if (SystemInfo.isMac) {
            footerPanel.add(buttonPanel, BorderLayout.EAST);
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

            int index = 0;
            JPanel leftPanel = new JPanel();
            leftPanel.add(cancelButton);
            TouchbarDataKeys.putDialogButtonDescriptor(cancelButton, index++);
            footerPanel.add(leftPanel, BorderLayout.WEST);

            buttonPanel.add(Box.createHorizontalStrut(5));
            buttonPanel.add(saveButton);
            TouchbarDataKeys.putDialogButtonDescriptor(saveButton, index++).setMainGroup(true).setDefault(true);
        }
        else {
            footerPanel.add(buttonPanel, BorderLayout.CENTER);
            GroupLayout layout = new GroupLayout(buttonPanel);
            buttonPanel.setLayout(layout);
            layout.setAutoCreateGaps(true);

            final GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
            final GroupLayout.ParallelGroup vGroup = layout.createParallelGroup();
            final Collection<Component> buttons = new ArrayList<>(5);

            add(hGroup, vGroup, null, Box.createHorizontalGlue());
            add(hGroup, vGroup, buttons, saveButton, cancelButton);

            layout.setHorizontalGroup(hGroup);
            layout.setVerticalGroup(vGroup);
            layout.linkSize(buttons.toArray(new Component[0]));
        }

        saveButton.addActionListener(e -> {
            ExecHelper.submit(() -> {
                try {
                    KnHelper.saveOnCluster(model.getProject(), editor.getEditor().getDocument().getText(), true);
                    UIHelper.executeInUI(model.getRefreshFunction());
                    UIHelper.executeInUI(() -> super.doOKAction());
                } catch (IOException | KubernetesClientException ex) {
                    UIHelper.executeInUI(() -> displayError(ex.getLocalizedMessage()));
                }
            });
        });

        cancelButton.addActionListener(e -> doCancelAction());

        return footerPanel;
    }

    private void displayError(String error) {
        logPanel.setVisible(true);
        txtAreaEventLog.setEditable(true);
        txtAreaEventLog.setText(error);
        txtAreaEventLog.setEditable(false);
        if (splitterPanel.getProportion() > 0.70F) {
            splitterPanel.setProportion(0.70F);
        }
    }

    private void add(final GroupLayout.Group hGroup,
                     final GroupLayout.Group vGroup,
                     @Nullable final Collection<? super Component> collection,
                     final Component... components) {
        for (Component component : components) {
            hGroup.addComponent(component);
            vGroup.addComponent(component);
            if (collection != null) collection.add(component);
        }
    }

    protected void initEditor(String filename, String content, com.intellij.openapi.editor.event.DocumentListener listener) {
        editor = new PsiAwareTextEditorImpl(model.getProject(), new LightVirtualFile(filename, content), TextEditorProvider.getInstance());
        editor.getEditor().getDocument().addDocumentListener(listener);
    }

    protected void updateEditor(String content) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            editor.getEditor().getDocument().setText(content);
        });
    }

    protected void updateEditor(JsonNode node) {
        try {
            updateEditor(YAMLHelper.JSONToYAML(node));
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    protected void updateYamlValueInEditor(String[] fieldPath, String value) {
        String yaml = editor.getEditor().getDocument().getText();
        try {
            JsonNode node = YAMLHelper.editValueInYAML(yaml, fieldPath, value);
            if (node == null) {
                return;
            }
            updateEditor(node);
        } catch(IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    protected Pair<JTextField, DocumentListener> createJTextField(String name, String ...fieldToUpdate) {
        JTextField txtField = new JTextField("");
        txtField.setName(name);
        txtField.setMaximumSize(new Dimension(999999, 33));
        DocumentListener listener = getTextFieldListener(fieldToUpdate, txtField);
        txtField.getDocument().addDocumentListener(listener);
        return Pair.of(txtField, listener);
    }

    protected JLabel createLabel(String name, String tooltip) {
        JLabel label = new JLabel(name);
        label.getFont().deriveFont(Font.BOLD);
        addTooltip(label, tooltip);
        return label;
    }

    protected JPanel createLabelInFlowPanel(String name, String tooltip) {
        JLabel label = createLabel(name, tooltip);
        return createComponentInFlowPanel(label);
    }

    protected JPanel createComponentInFlowPanel(JComponent component) {
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(component);
        return flowPanel;
    }

    private void addTooltip(@NotNull JComponent component, String textToDisplay) {
        if (!textToDisplay.isEmpty()) {
            component.setToolTipText(textToDisplay);
        }
    }

    protected JComboBox createComboBox(String name, List<String> collections, int selectedIndex, ItemListener listener) {
        JComboBox comboBox = new ComboBox();
        if (!name.isEmpty()) {
            comboBox.setName(name);
        }
        collections.forEach(service -> comboBox.addItem(service));
        comboBox.setSelectedIndex(selectedIndex);
        if (listener != null) {
            comboBox.addItemListener(listener);
        }
        return comboBox;
    }

    protected JPanel createPanelWithBorderLayout(Component leftComponent, Component centerComponent, Component rightComponent, int maxHeight) {
        JPanel panel = new JPanel(new BorderLayout());
        if (leftComponent != null) {
            panel.add(leftComponent, BorderLayout.LINE_START);
        }
        if (centerComponent != null) {
            panel.add(centerComponent, BorderLayout.CENTER);
        }
        if (rightComponent != null) {
            panel.add(rightComponent, BorderLayout.LINE_END);
        }
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
        return panel;
    }

    protected DocumentListener getTextFieldListener(String[] fieldPath, JTextField txtValueParam) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            public void update() {
                try {
                    String valueInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), fieldPath);
                    if (valueInYAML != null && !txtValueParam.getText().equals(valueInYAML)) {
                        updateYamlValueInEditor(fieldPath, txtValueParam.getText());
                        setSaveButtonVisibility();
                    }
                } catch (IOException e) {
                }
            }
        };
    }

    protected JScrollPane fitBoxInScrollPane(Box verticalBox) {
        verticalBox.add(new JPanel(new BorderLayout())); // hack to push components to the top

        JBScrollPane scroll = new JBScrollPane(verticalBox);
        scroll.setBorder(new EmptyBorder(0,0,0,0));

        return scroll;
    }

    protected abstract JScrollPane createBasicTabPanel();
    protected abstract JScrollPane createEditorTabPanel();
    protected abstract void setSaveButtonVisibility();
}
