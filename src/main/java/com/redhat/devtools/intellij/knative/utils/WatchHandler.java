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
package com.redhat.devtools.intellij.knative.utils;

import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import com.redhat.devtools.intellij.knative.watch.AbstractWatcher;
import com.redhat.devtools.intellij.knative.watch.FunctionWatcher;
import io.fabric8.knative.serving.v1.Service;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;


import static com.redhat.devtools.intellij.knative.Constants.KIND_FUNCTION;

public class WatchHandler {
    private Map<String, Watch> watches;
    private Map<String, Integer> watchRetry;
    private Kn kn;
    private static WatchHandler instance;

    private WatchHandler(Kn kn) {
        watches = new HashMap<>();
        watchRetry = new HashMap<>();
        this.kn = kn;
    }

    public static WatchHandler get(Kn kn) {
        if (instance == null) {
            instance = new WatchHandler(kn);
        }
        return instance;
    }

    public void watchResource(String id, String kindToWatch, Runnable doExecute) throws IOException {
        if (!watches.containsKey(id)
            && (!watchRetry.containsKey(id) || watchRetry.get(id) < 3)) {
            try {
                Watch watch = watchResource(kindToWatch, doExecute);
                if (watch != null) {
                    watches.put(id, watch);
                }
            } catch (IOException e) {
                int retry = watchRetry.getOrDefault(id, 0);
                watchRetry.put(id, ++retry);
                if (retry > 2) {
                    throw new IOException("Unable to watch " + kindToWatch + " resources on cluster");
                }
            }
        }
    }

    private Watch watchResource(String kindToWatch, Runnable doExecute) throws IOException {
        if (kindToWatch.equalsIgnoreCase(KIND_FUNCTION)) {
            return new FunctionWatcher(kn).doWatch(doExecute);
        }
        return null;
    }

    public void watchResource(String id, String kindToWatch, BiConsumer<Watcher.Action, Service> doExecute) {
        if (isRetryable(id)) {
            try {
                Watch watch = watchResource(kindToWatch, doExecute);
                if (watch != null) {
                    watches.put(id, watch);
                }
            } catch (IOException e) {
                int retry = watchRetry.getOrDefault(id, 0);

                watchRetry.put(id, ++retry);
            }
        }
    }

    private boolean isRetryable(String id) {
        return !watches.containsKey(id)
                && (!watchRetry.containsKey(id) || watchRetry.get(id) < 3);
    }

    private Watch watchResource(String kindToWatch, BiConsumer<Watcher.Action, Service> doExecute) throws IOException {
        if (kindToWatch.equalsIgnoreCase(KIND_FUNCTION)) {
            return new FunctionWatcher(kn).doWatch(doExecute);
        }
        return null;
    }

    public void remove(String id) {
        if (watches.containsKey(id)) {
            Watch watch = watches.remove(id);
            watch.close();
        }
    }

    public void removeAll() {
        this.watches.values().forEach(Watch::close);
        this.watches.clear();
        this.watchRetry.clear();
    }
}
