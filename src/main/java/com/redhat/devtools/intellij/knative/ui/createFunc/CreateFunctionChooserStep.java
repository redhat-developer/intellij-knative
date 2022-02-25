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

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import static com.redhat.devtools.intellij.knative.Constants.RUNTIME_FUNCTION_KEY;
import static com.redhat.devtools.intellij.knative.Constants.TEMPLATE_FUNCTION_KEY;

public class CreateFunctionChooserStep extends ModuleWizardStep {

    private final WizardContext context;
    private final CreateFunctionChooserStepUI ui;

    public CreateFunctionChooserStep(WizardContext context) {
        this.context = context;
        this.ui = new CreateFunctionChooserStepUI();
    }

    @Override
    public JComponent getComponent() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(ui.getComponent());
        return contentPanel;
    }

    @Override
    public void updateDataModel() {
        this.context.putUserData(RUNTIME_FUNCTION_KEY, ui.getRuntime());
        this.context.putUserData(TEMPLATE_FUNCTION_KEY, ui.getTemplate());
    }
}
