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
package com.redhat.devtools.intellij.knative.model;

import com.intellij.openapi.project.Project;
import java.util.Collections;
import java.util.List;

public class CreateDialogModel {

    private Project project;
    private String title;
    private String namespace;
    private Runnable refreshFunction;
    private List<String> services, serviceAccounts;

    public CreateDialogModel(Project project, String title, String namespace, Runnable refreshFunction) {
        this(project, title, namespace, refreshFunction, Collections.emptyList(), Collections.emptyList());
    }

    public CreateDialogModel(Project project, String title, String namespace, Runnable refreshFunction, List<String> services, List<String> serviceAccounts) {
        this.project = project;
        this.title = title;
        this.namespace = namespace;
        this.refreshFunction = refreshFunction;
        this.serviceAccounts = serviceAccounts;
        this.services = services;
    }

    public Project getProject() {
        return this.project;
    }

    public String getTitle() {
        return title;
    }

    public String getNamespace() {
        return namespace;
    }

    public Runnable getRefreshFunction() {
        return refreshFunction;
    }

    public List<String> getServices() {
        return services;
    }

    public List<String> getServiceAccounts() {
        return serviceAccounts;
    }
}
