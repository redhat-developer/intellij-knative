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
