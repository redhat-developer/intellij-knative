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

public class CreateFuncModel {

    private String name, path, runtime, template;
    private boolean importInProject;

    public CreateFuncModel(String name, String path, String runtime, String template, boolean importInProject) {
        this.name = name;
        this.path = path;
        this.runtime = runtime;
        this.template = template;
        this.importInProject = importInProject;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getTemplate() {
        return template;
    }

    public boolean isImportInProject() {
        return importInProject;
    }
}
