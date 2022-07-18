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
package com.redhat.devtools.intellij.knative.listener;

import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnFileListener implements AsyncFileListener {

    public final static Map<String, Date> lastFunctionEdit = new HashMap<>();

    public static void registerFunction(String funcPath) {
        if (!lastFunctionEdit.containsKey(funcPath)) {
            lastFunctionEdit.put(funcPath, new Date());
        }
    }

    public static boolean isFuncChangedSinceLastBuild(String funcPath, Date lastBuild) {
        Date lastEdit = lastFunctionEdit.getOrDefault(funcPath, null);
        return lastEdit != null && lastEdit.after(lastBuild);
    }

    @Override
    public @Nullable ChangeApplier prepareChange(@NotNull List<? extends VFileEvent> events) {
        events.forEach(event -> {
            if (event instanceof VFileContentChangeEvent) {
                if (isFuncYaml(event.getPath()) || isHiddenFileOrFolder(event.getPath())) {
                    return;
                }
                Date editDate = ((VFileContentChangeEvent) event).getModificationStamp() > 0 ? new Date() : null;
                lastFunctionEdit.keySet().forEach(funcPath -> {
                    if (event.getPath().contains(funcPath)) {
                        lastFunctionEdit.put(funcPath, editDate);
                    }
                });
            }
        });
        return null;
    }

    private boolean isFuncYaml(String path) {
        return new File(path).getName().equalsIgnoreCase("func.yaml");
    }

    private boolean isHiddenFileOrFolder(String path) {
        File file = new File(path);
        while(file != null) {
            if (file.isHidden()) {
                return true;
            }
            file = file.getParentFile();
        }
        return false;
    }
}
