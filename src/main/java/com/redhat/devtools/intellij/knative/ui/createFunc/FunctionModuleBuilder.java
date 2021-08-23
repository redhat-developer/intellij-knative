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

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.InvalidDataException;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import static com.redhat.devtools.intellij.knative.Constants.RUNTIME_FUNCTION_KEY;
import static com.redhat.devtools.intellij.knative.Constants.TEMPLATE_FUNCTION_KEY;

public class FunctionModuleBuilder extends ModuleBuilder {

    private WizardContext wizardContext;

    public FunctionModuleBuilder() {super();}

    @Override
    public ModuleType<?> getModuleType() {
        return ModuleType.EMPTY;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public @Nullable String getBuilderId() {
        return "function";
    }

    @Override
    public Icon getNodeIcon() {
        return IconLoader.findIcon("/images/knative-logo.svg", FunctionModuleBuilder.class);
    }

    @Override
    public String getDescription() {
        return "Create <b>Function</b> projects provided by IntelliJ Knative";
    }

    @Override
    public String getPresentableName() {
        return "Function";
    }

    @Override
    public @Nullable ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return new CreateFunctionChooserStep(context);
    }

    @Override
    public @NotNull Module createModule(@NotNull ModifiableModuleModel moduleModel) throws InvalidDataException, IOException, ModuleWithNameAlreadyExists, JDOMException, ConfigurationException {
        createFunction();
        return super.createModule(moduleModel);
    }

    private void createFunction() throws IOException {
        Kn kn = getKn();
        if (kn == null) {
            throw new IOException("Unable to create a function project. Function cli not available.");
        }

        File moduleFile = new File(getContentEntryPath());
        String runtime = wizardContext.getUserData(RUNTIME_FUNCTION_KEY);
        String template = wizardContext.getUserData(TEMPLATE_FUNCTION_KEY);
        CreateFuncModel model = new CreateFuncModel(moduleFile.getPath(), runtime, template);
        kn.createFunc(model);
    }

    private Kn getKn() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project: projects) {
            Kn kn = TreeHelper.getKn(project);
            if (kn != null) {
                return kn;
            }
        }
        return null;
    }

    @Override
    public ModuleWizardStep[] createFinishingSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        this.wizardContext = wizardContext;
        return super.createFinishingSteps(wizardContext, modulesProvider);
    }
}
