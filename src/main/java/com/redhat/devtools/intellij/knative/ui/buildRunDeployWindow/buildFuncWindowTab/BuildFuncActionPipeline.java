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
package com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.buildFuncWindowTab;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.FuncActionPipeline;

import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_CONTENT_NAME;

public class BuildFuncActionPipeline extends FuncActionPipeline {
    public BuildFuncActionPipeline(Project project, Function function) {
        super("Build", project, function);
    }

    @Override
    protected String getTabName() {
        return BUILDFUNC_CONTENT_NAME;
    }
}
