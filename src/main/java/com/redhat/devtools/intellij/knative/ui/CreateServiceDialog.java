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
import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
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
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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


import static com.redhat.devtools.intellij.knative.Constants.YAML_FIRST_IMAGE_PATH;
import static com.redhat.devtools.intellij.knative.Constants.YAML_NAME_PATH;

public class CreateServiceDialog extends DialogWrapper {
    private final Logger logger = LoggerFactory.getLogger(CreateServiceDialog.class);
    private JBTabbedPane contentPanel;
    private JPanel footerPanel, logPanel;
    private Project project;
    private JButton cancelButton, saveButton;
    private PsiAwareTextEditorImpl editor;
    private OnePixelSplitter splitterPanel;
    private JTextArea txtAreaEventLog;
    private Runnable refreshFunction;
    private JTextField txtValueParam, txtImageParam;
    private DocumentListener txtNameParamListener, txtImageParamListener;

    private final static String DEFAULT_NAME_IN_SNIPPET = "add service name";
    private final static String DEFAULT_FIRST_IMAGE_IN_SNIPPET = "add image url";

    public CreateServiceDialog(String title, Project project, String namespace, Runnable refreshFunction) {
        super(project, true);
        this.project = project;
        this.refreshFunction = refreshFunction;
        setTitle(title);
        initEditor(namespace);
        buildStructure();
        init();
    }

    private void initEditor(String namespace) {
        String content = "";
        try {
            content = EditorHelper.getSnippet("service").replace("$namespace", namespace);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        editor = new PsiAwareTextEditorImpl(project, new LightVirtualFile("service.yaml", content), TextEditorProvider.getInstance());
        editor.getEditor().getDocument().addDocumentListener(createEditorListener());
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

    private void buildStructure() {
        contentPanel= new JBTabbedPane();
        contentPanel.addTab("Basic", null, createBasicTabPanel(), "Basic");
        contentPanel.addTab("Editor", null, editor.getComponent(), "Editor");

        createLogPanel();

        cancelButton = new JButton(CommonBundle.getCancelButtonText());
        saveButton = new JButton("Create");
        saveButton.setEnabled(false);

        footerPanel = new JPanel(new BorderLayout());
    }

    private JScrollPane createBasicTabPanel() {
        Box verticalBox = Box.createVerticalBox();

        JPanel nameLabel = createLabelInFlowPanel("Name", "Name of service to be created");
        verticalBox.add(nameLabel);

        Pair<JTextField, DocumentListener> txtNamePair = createJTextField(YAML_NAME_PATH);
        txtValueParam = txtNamePair.getLeft();
        txtNameParamListener = txtNamePair.getRight();
        verticalBox.add(txtValueParam);

        JPanel imageLabel = createLabelInFlowPanel("Image", "Image to run (e.g knativesamples/helloworld)");
        verticalBox.add(imageLabel);

        Pair<JTextField, DocumentListener> txtImagePair = createJTextField(YAML_FIRST_IMAGE_PATH);
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

    private DocumentListener createListener(String[] fieldPath, JTextField txtValueParam) {
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

    private void setSaveButtonVisibility() {
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

    private void updateEditor(JsonNode node) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                editor.getEditor().getDocument().setText(YAMLHelper.JSONToYAML(node));
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        });
    }

    private Pair<JTextField, DocumentListener> createJTextField(String ...fieldToUpdate) {
        JTextField txtField = new JTextField("");
        txtField.setMaximumSize(new Dimension(999999, 33));
        DocumentListener listener = createListener(fieldToUpdate, txtField);
        txtField.getDocument().addDocumentListener(listener);
        return Pair.of(txtField, listener);
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
