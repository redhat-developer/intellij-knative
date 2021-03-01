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
package com.redhat.devtools.intellij.knative.tree;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.KnCliFactory;

import java.util.concurrent.CompletableFuture;

public class KnRootNode {
    private final Project project;
    private Kn kn;

    public KnRootNode(Project project) {
        this.project = project;
    }

    public CompletableFuture<Kn> initializeKn() {
        return KnCliFactory.getInstance().getKn(project).whenComplete((kn, err) -> this.kn = kn);
    }

    public Kn getKn() {
        return kn;
    }

    public CompletableFuture<Kn> load() {
        KnCliFactory.getInstance().resetKn();
        return initializeKn();
    }

    public Project getProject() {
        return project;
    }
}
