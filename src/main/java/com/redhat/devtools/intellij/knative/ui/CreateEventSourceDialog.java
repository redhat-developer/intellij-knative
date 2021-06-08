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
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.model.CreateDialogModel;
import com.redhat.devtools.intellij.knative.utils.CronUtils;
import com.redhat.devtools.intellij.knative.utils.EditorHelper;
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


import static com.redhat.devtools.intellij.knative.Constants.API_SOURCE;
import static com.redhat.devtools.intellij.knative.Constants.CUSTOM_SOURCE;
import static com.redhat.devtools.intellij.knative.Constants.PING_SOURCE;
import static com.redhat.devtools.intellij.knative.Constants.YAML_API_SOURCE_SERVICE_ACCOUNT;
import static com.redhat.devtools.intellij.knative.Constants.YAML_API_VERSION_PATH;
import static com.redhat.devtools.intellij.knative.Constants.YAML_KIND_PATH;
import static com.redhat.devtools.intellij.knative.Constants.YAML_NAME_PATH;
import static com.redhat.devtools.intellij.knative.Constants.YAML_PING_SOURCE_CONTENT_TYPE;
import static com.redhat.devtools.intellij.knative.Constants.YAML_PING_SOURCE_DATA;
import static com.redhat.devtools.intellij.knative.Constants.YAML_PING_SOURCE_SCHEDULE;
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

    /**
     * Create new basic tab
     * @return scrollPane containing all elements to being displayed in the basic tab
     */
    protected JScrollPane createBasicTabPanel() {
        Box verticalBox = Box.createVerticalBox();

        JPanel sourceTypeLabel = createLabelInFlowPanel("Type", "Type of the event source to be created");
        verticalBox.add(sourceTypeLabel); // label
        verticalBox.add(cmbSourceTypes[0]); // combo where to select source types

        sourceTypeBox = Box.createVerticalBox();
        updateSourceTypeBox(cmbSourceTypes[0].getSelectedItem().toString());
        verticalBox.add(sourceTypeBox); // panel containing elements based on type selected

        return fitBoxInScrollPane(verticalBox);
    }

    /**
     * Update the panel containing elements based on the source type selected (apiSource, pingSource, ...)
     *
     * @param sourceType the source type selected (apiSource, pingSource, ...)
     */
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

    /**
     * Update the panel containing elements needed to create a pingSource
     */
    private void updateSourceTypeBoxAsPingSource() {
        addNameLabelAndTextField(); // add label and textfield to write name new pingsource

        JPanel scheduleLabel = createLabelInFlowPanel("Schedule", "Schedule how often the PingSource should send an event");
        sourceTypeBox.add(scheduleLabel); // label schedule

        JComboBox cmbScheduleTimeUnits = createComboBox("timeUnit", Arrays.asList("minutes", "hours", "days"), 0, null);

        JSpinner spinnerSchedule = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE,1));
        spinnerSchedule.setEditor(new JSpinner.NumberEditor(spinnerSchedule, "#"));
        JTextField spinnerTextFieldSchedule = ((JSpinner.NumberEditor)spinnerSchedule.getEditor()).getTextField();
        spinnerTextFieldSchedule.setName("spinnerSchedule");
        PropertyChangeListener spinnerListener = evt -> {
            String value = spinnerTextFieldSchedule.getText();
            String timeUnit = cmbScheduleTimeUnits.getSelectedItem().toString();
            String timeInCronTabFormat = CronUtils.convertTimeToCronTabFormat(value, timeUnit);
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
                    String timeInCronTabFormat = CronUtils.convertTimeToCronTabFormat(value, timeUnit);
                    updateYamlValueInEditor(new String[]{"spec", "schedule"}, timeInCronTabFormat);
                }
            }
        };
        cmbScheduleTimeUnits.addItemListener(timeUnitsListener);
        activeComponents.add(Pair.of(cmbScheduleTimeUnits, timeUnitsListener));

        JPanel panel = createPanelWithBorderLayout(null, spinnerSchedule, cmbScheduleTimeUnits, cmbScheduleTimeUnits.getHeight());
        sourceTypeBox.add(panel); // add panel containing spinner (number value) and combo to specify scheduling (eg. 1 minute, 2 hours, ..)

        JPanel messageLabel = createLabelInFlowPanel("Message", "Message contained by the event sent");
        sourceTypeBox.add(messageLabel); // add label for message

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
        sourceTypeBox.add(messagePanel); // add panel containing combo and textfield to specify the message and its type (e.g "text/plain" - message)

        addSinkLabelAndCombo(); // add label and combo to define a sink
    }

    /**
     * Update the panel containing elements needed to create a apiSource
     */
    private void updateSourceTypeBoxAsApiSource() {
        addNameLabelAndTextField();  // add label and textfield to write name new apiSource

        JPanel serviceAccountLabel = createLabelInFlowPanel("Service Account", "Name of the service account to be used");
        sourceTypeBox.add(serviceAccountLabel); // add label for serviceAccount

        ItemListener saListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String sa = e.getItem().toString();
                updateYamlValueInEditor(YAML_API_SOURCE_SERVICE_ACCOUNT, sa);
            }
        };
        JComboBox cmbServiceAccount = createComboBox("serviceAccount", model.getServiceAccounts(), -1, saListener);
        activeComponents.add(Pair.of(cmbServiceAccount, saListener));
        sourceTypeBox.add(cmbServiceAccount); // add combo to pick serviceAccount to use

        addSinkLabelAndCombo(); // add label and combo to define a sink
    }

    /**
     * Create and add new label and textfield to specify the name of a new source
     */
    private void addNameLabelAndTextField() {
        JPanel nameSourceLabel = createLabelInFlowPanel("Name", "Name of the event source to be created");
        sourceTypeBox.add(nameSourceLabel);

        Pair<JTextField, DocumentListener> nameTextFieldAndListener = createJTextField("name", "metadata", "name");
        activeComponents.add(Pair.of(nameTextFieldAndListener.getLeft(), nameTextFieldAndListener.getRight()));
        sourceTypeBox.add(nameTextFieldAndListener.getLeft());
    }

    /**
     * Create and add new label and combo to pick the sink to be used
     */
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

    /**
     * Create new editor tab
     * @return scrollPane containing all elements to being displayed in the editor tab
     */
    protected JScrollPane createEditorTabPanel() {
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

        return fitBoxInScrollPane(verticalBox);
    }

    /**
     * Create editor listener to trigger further actions when editor changes
     * @return EditorListener
     */
    private com.intellij.openapi.editor.event.DocumentListener createEditorListener() {
        return new com.intellij.openapi.editor.event.DocumentListener() {
            @Override
            public void documentChanged(com.intellij.openapi.editor.event.DocumentEvent event) {
                updateBasicTabFieldsWithEditorValues();
                setSaveButtonVisibility();
            }
        };
    }

    /**
     * Changes basic tab fields based on changes occurred in editor to keep both tabs synchronized
     */
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

    /**
     * Update basic tab when apiSource is selected
     * @throws IOException if error occurred while parsing editor yaml
     */
    private void updateApiSourceBasicTabFields() throws IOException {
        updateNameApiSourceBasicTabTextField();
        updateServiceAccountBasicTabComboBox();
        updateSinkBasicTabComboBox();
    }

    /**
     * Update basic tab when pingSource is selected
     * @throws IOException if error occurred while parsing editor yaml
     */
    private void updatePingSourceBasicTabFields() throws IOException {
        updateNamePingSourceBasicTabTextField();
        updateTimeBasicTabFields();
        updateDataBasicTabTextField();
        updateMessageTypeBasicTabComboBox();
        updateSinkBasicTabComboBox();
    }

    /**
     * Update scheduling fields (combo and spinner) with content written in editor
     * @throws IOException if error occurred while parsing editor yaml
     */
    private void updateTimeBasicTabFields() throws IOException {
        String cronTabInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), YAML_PING_SOURCE_SCHEDULE);
        Pair<String, String> timeAndUnitPair = CronUtils.convertCronTabFormatInTimeAndUnitPair(cronTabInYAML);
        updateBasicTabTextField(timeAndUnitPair.getLeft(),"spinnerSchedule", "0");
        updateBasicTabComboBox(timeAndUnitPair.getRight(), "timeUnit", "");
    }

    /**
     * Update apiSource name textField in basic tab with value taken from editor YAML
     * @throws IOException if error occurred while parsing editor yaml
     */
    private void updateNameApiSourceBasicTabTextField() throws IOException {
        updateBasicTabTextField(YAML_NAME_PATH, "name", DEFAULT_API_SOURCE_NAME_IN_SNIPPET);
    }

    /**
     * Update pingSource name textField in basic tab with value taken from editor YAML
     * @throws IOException if error occurred while parsing editor yaml
     */
    private void updateNamePingSourceBasicTabTextField() throws IOException {
        updateBasicTabTextField(YAML_NAME_PATH, "name", DEFAULT_PING_SOURCE_NAME_IN_SNIPPET);
    }

    /**
     * Update data textField in basic tab with value taken from editor YAML
     * @throws IOException if error occurred while parsing editor yaml
     */
    private void updateDataBasicTabTextField() throws IOException {
        updateBasicTabTextField(YAML_PING_SOURCE_DATA, "data", DEFAULT_DATA_IN_SNIPPET);
    }

    /**
     *  Update generic textField in basic tab with value taken from editor YAML
     * @param yamlPath YAML path of the value to get
     * @param textFieldName name of the textField to update
     * @param defaultValue default value
     * @throws IOException if error occurred while parsing editor yaml
     */
    private void updateBasicTabTextField(String[] yamlPath, String textFieldName, String defaultValue) throws IOException {
        String dataInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), yamlPath);
        updateBasicTabTextField(dataInYAML, textFieldName, defaultValue);
    }

    /**
     *  Update generic textfield in basic tab with value taken from editor YAML
     * @param dataInYAML YAML value to use to update textField
     * @param textFieldName name of the textField to update
     * @param defaultValue default value
     */
    private void updateBasicTabTextField(String dataInYAML, String textFieldName, String defaultValue) {
        Optional<Pair<Component, EventListener>> dataTextBoxAndListener = activeComponents.stream().filter(pair -> pair.getLeft().getName().equals(textFieldName)).findFirst();
        if (dataInYAML != null
                && !dataInYAML.equals(defaultValue)
                && dataTextBoxAndListener.isPresent()
                && !((JTextField)dataTextBoxAndListener.get().getLeft()).getText().equals(dataInYAML)) {
            JTextField textField = ((JTextField)dataTextBoxAndListener.get().getLeft());
            EventListener listener = dataTextBoxAndListener.get().getRight();
            if (listener instanceof DocumentListener) {
                textField.getDocument().removeDocumentListener((DocumentListener) listener);
                textField.setText(dataInYAML);
                textField.getDocument().addDocumentListener((DocumentListener) listener);
            } else if (listener instanceof PropertyChangeListener) {
                textField.removePropertyChangeListener((PropertyChangeListener) listener);
                textField.setText(dataInYAML);
                textField.addPropertyChangeListener((PropertyChangeListener) listener);
            }

        }
    }

    /**
     * Update serviceAccount comboBox with serviceAccount taken from editor YAML
     * @throws IOException if error occurs while parsing editor yaml
     */
    private void updateServiceAccountBasicTabComboBox() throws IOException {
        updateBasicTabComboBox(YAML_API_SOURCE_SERVICE_ACCOUNT, "serviceAccount", DEFAULT_SERVICE_ACCOUNT_IN_SNIPPET);
    }

    /**
     * Update messageType comboBox with type taken from editor YAML
     * @throws IOException if error occurs while parsing editor yaml
     */
    private void updateMessageTypeBasicTabComboBox() throws IOException {
        updateBasicTabComboBox(YAML_PING_SOURCE_CONTENT_TYPE, "messageType", DEFAULT_CONTENT_TYPE_IN_SNIPPET);
    }

    /**
     * Update sink comboBox with sink taken from editor YAML
     * @throws IOException if error occurs while parsing editor yaml
     */
    private void updateSinkBasicTabComboBox() throws IOException {
        updateBasicTabComboBox(YAML_SOURCE_SINK, "sink", DEFAULT_SINK_IN_SNIPPET);
    }

    /**
     * Update generic comboBox with value taken from editor YAML
     * @param yamlPath YAML path of the value to get
     * @param comboName name of the comboBox to update
     * @param defaultValue default value
     * @throws IOException if error occurs while parsing editor yaml
     */
    private void updateBasicTabComboBox(String[] yamlPath, String comboName, String defaultValue) throws IOException {
        String dataInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), yamlPath);
        updateBasicTabComboBox(dataInYAML, comboName, defaultValue);
    }

    /**
     * Update generic comboBox with value taken from editor YAML
     * @param dataInYAML YAML value to use to update comboBox
     * @param comboName name of the comboBox to update
     * @param defaultValue default value
     */
    private void updateBasicTabComboBox(String dataInYAML, String comboName, String defaultValue) {
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

    /**
     * Set visibility of the Create button
     */
    protected void setSaveButtonVisibility() {
        saveButton.setEnabled(canCreateNewResource());
    }

    /**
     * Verify if the user has inserted all needed values to create a new event source
     * @return true if a new source can be created, false otherwise
     */
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

    /**
     * Verify if the user has inserted all needed values to create a new apiSource
     * @return true if a new apiSource can be created, false otherwise
     * @throws IOException if an error occurs while parsing YAML in editor
     */
    private boolean canCreateApiSource() throws IOException {
        return canCreateSource(Arrays.asList(
                Pair.of(YAML_NAME_PATH, DEFAULT_PING_SOURCE_NAME_IN_SNIPPET),
                Pair.of(YAML_API_SOURCE_SERVICE_ACCOUNT, DEFAULT_SERVICE_ACCOUNT_IN_SNIPPET),
                Pair.of(YAML_SOURCE_SINK, DEFAULT_SINK_IN_SNIPPET)
        ));
    }

    /**
     * Verify if the user has inserted all needed values to create a new pingSource
     * @return true if a new pingSource can be created, false otherwise
     * @throws IOException if an error occurs while parsing YAML in editor
     */
    private boolean canCreatePingSource() throws IOException {
        return canCreateSource(Arrays.asList(
                Pair.of(YAML_NAME_PATH, DEFAULT_PING_SOURCE_NAME_IN_SNIPPET),
                Pair.of(YAML_PING_SOURCE_DATA, DEFAULT_DATA_IN_SNIPPET),
                Pair.of(YAML_SOURCE_SINK, DEFAULT_SINK_IN_SNIPPET)
        ));
    }

    /**
     * Verify if the user has inserted all needed values to create a new customSource
     * @return true if a new customSource can be created, false otherwise
     * @throws IOException if an error occurs while parsing YAML in editor
     */
    private boolean canCreateCustomSource() throws IOException {
        return canCreateSource(Arrays.asList(
                Pair.of(YAML_API_VERSION_PATH, DEFAULT_CUSTOM_SOURCE_APIVERSION_IN_SNIPPET),
                Pair.of(YAML_KIND_PATH, DEFAULT_CUSTOM_SOURCE_KIND_IN_SNIPPET),
                Pair.of(YAML_NAME_PATH, DEFAULT_CUSTOM_SOURCE_NAME_IN_SNIPPET),
                Pair.of(YAML_SOURCE_SINK, DEFAULT_SINK_IN_SNIPPET)
                ));
    }

    /**
     * Verify if the user has inserted all needed values to create a new source
     * @param yamlPathsAndDefaultValues collections of YAML paths to being verified and their default values
     * @return true if a new source can be created, false otherwise
     * @throws IOException if an error occurs while parsing YAML in editor
     */
    private boolean canCreateSource(List<Pair<String[], String>> yamlPathsAndDefaultValues) throws IOException {
        for(Pair<String[], String> yamlPathAndDefaultValue: yamlPathsAndDefaultValues) {
            String valueInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), yamlPathAndDefaultValue.getLeft());
            if (Strings.isNullOrEmpty(valueInYAML) || valueInYAML.equals(yamlPathAndDefaultValue.getRight())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Initializes the two comboBoxes (one in Basic Tab and one in Editor Tab) containing the source types that can be created within the dialog
     */
    private void initSourceTypesCombo() {
        JComboBox cmbInBasicTab = createComboBox("basic", Arrays.asList(API_SOURCE, PING_SOURCE), 0,null);
        JComboBox cmbInEditorTab = createComboBox("editor", Arrays.asList(API_SOURCE, PING_SOURCE, CUSTOM_SOURCE), 0,null);
        this.cmbSourceTypes = new JComboBox[] { cmbInBasicTab, cmbInEditorTab };
        addSourceTypesComboBoxListener(cmbInBasicTab);
        addSourceTypesComboBoxListener(cmbInEditorTab);
    }

    private void addSourceTypesComboBoxListener(JComboBox comboBox) {
        comboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String sourceType = e.getItem().toString();
                syncSecondSourceTypesCombo((JComboBox) e.getSource(), sourceType);
                showSnippetInEditor(sourceType, model.getNamespace());
                updateSourceTypeBox(sourceType);
            }
        });
    }

    /**
     * Synchronizes the comboBox visible in the other tab
     * @param comboBox comboBox which was updated
     * @param newValue new value to update other combo
     */
    private void syncSecondSourceTypesCombo(JComboBox comboBox, String newValue) {
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

    /**
     * Verify if comboBox contains a value
     * @param comboBox combo to use for the search
     * @param value value to search for
     * @return true if the comboBox contains that value, false otherwise
     */
    private  boolean hasItem(JComboBox comboBox, String value) {
        int size = comboBox.getItemCount();
        for (int i = 0; i < size; i++) {
            if (comboBox.getItemAt(i).toString().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update editor with the snippet of the source type selected
     * @param sourceType source type selected
     * @param namespace active namespace on cluster
     */
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
}
