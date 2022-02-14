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
package com.redhat.devtools.intellij.knative.watch;


import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.utils.Scheduler;
import io.fabric8.knative.serving.v1.Service;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import java.io.IOException;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.knative.Constants.FUNCTON_LABEL_KEY;

public class FunctionWatcher extends AbstractWatcher {
    private static final Logger logger = LoggerFactory.getLogger(FunctionWatcher.class);

    public FunctionWatcher(Kn kn) {
        super(kn);
    }

    public Watch doWatch(Runnable doExecute) {
        try {
            return kn.watchServiceWithLabel(FUNCTON_LABEL_KEY, "true", getWatcher(doExecute));
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return null;
    }

    private Watcher<io.fabric8.knative.serving.v1.Service> getWatcher(Runnable doExecute) {
        Scheduler scheduler = new Scheduler(2000);
        return new Watcher<io.fabric8.knative.serving.v1.Service>() {
            @Override
            public void eventReceived(Action action, Service resource) {
                scheduler.schedule(doExecute);
            }

            @Override
            public void onClose(WatcherException cause) {
            }
        };
    }

    public Watch doWatch(BiConsumer<Watcher.Action, Service> doExecute) {
        try {
            return kn.watchServiceWithLabel(FUNCTON_LABEL_KEY, "true", getWatcher(doExecute));
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return null;
    }

    private Watcher<io.fabric8.knative.serving.v1.Service> getWatcher(BiConsumer<Watcher.Action, Service> doExecute) {
        return new Watcher<io.fabric8.knative.serving.v1.Service>() {
            @Override
            public void eventReceived(Action action, Service resource) {
                doExecute.accept(action, resource);
            }

            @Override
            public void onClose(WatcherException cause) {
            }
        };
    }
}
