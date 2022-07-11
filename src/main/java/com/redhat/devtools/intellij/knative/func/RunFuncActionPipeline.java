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
package com.redhat.devtools.intellij.knative.func;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.knative.kn.Function;

import static com.redhat.devtools.intellij.knative.Constants.RUNFUNC_CONTENT_NAME;

public class RunFuncActionPipeline extends FuncActionPipeline {
    public RunFuncActionPipeline(Project project, Function function) {
        super("Run", project, function);
    }

    @Override
    protected String getTabName() {
        return RUNFUNC_CONTENT_NAME;
    }
}
