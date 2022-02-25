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
import com.intellij.openapi.module.ModuleTypeId;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.InvalidDataException;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.File;
import java.io.IOException;

import static com.redhat.devtools.intellij.knative.Constants.GO_MODULE_TYPE_ID;
import static com.redhat.devtools.intellij.knative.Constants.GO_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.NODE_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.PYTHON_MODULE_TYPE_ID;
import static com.redhat.devtools.intellij.knative.Constants.PYTHON_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.QUARKUS_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.RUNTIME_FUNCTION_KEY;
import static com.redhat.devtools.intellij.knative.Constants.RUST_MODULE_TYPE_ID;
import static com.redhat.devtools.intellij.knative.Constants.RUST_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.SPRINGBOOT_RUNTIME;
import static com.redhat.devtools.intellij.knative.Constants.TEMPLATE_FUNCTION_KEY;
import static com.redhat.devtools.intellij.knative.Constants.TYPESCRIPT_RUNTIME;

public class FunctionModuleBuilder extends ModuleBuilder {

    private WizardContext wizardContext;

    public FunctionModuleBuilder() {super();}

    @Override
    public ModuleType<?> getModuleType() {
        if (wizardContext == null || wizardContext.getUserData(RUNTIME_FUNCTION_KEY) == null) {
            return ModuleTypeManager.getInstance().getDefaultModuleType();
        }
        return getModuleByRuntime(wizardContext.getUserData(RUNTIME_FUNCTION_KEY));
    }

    private ModuleType<?> getModuleByRuntime(String runtime) {
        switch (runtime) {
            case PYTHON_RUNTIME: {
                return ModuleTypeManager.getInstance().findByID(PYTHON_MODULE_TYPE_ID);
            }
            case GO_RUNTIME: {
                return ModuleTypeManager.getInstance().findByID(GO_MODULE_TYPE_ID);
            }
            case NODE_RUNTIME:
            case TYPESCRIPT_RUNTIME: {
                return ModuleTypeManager.getInstance().findByID(ModuleTypeId.WEB_MODULE);
            }
            case QUARKUS_RUNTIME:
            case SPRINGBOOT_RUNTIME: {
                return ModuleTypeManager.getInstance().findByID(ModuleTypeId.JAVA_MODULE);
            }
            case RUST_RUNTIME: {
                return ModuleTypeManager.getInstance().findByID(RUST_MODULE_TYPE_ID);
            }
            default:
                return ModuleTypeManager.getInstance().getDefaultModuleType();
        }
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
        File moduleFile = new File(getContentEntryPath());
        String runtime = wizardContext.getUserData(RUNTIME_FUNCTION_KEY);
        String template = wizardContext.getUserData(TEMPLATE_FUNCTION_KEY);
        FunctionBuilderUtils.createFunction(moduleFile.getPath(), runtime, template);
        return super.createModule(moduleModel);
    }

    @Override
    public ModuleWizardStep[] createFinishingSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        this.wizardContext = wizardContext;
        return super.createFinishingSteps(wizardContext, modulesProvider);
    }
}
