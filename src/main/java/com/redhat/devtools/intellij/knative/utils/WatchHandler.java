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
    private Kn kn;
    private static WatchHandler instance;

    private WatchHandler(Kn kn) {
        watches = new HashMap<>();
        this.kn = kn;
    }

    public static WatchHandler get(Kn kn) {
        if (instance == null) {
            instance = new WatchHandler(kn);
        }
        return instance;
    }

    public void watchResource(String id, String kindToWatch, Runnable doExecute) {
        if (!watches.containsKey(id)) {
            Watch watch = watchResource(kindToWatch, doExecute);
            if (watch != null) {
                watches.put(id, watch);
            }
        }
    }

    private Watch watchResource(String kindToWatch, Runnable doExecute) {
        if (kindToWatch.equalsIgnoreCase(KIND_FUNCTION)) {
            return new FunctionWatcher(kn).doWatch(doExecute);
        }
        return null;
    }

    public void watchResource(String id, String kindToWatch, BiConsumer<Watcher.Action, Service> doExecute) {
        if (!watches.containsKey(id)) {
            Watch watch = watchResource(kindToWatch, doExecute);
            if (watch != null) {
                watches.put(id, watch);
            }
        }
    }

    private Watch watchResource(String kindToWatch, BiConsumer<Watcher.Action, Service> doExecute) {
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
    }
}
