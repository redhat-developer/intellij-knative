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
package com.redhat.devtools.intellij.knative.kn;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.DownloadHelper;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnCliFactory {
    private static final Logger logger = LoggerFactory.getLogger(KnCliFactory.class);
    private static KnCliFactory INSTANCE;
    private Project lastProject;

    public static KnCliFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KnCliFactory();
        }
        return INSTANCE;
    }

    private CompletableFuture<Kn> future;

    private KnCliFactory() {
    }

    public CompletableFuture<Kn> getKn(Project project) {
        if (future == null) {
            download(project);
        } else {
            future.whenComplete((kn, throwable) -> {
                if (!lastProject.equals(project)) {
                    download(project);
                }
            });
        }
        return future;
    }

    private void download(Project project) {
        lastProject = project;
        CompletableFuture<String> knCompletableFuture = DownloadHelper.getInstance()
                .downloadIfRequiredAsync("kn", KnCliFactory.class.getResource("/kn.json"));
        CompletableFuture<String> funcCompletableFuture = DownloadHelper.getInstance()
                .downloadIfRequiredAsync("func", KnCliFactory.class.getResource("/func.json"));
        future = knCompletableFuture.thenCompose(knCommand ->
                funcCompletableFuture.thenApply(funcCommand -> new KnCli(project, knCommand, funcCommand)));
    }

    public void resetKn() {
        future = null;
    }
}
