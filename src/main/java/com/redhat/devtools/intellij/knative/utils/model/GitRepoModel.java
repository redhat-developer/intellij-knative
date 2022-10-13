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
package com.redhat.devtools.intellij.knative.utils.model;

import org.jetbrains.annotations.NotNull;

public class GitRepoModel {

    private final String repository;
    private final String branch;

    public GitRepoModel(@NotNull String repository) {
        this(repository, "");
    }

    public GitRepoModel(@NotNull String repository, String branch) {
        this.repository = repository;
        this.branch = branch;
    }

    public String getRepository() {
        return repository;
    }

    public String getBranch() {
        return branch;
    }
}
