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
package com.redhat.devtools.intellij.knative.git;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.knative.kn.Function;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;

public class GitHandler {

    public static GitRepository getFunctionRepo(Project project, Function function) {
        for (GitRepository gitRepository: GitUtil.getRepositoryManager(project).getRepositories()) {
            if (gitRepository.getRoot().getPath().equalsIgnoreCase(function.getLocalPath())) {
                return gitRepository;
            }
        }
        return null;
    }
}
