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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Divider;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.mac.TouchbarDataKeys;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.utils.EditorHelper;
import com.redhat.devtools.intellij.knative.utils.KnHelper;
import com.sun.tools.javac.comp.Resolve;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateEventSourceDialog extends DialogWrapper {
    private final Logger logger = LoggerFactory.getLogger(CreateEventSourceDialog.class);
    private JBTabbedPane contentPanel;
    private JPanel footerPanel, logPanel;
    private JComboBox[] cmbSourceTypes;
    private JButton cancelButton, saveButton;
    private Project project;
    private PsiAwareTextEditorImpl editor;
    private JTextArea txtAreaEventLog;
    private OnePixelSplitter splitterPanel;
    private Runnable refreshFunction;

    public CreateEventSourceDialog(Project project, String namespace, Runnable refreshFunction, List<String> services) {
        super(project, true);
        this.project = project;
        this.refreshFunction = refreshFunction;
        setTitle("Create Event Source");
        initSourceTypesCombo(namespace);
        buildStructure(namespace, services);
        init();
    }

    private void buildStructure(String namespace, List<String> services) {
        contentPanel= new JBTabbedPane();
        contentPanel.addTab("Basic", null, createBasicTabPanel(services), "Basic");
        contentPanel.addTab("Editor", null, createEditorPanel(namespace), "Editor");

        createLogPanel();

        cancelButton = new JButton(CommonBundle.getCancelButtonText());
        saveButton = new JButton("Create");

        footerPanel = new JPanel(new BorderLayout());
    }

    private JScrollPane createBasicTabPanel(List<String> services) {
        Box verticalBox = Box.createVerticalBox();

        JPanel sourceTypeLabel = createLabelInFlowPanel("Type", "Type of the event source to be created");
        verticalBox.add(sourceTypeLabel);
        verticalBox.add(cmbSourceTypes[0]);

        JPanel nameSourceLabel = createLabelInFlowPanel("Name", "Name of the event source to be created");
        verticalBox.add(nameSourceLabel);

        JTextField txtValueParam = createJTextField("metadata", "name");
        verticalBox.add(txtValueParam);

        JPanel sinkLabel = createLabelInFlowPanel("Sink", "Name of the service to be used as sink");
        verticalBox.add(sinkLabel);

        JComboBox cmbServicesAsSink = new ComboBox();
        services.forEach(service -> cmbServicesAsSink.addItem(service));
        cmbServicesAsSink.setSelectedIndex(-1);
        verticalBox.add(cmbServicesAsSink);

        verticalBox.add(new JPanel(new BorderLayout())); // hack to push components to the top

        JBScrollPane scroll = new JBScrollPane(verticalBox);
        scroll.setBorder(new EmptyBorder(0,0,0,0));

        return scroll;
    }

    private JScrollPane createEditorPanel(String namespace) {
        Box verticalBox = Box.createVerticalBox();

        JPanel sourceTypeLabel = createLabelInFlowPanel("Type", "Type of the event source to be created");
        verticalBox.add(sourceTypeLabel);
        verticalBox.add(cmbSourceTypes[1]);

        String content = "";
        try {
            content = EditorHelper.getSnippet("service").replace("$namespace", namespace);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        editor = new PsiAwareTextEditorImpl(project, new LightVirtualFile("service.yaml", content), TextEditorProvider.getInstance());
        verticalBox.add(editor.getComponent());

        verticalBox.add(new JPanel(new BorderLayout())); // hack to push components to the top

        JBScrollPane scroll = new JBScrollPane(verticalBox);
        scroll.setBorder(new EmptyBorder(0,0,0,0));

        return scroll;
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

    private JPanel createLabelInFlowPanel(String name, String tooltip) {
        JLabel label = new JLabel(name);
        label.getFont().deriveFont(Font.BOLD);
        // addTooltip(label, tooltip);
        return createComponentInFlowPanel(label);
    }

    private JPanel createComponentInFlowPanel(JComponent component) {
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowPanel.add(component);
        return flowPanel;
    }

    private JTextField createJTextField(String ...fieldToUpdate) {
        JTextField txtField = new JTextField("");
        txtField.setMaximumSize(new Dimension(999999, 33));
        // addListener(fieldToUpdate, txtField);
        return txtField;
    }

    private void initSourceTypesCombo(String namespace) {
        ComboBox cmbInBasicTab = createSourceTypesCombo("basic");
        ComboBox cmbInEditorTab = createSourceTypesCombo("editor");
        this.cmbSourceTypes = new ComboBox[] { cmbInBasicTab, cmbInEditorTab };
        addListenerSourceTypes(cmbInBasicTab, namespace);
        addListenerSourceTypes(cmbInEditorTab, namespace);
    }

    private ComboBox createSourceTypesCombo(String name) {
        ComboBox cmbSourceTypes = new ComboBox();
        cmbSourceTypes.addItem("ApiSource");
        cmbSourceTypes.addItem("PingSource");
        cmbSourceTypes.addItem("SinkBinding");
        cmbSourceTypes.setName(name);
        return cmbSourceTypes;
    }

    private void addListenerSourceTypes(ComboBox comboBox, String namespace) {
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String sourceType = e.getItem().toString();
                syncSecondSourceTypesCombo((ComboBox)e.getSource(), sourceType);
                showSnippetInEditor(sourceType, namespace);
            }
        });
    }

    private void syncSecondSourceTypesCombo(ComboBox comboBox, String newValue) {
        Arrays.stream(cmbSourceTypes).filter(cmb -> !cmb.getName().equals(comboBox.getName())).forEach(cmb -> {
            ItemListener[] listeners = cmb.getItemListeners();
            Arrays.stream(listeners).forEach(listener -> cmb.removeItemListener(listener));
            cmb.setSelectedItem(newValue);
            Arrays.stream(listeners).forEach(listener -> cmb.addItemListener(listener));
        });
    }

    private void showSnippetInEditor(String sourceType, String namespace) {
        String content = "";
        switch (sourceType) {
            case "ApiSource": {
                try {
                    content = EditorHelper.getSnippet("apiserversource").replace("$namespace", namespace);
                } catch (IOException e) { }
                break;
            }
            case "PingSource": {
                try {
                    content = EditorHelper.getSnippet("pingsource").replace("$namespace", namespace);
                } catch (IOException e) { }
                break;
            }
            case "SinkBinding": {
                try {
                    content = EditorHelper.getSnippet("sinkbinding");
                } catch (IOException e) { }
                break;
            }
            default: {
                break;
            }
        }
        updateEditor(content);
    }

    private void updateEditor(String content) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            editor.getEditor().getDocument().setText(content);
        });
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
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
                    KnHelper.saveOnCluster(this.project, editor.getEditor().getDocument().getText(), true);
                    UIHelper.executeInUI(refreshFunction);
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
}
