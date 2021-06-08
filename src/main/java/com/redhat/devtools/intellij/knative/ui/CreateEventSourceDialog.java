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

import com.google.common.base.Strings;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.model.CreateDialogModel;
import com.redhat.devtools.intellij.knative.utils.EditorHelper;
import com.redhat.devtools.intellij.knative.utils.KnHelper;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.knative.Constants.YAML_API_SOURCE_SERVICE_ACCOUNT;
import static com.redhat.devtools.intellij.knative.Constants.YAML_API_VERSION_PATH;
import static com.redhat.devtools.intellij.knative.Constants.YAML_KIND_PATH;
import static com.redhat.devtools.intellij.knative.Constants.YAML_NAME_PATH;
import static com.redhat.devtools.intellij.knative.Constants.YAML_PING_SOURCE_CONTENT_TYPE;
import static com.redhat.devtools.intellij.knative.Constants.YAML_PING_SOURCE_DATA;
import static com.redhat.devtools.intellij.knative.Constants.YAML_SOURCE_SINK;

public class CreateEventSourceDialog extends CreateDialog {
    private final Logger logger = LoggerFactory.getLogger(CreateEventSourceDialog.class);
    private JComboBox[] cmbSourceTypes;
    private Box sourceTypeBox;
    private List<Pair<Component, EventListener>> activeComponents;

    private static final String DEFAULT_API_SOURCE_NAME_IN_SNIPPET = "<apiserversource>";
    private static final String DEFAULT_PING_SOURCE_NAME_IN_SNIPPET = "<pingsource>";
    private static final String DEFAULT_DATA_IN_SNIPPET = "<data>";
    private static final String DEFAULT_SERVICE_ACCOUNT_IN_SNIPPET = "<service-account>";
    private static final String DEFAULT_SINK_IN_SNIPPET = "<sink>";
    private static final String DEFAULT_CONTENT_TYPE_IN_SNIPPET = "application/json";
    private static final String DEFAULT_CUSTOM_SOURCE_APIVERSION_IN_SNIPPET = "<source-apiversion>";
    private static final String DEFAULT_CUSTOM_SOURCE_KIND_IN_SNIPPET = "<source-kind>";
    private static final String DEFAULT_CUSTOM_SOURCE_NAME_IN_SNIPPET = "<customsource>";

    public CreateEventSourceDialog(CreateDialogModel model) {
        super(model);
        this.activeComponents = new ArrayList<>();
        initSourceTypesCombo();
        init();
    }

    protected JScrollPane createBasicTabPanel() {
        Box verticalBox = Box.createVerticalBox();

        JPanel sourceTypeLabel = createLabelInFlowPanel("Type", "Type of the event source to be created");
        verticalBox.add(sourceTypeLabel);
        verticalBox.add(cmbSourceTypes[0]);

        sourceTypeBox = Box.createVerticalBox();
        updateSourceTypeBox(cmbSourceTypes[0].getSelectedItem().toString());
        verticalBox.add(sourceTypeBox);

        verticalBox.add(new JPanel(new BorderLayout())); // hack to push components to the top

        JBScrollPane scroll = new JBScrollPane(verticalBox);
        scroll.setBorder(new EmptyBorder(0,0,0,0));

        return scroll;
    }

    private void updateSourceTypeBox(String sourceType) {
        sourceTypeBox.removeAll();
        activeComponents.clear();

        switch (sourceType) {
            case "ApiSource": {
                updateSourceTypeBoxAsApiSource();
                break;
            }
            case "PingSource": {
                updateSourceTypeBoxAsPingSource();
                break;
            }
        }

        sourceTypeBox.revalidate();
        sourceTypeBox.repaint();

    }

    private void updateSourceTypeBoxAsPingSource() {
        addNameLabelAndTextField();

        JPanel scheduleLabel = createLabelInFlowPanel("Schedule", "Schedule how often the PingSource should send an event");
        sourceTypeBox.add(scheduleLabel);

        JComboBox cmbScheduleTimeUnits = createComboBox("timeUnit", Arrays.asList("minutes", "hours", "days"), 0, null);

        JSpinner spinnerSchedule = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE,1));
        spinnerSchedule.setName("spinnerSchedule");
        spinnerSchedule.setEditor(new JSpinner.NumberEditor(spinnerSchedule, "#"));
        JTextField spinnerTextFieldSchedule = ((JSpinner.NumberEditor)spinnerSchedule.getEditor()).getTextField();
        PropertyChangeListener spinnerListener = evt -> {
            String value = spinnerTextFieldSchedule.getText();
            String timeUnit = cmbScheduleTimeUnits.getSelectedItem().toString();
            String timeInCronTabFormat = convertTimeToCronTabFormat(value, timeUnit);
            updateYamlValueInEditor(new String[]{"spec", "schedule"}, timeInCronTabFormat);
        };
        spinnerTextFieldSchedule.addPropertyChangeListener(spinnerListener);
        ((NumberFormatter)((JFormattedTextField) spinnerTextFieldSchedule).getFormatter()).setAllowsInvalid(false);
        activeComponents.add(Pair.of(spinnerTextFieldSchedule, spinnerListener));

        ItemListener timeUnitsListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String value = spinnerTextFieldSchedule.getText();
                if (!value.equals("0")) {
                    String timeUnit = e.getItem().toString();
                    String timeInCronTabFormat = convertTimeToCronTabFormat(value, timeUnit);
                    updateYamlValueInEditor(new String[]{"spec", "schedule"}, timeInCronTabFormat);
                }
            }
        };
        cmbScheduleTimeUnits.addItemListener(timeUnitsListener);
        activeComponents.add(Pair.of(cmbScheduleTimeUnits, timeUnitsListener));

        JPanel panel = createPanelWithBorderLayout(null, spinnerSchedule, cmbScheduleTimeUnits, cmbScheduleTimeUnits.getHeight());
        sourceTypeBox.add(panel);

        JPanel messageLabel = createLabelInFlowPanel("Message", "Message contained by the event sent");
        sourceTypeBox.add(messageLabel);

        Pair<JTextField, DocumentListener> dataTextFieldAndListener = createJTextField("data", "spec", "data");
        activeComponents.add(Pair.of(dataTextFieldAndListener.getLeft(), dataTextFieldAndListener.getRight()));

        ItemListener messageTypesListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String contentType = e.getItem().toString();
                updateYamlValueInEditor(new String[] { "spec", "contentType" }, contentType);
            }
        };
        JComboBox cmbMessageTypes = createComboBox("messageType", Arrays.asList("application/json", "text/plain"), 0, messageTypesListener);
        activeComponents.add(Pair.of(cmbMessageTypes, messageTypesListener));
        JPanel messagePanel = createPanelWithBorderLayout(cmbMessageTypes, dataTextFieldAndListener.getLeft(), null, cmbMessageTypes.getHeight());
        sourceTypeBox.add(messagePanel);

        addSinkLabelAndCombo();
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

    private void updateSourceTypeBoxAsApiSource() {
        addNameLabelAndTextField();

        JPanel serviceAccountLabel = createLabelInFlowPanel("Service Account", "Name of the service account to be used");
        sourceTypeBox.add(serviceAccountLabel);

        ItemListener saListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String sa = e.getItem().toString();
                updateYamlValueInEditor(new String[] { "spec", "serviceAccountName" }, sa);
            }
        };
        JComboBox cmbServiceAccount = createComboBox("serviceAccount", model.getServiceAccounts(), -1, saListener);
        activeComponents.add(Pair.of(cmbServiceAccount, saListener));
        sourceTypeBox.add(cmbServiceAccount);

        addSinkLabelAndCombo();
    }

    private void addNameLabelAndTextField() {
        JPanel nameSourceLabel = createLabelInFlowPanel("Name", "Name of the event source to be created");
        sourceTypeBox.add(nameSourceLabel);

        Pair<JTextField, DocumentListener> nameTextFieldAndListener = createJTextField("name", "metadata", "name");
        activeComponents.add(Pair.of(nameTextFieldAndListener.getLeft(), nameTextFieldAndListener.getRight()));
        sourceTypeBox.add(nameTextFieldAndListener.getLeft());
    }

    private void addSinkLabelAndCombo() {
        JPanel sinkLabel = createLabelInFlowPanel("Sink", "Name of the service to be used as sink");
        sourceTypeBox.add(sinkLabel);

        ItemListener sinkListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String service = e.getItem().toString();
                updateYamlValueInEditor(new String[] { "spec", "sink", "ref", "name" }, service);
            }
        };
        JComboBox cmbServicesAsSink = createComboBox("sink", model.getServices(), -1, sinkListener);
        activeComponents.add(Pair.of(cmbServicesAsSink, sinkListener));
        sourceTypeBox.add(cmbServicesAsSink);
    }

    protected JScrollPane createEditorPanel() {
        Box verticalBox = Box.createVerticalBox();

        JLabel sourceTypeLabel = createLabel("Type", "Type of the event source to be created");
        sourceTypeLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        JPanel sourceTypePanel = createPanelWithBorderLayout(sourceTypeLabel, cmbSourceTypes[1], null, cmbSourceTypes[1].getHeight());
        sourceTypePanel.setBorder(new EmptyBorder(5, 0, 15, 0));
        verticalBox.add(sourceTypePanel);

        String content = "";
        try {
            content = EditorHelper.getSnippet("apiserversource").replace("$namespace", model.getNamespace());
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        initEditor("eventsource.yaml", content, createEditorListener());
        verticalBox.add(editor.getComponent());

        verticalBox.add(new JPanel(new BorderLayout())); // hack to push components to the top

        JBScrollPane scroll = new JBScrollPane(verticalBox);
        scroll.setBorder(new EmptyBorder(0,0,0,0));

        return scroll;
    }

    private com.intellij.openapi.editor.event.DocumentListener createEditorListener() {
        return new com.intellij.openapi.editor.event.DocumentListener() {
            @Override
            public void documentChanged(com.intellij.openapi.editor.event.DocumentEvent event) {
                updateBasicTabFieldsWithEditorValues();
                setSaveButtonVisibility();
            }
        };
    }

    private void updateBasicTabFieldsWithEditorValues() {
        try {
            JComboBox comboInEditorTab = cmbSourceTypes[1];
            switch (comboInEditorTab.getSelectedItem().toString()) {
                case "ApiSource": {
                    updateApiSourceBasicTabFields();
                    break;
                }
                case "PingSource": {
                    updatePingSourceBasicTabFields();
                    break;
                }
                default:
                    break;
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    private void updateApiSourceBasicTabFields() throws IOException {
        updateNameApiSourceBasicTabTextField();
        updateServiceAccountBasicTabComboBox();
        updateSinkBasicTabComboBox();
    }

    private void updatePingSourceBasicTabFields() throws IOException {
        updateNamePingSourceBasicTabTextField();
        updateDataBasicTabTextField();
        updateMessageTypeBasicTabComboBox();
        updateSinkBasicTabComboBox();
    }

    private void updateNameApiSourceBasicTabTextField() throws IOException {
        updateBasicTabTextField(YAML_NAME_PATH, "name", DEFAULT_API_SOURCE_NAME_IN_SNIPPET);
    }

    private void updateNamePingSourceBasicTabTextField() throws IOException {
        updateBasicTabTextField(YAML_NAME_PATH, "name", DEFAULT_PING_SOURCE_NAME_IN_SNIPPET);
    }

    private void updateDataBasicTabTextField() throws IOException {
        updateBasicTabTextField(YAML_PING_SOURCE_DATA, "data", DEFAULT_DATA_IN_SNIPPET);
    }

    private void updateBasicTabTextField(String[] yamlPath, String textFieldName, String defaultValue) throws IOException {
        String dataInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), yamlPath);
        Optional<Pair<Component, EventListener>> dataTextBoxAndListener = activeComponents.stream().filter(pair -> pair.getLeft().getName().equals(textFieldName)).findFirst();
        if (dataInYAML != null
                && !dataInYAML.equals(defaultValue)
                && dataTextBoxAndListener.isPresent()
                && !((JTextField)dataTextBoxAndListener.get().getLeft()).getText().equals(dataInYAML)) {
            JTextField textField = ((JTextField)dataTextBoxAndListener.get().getLeft());
            textField.getDocument().removeDocumentListener((DocumentListener) dataTextBoxAndListener.get().getRight());
            textField.setText(dataInYAML);
            textField.getDocument().addDocumentListener((DocumentListener) dataTextBoxAndListener.get().getRight());
        }
    }

    private void updateServiceAccountBasicTabComboBox() throws IOException {
        updateBasicTabComboBox(YAML_API_SOURCE_SERVICE_ACCOUNT, "serviceAccount", DEFAULT_SERVICE_ACCOUNT_IN_SNIPPET);
    }

    private void updateMessageTypeBasicTabComboBox() throws IOException {
        updateBasicTabComboBox(YAML_PING_SOURCE_CONTENT_TYPE, "messageType", DEFAULT_CONTENT_TYPE_IN_SNIPPET);
    }

    private void updateSinkBasicTabComboBox() throws IOException {
        updateBasicTabComboBox(YAML_SOURCE_SINK, "sink", DEFAULT_SINK_IN_SNIPPET);
    }

    private void updateBasicTabComboBox(String[] yamlPath, String comboName, String defaultValue) throws IOException {
        String dataInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), yamlPath);
        Optional<Pair<Component, EventListener>> dataComboAndListener = activeComponents.stream().filter(pair -> pair.getLeft().getName().equals(comboName)).findFirst();
        if (dataInYAML != null
                && !dataInYAML.equals(defaultValue)
                && dataComboAndListener.isPresent()
                && (((JComboBox)dataComboAndListener.get().getLeft()).getSelectedIndex() == -1
                || !((JComboBox)dataComboAndListener.get().getLeft()).getSelectedItem().toString().equals(dataInYAML)
        )) {
            JComboBox comboBox = ((JComboBox)dataComboAndListener.get().getLeft());
            comboBox.removeItemListener((ItemListener) dataComboAndListener.get().getRight());
            comboBox.setSelectedItem(dataInYAML);
            if (comboBox.getSelectedIndex() != -1 && !comboBox.getSelectedItem().toString().equals(dataInYAML)) {
                comboBox.setSelectedIndex(-1);
            }
            comboBox.addItemListener((ItemListener) dataComboAndListener.get().getRight());
        }
    }

    protected void setSaveButtonVisibility() {
        saveButton.setEnabled(canCreateNewResource());
    }

    private boolean canCreateNewResource() {
        try {
            JComboBox comboInEditorTab = cmbSourceTypes[1];
            switch (comboInEditorTab.getSelectedItem().toString()) {
                case "ApiSource": {
                    return canCreateApiSource();
                }
                case "PingSource": {
                    return canCreatePingSource();
                }
                default: {
                    return canCreateCustomSource();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private boolean canCreateApiSource() throws IOException {
        return canCreateSource(Arrays.asList(
                Pair.of(YAML_NAME_PATH, DEFAULT_PING_SOURCE_NAME_IN_SNIPPET),
                Pair.of(YAML_API_SOURCE_SERVICE_ACCOUNT, DEFAULT_SERVICE_ACCOUNT_IN_SNIPPET),
                Pair.of(YAML_SOURCE_SINK, DEFAULT_SINK_IN_SNIPPET)
        ));
    }

    private boolean canCreatePingSource() throws IOException {
        return canCreateSource(Arrays.asList(
                Pair.of(YAML_NAME_PATH, DEFAULT_PING_SOURCE_NAME_IN_SNIPPET),
                Pair.of(YAML_PING_SOURCE_DATA, DEFAULT_DATA_IN_SNIPPET),
                Pair.of(YAML_SOURCE_SINK, DEFAULT_SINK_IN_SNIPPET)
        ));
    }

    private boolean canCreateCustomSource() throws IOException {
        return canCreateSource(Arrays.asList(
                Pair.of(YAML_API_VERSION_PATH, DEFAULT_CUSTOM_SOURCE_APIVERSION_IN_SNIPPET),
                Pair.of(YAML_KIND_PATH, DEFAULT_CUSTOM_SOURCE_KIND_IN_SNIPPET),
                Pair.of(YAML_NAME_PATH, DEFAULT_CUSTOM_SOURCE_NAME_IN_SNIPPET),
                Pair.of(YAML_SOURCE_SINK, DEFAULT_SINK_IN_SNIPPET)
                ));
    }

    private boolean canCreateSource(List<Pair<String[], String>> yamlPathsAndDefaultValues) throws IOException {
        for(Pair<String[], String> yamlPathAndDefaultValue: yamlPathsAndDefaultValues) {
            String valueInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), yamlPathAndDefaultValue.getLeft());
            if (Strings.isNullOrEmpty(valueInYAML) || valueInYAML.equals(yamlPathAndDefaultValue.getRight())) {
                return false;
            }
        }
        return true;
    }

    private void initSourceTypesCombo() {
        ComboBox cmbInBasicTab = createSourceTypesCombo("basic");
        ComboBox cmbInEditorTab = createSourceTypesCombo("editor");
        cmbInEditorTab.addItem("CustomSource");
        this.cmbSourceTypes = new ComboBox[] { cmbInBasicTab, cmbInEditorTab };
        addListenerSourceTypes(cmbInBasicTab);
        addListenerSourceTypes(cmbInEditorTab);
    }

    private ComboBox createSourceTypesCombo(String name) {
        ComboBox cmbSourceTypes = new ComboBox();
        cmbSourceTypes.addItem("ApiSource");
        cmbSourceTypes.addItem("PingSource");
        cmbSourceTypes.setName(name);
        return cmbSourceTypes;
    }

    private void addListenerSourceTypes(ComboBox comboBox) {
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String sourceType = e.getItem().toString();
                syncSecondSourceTypesCombo((ComboBox) e.getSource(), sourceType);
                showSnippetInEditor(sourceType, model.getNamespace());
                updateSourceTypeBox(sourceType);
            }
        });
    }

    private void syncSecondSourceTypesCombo(ComboBox comboBox, String newValue) {
        Arrays.stream(cmbSourceTypes).filter(cmb -> !cmb.getName().equals(comboBox.getName())).forEach(cmb -> {
            ItemListener[] listeners = cmb.getItemListeners();
            Arrays.stream(listeners).forEach(listener -> cmb.removeItemListener(listener));
            if (hasItem(cmb, newValue)) {
                cmb.setSelectedItem(newValue);
            } else {
                cmb.setSelectedIndex(-1);
            }
            Arrays.stream(listeners).forEach(listener -> cmb.addItemListener(listener));
        });
    }

    private  boolean hasItem(JComboBox comboBox, String value) {
        int size = comboBox.getItemCount();
        for (int i = 0; i < size; i++) {
            if (comboBox.getItemAt(i).toString().equals(value)) {
                return true;
            }
        }
        return false;
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
            case "CustomSource": {
                try {
                    content = EditorHelper.getSnippet("customsource").replace("$namespace", namespace);
                } catch (IOException e) { }
                break;
            }
            default: {
                break;
            }
        }
        updateEditor(content);
    }

    protected void create() throws IOException, KubernetesClientException {
        KnHelper.saveOnCluster(model.getProject(), editor.getEditor().getDocument().getText(), true);
        UIHelper.executeInUI(model.getRefreshFunction());
        UIHelper.executeInUI(() -> super.doOKAction());
    }
}
