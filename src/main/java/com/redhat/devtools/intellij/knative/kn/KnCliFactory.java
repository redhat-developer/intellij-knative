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

public class KnCliFactory {
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
        if (future == null) {
            future = DownloadHelper.getInstance().downloadIfRequiredAsync("kn", KnCliFactory.class.getResource("/kn.json")).thenApply(command -> new KnCli(project, command));
        }
        return future;
    }

    public void resetKn() {
        future = null;
    }
}
