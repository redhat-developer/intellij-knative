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
package com.redhat.devtools.intellij.knative.ui.invokeFunc;

import com.google.common.io.Files;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.ui.BaseDialog;
import com.redhat.devtools.intellij.knative.utils.MimeTypes;
import com.redhat.devtools.intellij.knative.utils.model.InvokeModel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.redhat.devtools.intellij.knative.ui.UIConstants.ROW_DIMENSION;

public class InvokeDialog extends BaseDialog {
    private final Logger logger = LoggerFactory.getLogger(InvokeDialog.class);
    private JPanel wrapperPanel, contentPanel, panelPath, panelNamespace, panelURL;
    private JBScrollPane scrollPane;
    private Project project;
    private JTextField txtDataParam, txtIDParam, txtURLParam, txtSourceParam, txtTypeParam;
    private JCheckBox chkURLParam, chkSourceParam, chkTypeParam, chkFormatParam;
    private JRadioButton radioButtonInvokeLocal, radioButtonInvokeRemote;
    private TextFieldWithAutoCompletion<String> txtTypesWithAutoCompletion;
    private ComboBox<String> cmbDataType, cmbFormat;
    private enum INSTANCE {
        LOCAL,
        REMOTE,
        LOCAL_REMOTE
    }
    private INSTANCE funcInstance;
    private final Function function;
    private InvokeModel model;

    private TitledBorder titledBorderInvokeSection;


    private static final String AUTOMATICALLY_GENERATED_TEXT = "Automatically generated";
    private static final String TEXT_OPTION = "Text";
    private static final String FILE_OPTION = "File";
    private static final String LOCAL_OPTION = "Local";
    private static final String REMOTE_OPTION = "Remote";

    public InvokeDialog(String title, Project project, Function function, InvokeModel model) {
        super(project, true);
        this.project = project;
        this.function = function;
        this.model = model;
        setFuncInstance(function);
        setTitle(title);
        setOKButtonText("Invoke");
        buildStructure();
        init();
    }

    private void setFuncInstance(Function function) {
        boolean isLocal = !function.getLocalPath().isEmpty();
        boolean isRemote = function.isPushed();

        if (isLocal && isRemote) {
            this.funcInstance = INSTANCE.LOCAL_REMOTE;
        } else if (isLocal) {
            this.funcInstance = INSTANCE.LOCAL;
        } else {
            this.funcInstance = INSTANCE.REMOTE;
        }
    }

    private void buildStructure() {
        wrapperPanel = new JPanel(new BorderLayout());

        if (funcInstance == INSTANCE.LOCAL_REMOTE) {
            addInstancePicker();
        }

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        fillContentPanel();

        titledBorderInvokeSection = BorderFactory.createTitledBorder(
                new MatteBorder(1, 0, 0, 0, new JTextField().getBackground()),
                "Invoke " + (funcInstance == INSTANCE.REMOTE ? REMOTE_OPTION : LOCAL_OPTION) + " Function"
        );

        scrollPane = new JBScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(titledBorderInvokeSection);

        wrapperPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void fillContentPanel() {
        addIDField();

        if (funcInstance == INSTANCE.LOCAL
                || funcInstance == INSTANCE.LOCAL_REMOTE) {
            panelPath = addPathField();
        }

        boolean hasRemoteOption = funcInstance == INSTANCE.REMOTE || funcInstance == INSTANCE.LOCAL_REMOTE;
        if (hasRemoteOption) {
            panelNamespace = addNamespaceField();
            panelNamespace.setVisible(funcInstance == INSTANCE.REMOTE);
        }

        addDataField();

        addContentTypeField();

        if (hasRemoteOption) {
            chkURLParam = new JCheckBox("<html>Target this custom URL when invoking the function</html>");
            txtURLParam = new JTextField(function.getUrl());
            panelURL = addGroupedComponentsToContent(
                    "URL",
                    chkURLParam,
                    txtURLParam
            );
            panelURL.setVisible(funcInstance == INSTANCE.REMOTE);
        }

        addInstanceButtonListener(radioButtonInvokeLocal,
                "Invoke Local Function",
                Collections.singletonList(panelPath),
                Arrays.asList(panelNamespace, panelURL));
        addInstanceButtonListener(radioButtonInvokeRemote,
                "Invoke Remote Function",
                Arrays.asList(panelNamespace, panelURL),
                Collections.singletonList(panelPath));

        chkSourceParam = new JCheckBox("<html>Use a custom source value for the request data. (Env: $FUNC_SOURCE)" +
                " (default \"/boson/fn\")</html>");
        String defaultSource = System.getenv("FUNC_SOURCE") == null ?
                "/boson/fn" :
                System.getenv("FUNC_SOURCE");
        txtSourceParam = new JTextField(defaultSource);
        addGroupedComponentsToContent(
                "Source",
                chkSourceParam,
                txtSourceParam
        );

        chkTypeParam = new JCheckBox("<html>Use a custom type value for the request data. (Env: $FUNC_TYPE) (default \"boson.fn\")</html>");
        String defaultType = System.getenv("FUNC_TYPE") == null ?
                "boson.fn" :
                System.getenv("FUNC_TYPE");
        txtTypeParam = new JTextField(defaultType);
        addGroupedComponentsToContent(
                "Type",
                chkTypeParam,
                txtTypeParam
        );


        chkFormatParam = new JCheckBox("<html>Select a specific format of the message to be sent. If not chosen, " +
                "it will be selected automatically.</html>");
        cmbFormat = new ComboBox<>();
        cmbFormat.addItem("http");
        cmbFormat.addItem("cloudevent");

        addGroupedComponentsToContent(
                "Format",
                chkFormatParam,
                cmbFormat
        );
    }

    private void addInstanceButtonListener(JRadioButton radioButton, String title, List<JPanel> toBeShown, List<JPanel> toBeHidden) {
        if (radioButton != null) {
            radioButton.addActionListener(e -> {
                titledBorderInvokeSection.setTitle(title);
                toBeHidden.forEach(panel -> panel.setVisible(false));
                toBeShown.forEach(panel -> panel.setVisible(true));
                scrollPane.repaint();
            });
        }
    }

    private void addContentTypeField() {
        JLabel lblContentType = createLabel("Content-Type:",
                "Content Type of the data. (Env: $FUNC_CONTENT_TYPE) (default \"text/plain\")",
                null);
        String defaultContentType = System.getenv("FUNC_CONTENT_TYPE") == null ?
                MimeTypes.TEXT_PLAIN :
                System.getenv("FUNC_CONTENT_TYPE");
        txtTypesWithAutoCompletion = new TextFieldWithAutoCompletion(project,
                new TextFieldWithAutoCompletion.StringsCompletionProvider(
                        MimeTypes.getMime().values(),
                        null),
                true,
                defaultContentType
        ) {
            @Override
            protected EditorEx createEditor() {
                EditorEx editor = super.createEditor();
                CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(new JTextField().getBorder(), JBUI.Borders.empty(7, 7, 3, 7));
                editor.setBorder(compoundBorder);
                editor.getContentSize().setSize(200, new JTextField().getHeight());
                return editor;
            }
        };

        addComponentToContent(contentPanel, lblContentType, txtTypesWithAutoCompletion, null, 0);
    }

    private void addDataField() {
        JLabel lblDataParam = createLabel("Data:",
                "Data to send in the request.",
                null);

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Choose a file to be used as data.");
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        String defaultData = "Hello World";
        txtDataParam = new JTextField(defaultData);
        JPanel filePickerPanel = new JPanel(new BorderLayout());
        filePickerPanel.add(txtDataParam, BorderLayout.CENTER);
        JButton select = new JButton("...");
        select.setPreferredSize(new Dimension(50, select.getHeight()));
        select.setVisible(false);
        filePickerPanel.add(select, BorderLayout.EAST);
        select.addActionListener(e -> {
            int returnValue = jfc.showOpenDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                updateData(selectedFile.getAbsolutePath(), selectedFile.getAbsolutePath(), false);
                String ext = Files.getFileExtension(selectedFile.getName());
                String contentType = MimeTypes.getMime().getOrDefault("." + ext, MimeTypes.APPLICATION_OCTET_STREAM);
                txtTypesWithAutoCompletion.setText(contentType);
            }
        });

        cmbDataType = new ComboBox<>();
        cmbDataType.addItem(TEXT_OPTION);
        cmbDataType.addItem(FILE_OPTION);
        addComponentToContent(contentPanel, lblDataParam, filePickerPanel, cmbDataType, 0);

        cmbDataType.addItemListener(itemEvent -> {
            // when combo box value change
            if (itemEvent.getStateChange() == 1) {
                String optionSelected = (String) itemEvent.getItem();
                if (optionSelected.equals(TEXT_OPTION)) {
                    select.setVisible(false);
                    updateData(defaultData, "", true);
                    txtTypesWithAutoCompletion.setText(MimeTypes.TEXT_PLAIN);
                } else {
                    select.setVisible(true);
                    updateData("", "", false);
                    txtTypesWithAutoCompletion.setText(MimeTypes.APPLICATION_OCTET_STREAM);
                }
                contentPanel.repaint();
            }
        });
    }

    private void updateData(String text, String tooltip, boolean finalEnablingState) {
        if (!txtDataParam.isEnabled()) {
            txtDataParam.setEnabled(true);
        }
        txtDataParam.setText(text);
        txtDataParam.setToolTipText(tooltip);
        txtDataParam.setEnabled(finalEnablingState);
    }

    private JPanel addPathField() {
        JLabel lblPathParam = createLabel("Path:",
                "Path to the Function which should have its instance invoked",
                null);

        JTextField txtPathParam = new JTextField(this.function.getLocalPath());
        txtPathParam.setEnabled(false);
        return addComponentToContent(contentPanel, lblPathParam, txtPathParam, null, 0);
    }

    private JPanel addNamespaceField() {
        JLabel lblNamespaceParam = createLabel("Namespace:",
                "The namespace on the cluster",
                null);

        JTextField txtNamespaceParam = new JTextField(model.getNamespace());
        txtNamespaceParam.setEnabled(false);
        JPanel panelNamespace = addComponentToContent(contentPanel, lblNamespaceParam, txtNamespaceParam, null, 0);
        panelNamespace.setVisible(funcInstance == INSTANCE.REMOTE);
        return panelNamespace;
    }

    private void addIDField() {
        JLabel lblIDParam = createLabel("ID:",
                "ID for the request data. (Env: $FUNC_ID) (default \"ca8758fc-3bcc-4057-871e-5cea37fa215b\")",
                null);

        txtIDParam = new JTextField("Automatically generated");
        txtIDParam.setEnabled(false);

        JCheckBox chkIDCustom = new JCheckBox("<html>Use custom ID</html>");
        chkIDCustom.setBorder(JBUI.Borders.emptyRight(5));
        chkIDCustom.addItemListener(itemEvent -> {
            if (chkIDCustom.isSelected()) {
                txtIDParam.setEnabled(true);
                String defaultID = System.getenv("FUNC_ID") == null ?
                        "ca8758fc-3bcc-4057-871e-5cea37fa215b" :
                        System.getenv("FUNC_ID");
                txtIDParam.setText(defaultID);
            } else {
                txtIDParam.setText(AUTOMATICALLY_GENERATED_TEXT);
                txtIDParam.setEnabled(false);
            }
        });
        addComponentToContent(contentPanel, lblIDParam, txtIDParam, chkIDCustom, 15);
    }

    private void addInstancePicker() {
        JLabel lblTargetParam = createLabel("Invoke instance:",
                "Function instance to invoke",
                JBUI.Borders.empty(0, 3, 0, 15));

        radioButtonInvokeLocal = new JRadioButton(LOCAL_OPTION);
        radioButtonInvokeLocal.setSelected(true);

        radioButtonInvokeRemote = new JRadioButton(REMOTE_OPTION);

        ButtonGroup group = new ButtonGroup();
        group.add(radioButtonInvokeLocal);
        group.add(radioButtonInvokeRemote);
        JPanel radioButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        radioButtonsPanel.add(lblTargetParam);
        radioButtonsPanel.add(radioButtonInvokeLocal);
        radioButtonsPanel.add(radioButtonInvokeRemote);
        radioButtonsPanel.setBorder(JBUI.Borders.emptyBottom(20));

        JPanel panel = createFilledPanel(null, radioButtonsPanel, null);
        wrapperPanel.add(panel, BorderLayout.NORTH);
    }



    private JPanel addGroupedComponentsToContent(String title, JCheckBox chkEnableComponent, JComponent componentCenter) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        Border compoundBorderMargin = BorderFactory.createCompoundBorder(JBUI.Borders.emptyBottom(8), new MatteBorder(1, 1, 1, 1, new JTextField().getBackground()));
        Border compoundBorderMargin2 = BorderFactory.createCompoundBorder(compoundBorderMargin, JBUI.Borders.empty(5, 0));
        TitledBorder titledBorder = BorderFactory.createTitledBorder(compoundBorderMargin2, title);
        panel.setBorder(titledBorder);

        chkEnableComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkEnableComponent.setBorder(JBUI.Borders.emptyBottom(5));
        panel.add(chkEnableComponent);

        componentCenter.setAlignmentX(Component.LEFT_ALIGNMENT);
        componentCenter.setMaximumSize(ROW_DIMENSION);
        componentCenter.setEnabled(false);
        panel.add(componentCenter);

        chkEnableComponent.addItemListener(e -> componentCenter.setEnabled(chkEnableComponent.isSelected()));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(panel);
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return txtDataParam;
    }

    public static void main(String[] args) {
        InvokeDialog dialog = new InvokeDialog("", null,  null, null);
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    @Override
    protected void doOKAction() {
        String ID = txtIDParam.getText();
        if (!ID.equals(AUTOMATICALLY_GENERATED_TEXT)) {
            model.setID(ID);
        }

        String dataType = cmbDataType.getSelectedItem().toString();
        String contentType = txtTypesWithAutoCompletion.getText();

        if (dataType.equals(TEXT_OPTION)) {
            model.setData(txtDataParam.getText());
            model.setContentType(contentType.isEmpty() ? MimeTypes.TEXT_PLAIN : contentType);
        } else {
            model.setFile(txtDataParam.getText());
            model.setContentType(contentType.isEmpty() ? MimeTypes.APPLICATION_OCTET_STREAM : contentType);
        }

        if ((funcInstance.equals(INSTANCE.LOCAL_REMOTE) && radioButtonInvokeLocal.isSelected())
                || funcInstance.equals(INSTANCE.LOCAL)) {
            model.setTarget("local");
        } else if (chkURLParam.isSelected()) {
            model.setTarget(txtURLParam.getText());
        } else {
            model.setTarget("remote");
        }

        if (chkSourceParam.isSelected()) {
            model.setSource(txtSourceParam.getText());
        }

        if (chkTypeParam.isSelected()) {
            model.setType(txtTypeParam.getText());
        }

        if (chkFormatParam.isSelected()) {
            model.setFormat(cmbFormat.getSelectedItem().toString());
        }

        super.doOKAction();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setMinimumSize(new Dimension(500, 400));
        panel.add(wrapperPanel, BorderLayout.CENTER);
        return panel;
    }
}
