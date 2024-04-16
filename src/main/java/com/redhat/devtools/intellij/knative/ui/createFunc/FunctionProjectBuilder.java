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
package com.redhat.devtools.intellij.knative.ui.createFunc;

import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectGeneratorPeer;
import com.intellij.platform.WebProjectGenerator;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.IOException;

public class FunctionProjectBuilder extends WebProjectTemplate {

    private CreateFunctionChooserStepUI ui;

    @Override
    public String getDescription() {
        return "Create <b>Function</b> projects provided by IntelliJ Knative";
    }

    @NotNull
    @Override
    public String getName() {
        return "Function";
    }

    @Override
    public Icon getIcon() {
        return IconLoader.findIcon("/images/knative-logo.svg", FunctionProjectBuilder.class);
    }

    @Override
    public void generateProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Object settings, @NotNull Module module) {
        try {
            FunctionBuilderUtils.createFunction(project.getBasePath(), ui.getRuntime(), ui.getTemplate());
        } catch (IOException e) {
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
        }
    }

    @NotNull
    @Override
    public ProjectGeneratorPeer<Object> createPeer() {
        return new ProjectGeneratorPeer<Object>() {
            @NotNull
            @Override
            public JComponent getComponent() {
                ui = new CreateFunctionChooserStepUI();
                JPanel contentPanel = new JPanel(new BorderLayout());
                contentPanel.add(ui.getComponent());
                return contentPanel;
            }

            @Override
            public void buildUI(@NotNull SettingsStep settingsStep) {
                settingsStep.addSettingsComponent(getComponent());
            }

            @NotNull
            @Override
            public Object getSettings() {
                return new Object();
            }

            @Nullable
            @Override
            public ValidationInfo validate() {
                return null;
            }

            @Override
            public boolean isBackgroundJobRunning() {
                return false;
            }

            @Override
            public void addSettingsListener(@NotNull ProjectGeneratorPeer.SettingsListener listener) {
                ProjectGeneratorPeer.super.addSettingsListener(listener);
            }
        };
    }
}
