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
        Kn kn = getKn();
        if (future == null
                || (kn != null && !kn.getProject().equals(project))) {
            CompletableFuture<String> knCompletableFuture = DownloadHelper.getInstance()
                    .downloadIfRequiredAsync("kn", KnCliFactory.class.getResource("/kn.json"));
            CompletableFuture<String> funcCompletableFuture = DownloadHelper.getInstance()
                    .downloadIfRequiredAsync("func", KnCliFactory.class.getResource("/func.json"));
            future = knCompletableFuture.thenCompose(knCommand ->
                    funcCompletableFuture.thenApply(funcCommand -> new KnCli(project, knCommand, funcCommand)));
        }
        return future;
    }

    private Kn getKn() {
        try {
            if (future != null) {
                return future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public void resetKn() {
        future = null;
    }
}
