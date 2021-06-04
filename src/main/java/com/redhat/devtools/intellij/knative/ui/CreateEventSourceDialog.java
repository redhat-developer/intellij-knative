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
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.panels.VerticalBox;
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
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
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
    private Box sourceTypeBox;
    private Runnable refreshFunction;

    public CreateEventSourceDialog(Project project, String namespace, Runnable refreshFunction, List<String> services, List<String> serviceAccounts) {
        super(project, true);
        this.project = project;
        this.refreshFunction = refreshFunction;
        setTitle("Create Event Source");
        initSourceTypesCombo(namespace, services, serviceAccounts);
        buildStructure(namespace, services, serviceAccounts);
        init();
    }

    private void buildStructure(String namespace, List<String> services, List<String> serviceAccounts) {
        contentPanel= new JBTabbedPane();
        contentPanel.addTab("Basic", null, createBasicTabPanel(services, serviceAccounts), "Basic");
        contentPanel.addTab("Editor", null, createEditorPanel(namespace), "Editor");

        createLogPanel();

        cancelButton = new JButton(CommonBundle.getCancelButtonText());
        saveButton = new JButton("Create");

        footerPanel = new JPanel(new BorderLayout());
    }

    private JScrollPane createBasicTabPanel(List<String> services, List<String> serviceAccounts) {
        Box verticalBox = Box.createVerticalBox();

        JPanel sourceTypeLabel = createLabelInFlowPanel("Type", "Type of the event source to be created");
        verticalBox.add(sourceTypeLabel);
        verticalBox.add(cmbSourceTypes[0]);

        sourceTypeBox = Box.createVerticalBox();
        updateSourceTypeBox(cmbSourceTypes[0].getSelectedItem().toString(), services, serviceAccounts);
        verticalBox.add(sourceTypeBox);

        verticalBox.add(new JPanel(new BorderLayout())); // hack to push components to the top

        JBScrollPane scroll = new JBScrollPane(verticalBox);
        scroll.setBorder(new EmptyBorder(0,0,0,0));

        return scroll;
    }

    private void updateSourceTypeBox(String sourceType, List<String> services, List<String> serviceAccounts) {
        sourceTypeBox.removeAll();

        switch (sourceType) {
            case "ApiSource": {
                updateSourceTypeBoxAsApiSource(services, serviceAccounts);
                break;
            }
            case "PingSource": {
                updateSourceTypeBoxAsPingSource(services);
                break;
            }
        }

        sourceTypeBox.revalidate();
        sourceTypeBox.repaint();


    }

    private void updateSourceTypeBoxAsPingSource(List<String> services) {
        JPanel nameSourceLabel = createLabelInFlowPanel("Name", "Name of the event source to be created");
        sourceTypeBox.add(nameSourceLabel);

        JTextField txtValueParam = createJTextField("metadata", "name");
        sourceTypeBox.add(txtValueParam);

        JPanel scheduleLabel = createLabelInFlowPanel("Schedule", "Schedule how often the PingSource should send an event");
        sourceTypeBox.add(scheduleLabel);

        JComboBox cmbScheduleTimeUnits = new ComboBox();
        cmbScheduleTimeUnits.addItem("minutes");
        cmbScheduleTimeUnits.addItem("hours");
        cmbScheduleTimeUnits.addItem("days");

        JSpinner spinnerSchedule = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE,1));
        spinnerSchedule.setName("spinnerSchedule");
        spinnerSchedule.setEditor(new JSpinner.NumberEditor(spinnerSchedule, "#"));
        JTextField spinnerTextFieldSchedule = ((JSpinner.NumberEditor)spinnerSchedule.getEditor()).getTextField();
        spinnerTextFieldSchedule.addPropertyChangeListener(evt -> {
            String value = spinnerTextFieldSchedule.getText();
            String timeUnit = cmbScheduleTimeUnits.getSelectedItem().toString();
            String timeInCronTabFormat = convertTimeToCronTabFormat(value, timeUnit);
            updateYamlValueInEditor(new String[]{"spec", "schedule"}, timeInCronTabFormat);
        });
        ((NumberFormatter)((JFormattedTextField) spinnerTextFieldSchedule).getFormatter()).setAllowsInvalid(false);

        JPanel panel = new JPanel(new BorderLayout());
        //panel.setBorder(new EmptyBorder(7, 0, 0, 0));


        cmbScheduleTimeUnits.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String value = spinnerTextFieldSchedule.getText();
                if (!value.equals("0")) {
                    String timeUnit = e.getItem().toString();
                    String timeInCronTabFormat = convertTimeToCronTabFormat(value, timeUnit);
                    updateYamlValueInEditor(new String[]{"spec", "contentType"}, timeInCronTabFormat);
                }
            }
        });

        panel.add(spinnerSchedule, BorderLayout.CENTER);
        panel.add(cmbScheduleTimeUnits, BorderLayout.LINE_END);

        sourceTypeBox.add(panel);

        JPanel messageLabel = createLabelInFlowPanel("Message", "Message contained by the event sent");
        sourceTypeBox.add(messageLabel);

        JTextField txtMessageData = createJTextField("spec", "data");

        JComboBox cmbMessageTypes = new ComboBox();
        cmbMessageTypes.addItem("application/json");
        cmbMessageTypes.addItem("text/plain");
        cmbMessageTypes.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String contentType = e.getItem().toString();
                updateYamlValueInEditor(new String[] { "spec", "contentType" }, contentType);
            }
        });



        JPanel messagePanel = new JPanel(new BorderLayout());
        //panel.setBorder(new EmptyBorder(7, 0, 0, 0));

        messagePanel.add(cmbMessageTypes, BorderLayout.LINE_START);
        messagePanel.add(txtMessageData, BorderLayout.CENTER);
        sourceTypeBox.add(messagePanel);

        JPanel sinkLabel = createLabelInFlowPanel("Sink", "Name of the service to be used as sink");
        sourceTypeBox.add(sinkLabel);

        JComboBox cmbServicesAsSink = new ComboBox();
        services.forEach(service -> cmbServicesAsSink.addItem(service));
        cmbServicesAsSink.setSelectedIndex(-1);
        cmbServicesAsSink.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String service = e.getItem().toString();
                updateYamlValueInEditor(new String[] { "spec", "sink", "ref", "name" }, service);
            }
        });
        sourceTypeBox.add(cmbServicesAsSink);
    }

    private String convertTimeToCronTabFormat(String value, String unit) {
        switch(unit) {
            case "minutes": {
                return "*/" + value + " * * * *";
            }
            case "hours": {
                return "* */" + value + " * * *";
            }
            case "days": {
                return "* * */" + value + " * *";
            }
            default: {
                return "";
            }
        }
    }

    private void updateSourceTypeBoxAsApiSource(List<String> services, List<String> serviceAccounts) {
        JPanel nameSourceLabel = createLabelInFlowPanel("Name", "Name of the event source to be created");
        sourceTypeBox.add(nameSourceLabel);

        JTextField txtValueParam = createJTextField("metadata", "name");
        sourceTypeBox.add(txtValueParam);

        JPanel serviceAccountLabel = createLabelInFlowPanel("Service Account", "Name of the service account to be used");
        sourceTypeBox.add(serviceAccountLabel);

        JComboBox cmbServiceAccount = new ComboBox();
        serviceAccounts.forEach(sa -> cmbServiceAccount.addItem(sa));
        cmbServiceAccount.setSelectedIndex(-1);
        cmbServiceAccount.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String sa = e.getItem().toString();
                updateYamlValueInEditor(new String[] { "spec", "serviceAccountName" }, sa);
            }
        });
        sourceTypeBox.add(cmbServiceAccount);

        JPanel sinkLabel = createLabelInFlowPanel("Sink", "Name of the service to be used as sink");
        sourceTypeBox.add(sinkLabel);

        JComboBox cmbServicesAsSink = new ComboBox();
        services.forEach(service -> cmbServicesAsSink.addItem(service));
        cmbServicesAsSink.setSelectedIndex(-1);
        cmbServicesAsSink.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String service = e.getItem().toString();
                updateYamlValueInEditor(new String[] { "spec", "sink", "ref", "name" }, service);
            }
        });
        sourceTypeBox.add(cmbServicesAsSink);
    }

    private JScrollPane createEditorPanel(String namespace) {
        Box verticalBox = Box.createVerticalBox();

        JPanel sourceTypeLabel = createLabelInFlowPanel("Type", "Type of the event source to be created");
        verticalBox.add(sourceTypeLabel);
        verticalBox.add(cmbSourceTypes[1]);

        String content = "";
        try {
            content = EditorHelper.getSnippet("apiserversource").replace("$namespace", namespace);
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
        addListenerToTextField(fieldToUpdate, txtField);
        return txtField;
    }

    private void addListenerToTextField(String[] fieldPath, JTextField txtValueParam) {
        txtValueParam.getDocument().addDocumentListener(new DocumentListener() {
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
                if (!txtValueParam.getText().isEmpty()) {
                    updateYamlValueInEditor(fieldPath, txtValueParam.getText());
                }
            }
        });
    }

    private void initSourceTypesCombo(String namespace, List<String> services, List<String> serviceAccounts) {
        ComboBox cmbInBasicTab = createSourceTypesCombo("basic");
        ComboBox cmbInEditorTab = createSourceTypesCombo("editor");
        this.cmbSourceTypes = new ComboBox[] { cmbInBasicTab, cmbInEditorTab };
        addListenerSourceTypes(cmbInBasicTab, namespace, services, serviceAccounts);
        addListenerSourceTypes(cmbInEditorTab, namespace, services, serviceAccounts);
    }

    private ComboBox createSourceTypesCombo(String name) {
        ComboBox cmbSourceTypes = new ComboBox();
        cmbSourceTypes.addItem("ApiSource");
        cmbSourceTypes.addItem("PingSource");
        cmbSourceTypes.addItem("SinkBinding");
        cmbSourceTypes.setName(name);
        return cmbSourceTypes;
    }

    private void addListenerSourceTypes(ComboBox comboBox, String namespace, List<String> services, List<String> serviceAccounts) {
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String sourceType = e.getItem().toString();
                syncSecondSourceTypesCombo((ComboBox)e.getSource(), sourceType);
                showSnippetInEditor(sourceType, namespace);
                updateSourceTypeBox(sourceType, services, serviceAccounts);
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

    private void updateEditor(JsonNode node) {
        try {
            updateEditor(YAMLHelper.JSONToYAML(node));
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }

    }

    private void updateYamlValueInEditor(String[] fieldPath, String value) {
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
