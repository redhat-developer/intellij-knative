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
package com.redhat.devtools.intellij.knative.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FuncUtils {

    public static boolean isTektonReady(Kn kn) {
        try {
            return isTektonAwareAsync(kn).get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }

    private static CompletableFuture<Boolean> isTektonAwareAsync(Kn kn) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        ExecHelper.submit(() -> {
            try {
                result.complete(kn.isTektonAware());
            } catch (IOException e) {
                result.complete(false);
            }
        });
        return result;
    }

    public static boolean isKnativeReady(Kn kn) {
        try {
            return isKnServingAndEventingAwareAsync(kn).get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return false;
        }
    }

    private static CompletableFuture<Boolean> isKnServingAndEventingAwareAsync(Kn kn) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        ExecHelper.submit(() -> {
            try {
                result.complete(kn.isKnativeServingAware() && kn.isKnativeEventingAware());
            } catch (IOException e) {
                result.complete(false);
            }
        });
        return result;
    }

    public static JsonNode getFuncSection(Kn kn, String path, String[] section) throws IOException {
        URL funcFileURL = kn.getFuncFileURL(Paths.get(path));
        String content = YAMLHelper.JSONToYAML(YAMLHelper.URLToJSON(funcFileURL));
        return YAMLHelper.getValueFromYAML(content, section);
    }
}
