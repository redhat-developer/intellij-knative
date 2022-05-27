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
package com.redhat.devtools.intellij.knative.ui.brdWindowTabs;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.knative.kn.Function;

import javax.swing.Icon;

public interface IFuncAction {
    long getStartTime();

    long getEndTime();

    Project getProject();

    String getFuncName();

    Icon getStateIcon();

    String getState();

    boolean isFinished();

    boolean isSuccessfullyCompleted();

    Function getFunction();

    String getStartingDate();


}
