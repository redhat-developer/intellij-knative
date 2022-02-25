package com.redhat.devtools.intellij.knative.ui.createFunc;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.telemetry.TelemetryService;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;

import java.io.File;
import java.io.IOException;

import static com.redhat.devtools.intellij.knative.Constants.RUNTIME_FUNCTION_KEY;
import static com.redhat.devtools.intellij.knative.Constants.TEMPLATE_FUNCTION_KEY;
import static com.redhat.devtools.intellij.knative.telemetry.TelemetryService.NAME_PREFIX_CRUD;

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
public class FunctionBuilderUtils {

    public static void createFunction(String path, String runtime, String template) throws IOException {
        TelemetryMessageBuilder.ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_CRUD + "create func");
        Kn kn = getKn();
        if (kn == null) {
            telemetry
                    .result("Kn cli is null")
                    .send();
            throw new IOException("Unable to create a function project. Function cli not loaded/available." +
                    " Please open up the Knative/Function view to initialize the plugin.");
        }

        CreateFuncModel model = new CreateFuncModel(path, runtime, template);
        try {
            kn.createFunc(model);
            telemetry
                    .success()
                    .send();
        } catch (IOException e) {
            telemetry
                    .error(e.getLocalizedMessage())
                    .send();
            throw e;
        }

    }

    private static Kn getKn() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project: projects) {
            Kn kn = TreeHelper.getKn(project);
            if (kn != null) {
                return kn;
            }
        }
        return null;
    }
}
