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
import com.google.common.base.Strings;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.model.CreateDialogModel;
import com.redhat.devtools.intellij.knative.utils.EditorHelper;
import com.redhat.devtools.intellij.knative.utils.KnHelper;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.knative.Constants.YAML_FIRST_IMAGE_PATH;
import static com.redhat.devtools.intellij.knative.Constants.YAML_NAME_PATH;

public class CreateServiceDialog extends CreateDialog {
    private final Logger logger = LoggerFactory.getLogger(CreateServiceDialog.class);
    private JTextField txtValueParam, txtImageParam;
    private DocumentListener txtNameParamListener, txtImageParamListener;

    private final static String DEFAULT_NAME_IN_SNIPPET = "add service name";
    private final static String DEFAULT_FIRST_IMAGE_IN_SNIPPET = "add image url";

    public CreateServiceDialog(CreateDialogModel model) {
        super(model);
        init();
    }

    protected JScrollPane createBasicTabPanel() {
        Box verticalBox = Box.createVerticalBox();

        JPanel nameLabel = createLabelInFlowPanel("Name", "Name of service to be created");
        verticalBox.add(nameLabel);

        Pair<JTextField, DocumentListener> txtNamePair = createJTextField("name", YAML_NAME_PATH);
        txtValueParam = txtNamePair.getLeft();
        txtNameParamListener = txtNamePair.getRight();
        verticalBox.add(txtValueParam);

        JPanel imageLabel = createLabelInFlowPanel("Image", "Image to run (e.g knativesamples/helloworld)");
        verticalBox.add(imageLabel);

        Pair<JTextField, DocumentListener> txtImagePair = createJTextField("image", YAML_FIRST_IMAGE_PATH);
        txtImageParam = txtImagePair.getLeft();
        txtImageParamListener = txtImagePair.getRight();
        verticalBox.add(txtImageParam);

        JCheckBox chkPrivateService = new JBCheckBox();
        chkPrivateService.setText("Make service available only on the cluster-local network.");
        chkPrivateService.addItemListener(getChkPrivateServiceListener());
        chkPrivateService.setBorder(new EmptyBorder(10, 0, 0, 0));
        verticalBox.add(createComponentInFlowPanel(chkPrivateService));

        verticalBox.add(new JPanel(new BorderLayout())); // hack to push components to the top

        JBScrollPane scroll = new JBScrollPane(verticalBox);
        scroll.setBorder(new EmptyBorder(0,0,0,0));

        return scroll;
    }

    protected JScrollPane createEditorPanel() {
        initEditor();
        return  new JBScrollPane(editor.getComponent());
    }

    private void initEditor() {
        String content = "";
        try {
            content = EditorHelper.getSnippet("service").replace("$namespace", model.getNamespace());
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        initEditor("service.yaml", content, createEditorListener());
    }

    private com.intellij.openapi.editor.event.DocumentListener createEditorListener() {
        return new com.intellij.openapi.editor.event.DocumentListener() {
            @Override
            public void documentChanged(com.intellij.openapi.editor.event.DocumentEvent event) {
                try {
                    String nameInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), YAML_NAME_PATH);
                    if (nameInYAML != null
                            && !txtValueParam.getText().equals(nameInYAML)
                            && !nameInYAML.equals(DEFAULT_NAME_IN_SNIPPET)) {
                        txtValueParam.getDocument().removeDocumentListener(txtNameParamListener);
                        txtValueParam.setText(nameInYAML);
                        txtValueParam.getDocument().addDocumentListener(txtNameParamListener);
                    }
                    String imageInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), YAML_FIRST_IMAGE_PATH);
                    if (imageInYAML != null
                            && !txtImageParam.getText().equals(imageInYAML)
                            && !imageInYAML.equals(DEFAULT_FIRST_IMAGE_IN_SNIPPET)) {
                        txtImageParam.getDocument().removeDocumentListener(txtImageParamListener);
                        txtImageParam.setText(imageInYAML);
                        txtImageParam.getDocument().addDocumentListener(txtImageParamListener);
                    }
                } catch (IOException e) {
                }
                setSaveButtonVisibility();
            }
        };
    }

    protected void setSaveButtonVisibility() {
        try {
            String nameInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), YAML_NAME_PATH);
            boolean hasName = !Strings.isNullOrEmpty(nameInYAML) && !nameInYAML.equals(DEFAULT_NAME_IN_SNIPPET);
            String imageInYAML = YAMLHelper.getStringValueFromYAML(editor.getEditor().getDocument().getText(), YAML_FIRST_IMAGE_PATH);
            boolean hasImage = !Strings.isNullOrEmpty(imageInYAML) && !imageInYAML.equals(DEFAULT_FIRST_IMAGE_IN_SNIPPET);
            saveButton.setEnabled(hasName && hasImage);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    private ItemListener getChkPrivateServiceListener() {
        return e -> {
            String yaml = editor.getEditor().getDocument().getText();
            try {
                JsonNode updated;
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updated = YAMLHelper.addLabelToResource(yaml, "networking.knative.dev/visibility", "cluster-local");
                } else {
                    updated = YAMLHelper.removeLabelFromResource(yaml, "networking.knative.dev/visibility");
                }
                updateEditor(updated);
            } catch (IOException ex) {
                logger.warn(ex.getLocalizedMessage(), ex);
            }
        };
    }

    protected void create() throws IOException, KubernetesClientException {
        KnHelper.saveOnCluster(model.getProject(), editor.getEditor().getDocument().getText(), true);
        UIHelper.executeInUI(model.getRefreshFunction());
        UIHelper.executeInUI(() -> super.doOKAction());
    }
}
