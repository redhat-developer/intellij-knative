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
package com.redhat.devtools.intellij.knative.tree;

import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import com.redhat.devtools.intellij.knative.utils.WatchHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClusterModelSynchronizer {

    private KnTreeStructure treeStructure;
    private volatile Map<String, Object> resourceToNodeMapping = new HashMap<>();

    public ClusterModelSynchronizer(KnTreeStructure treeStructure) {
        this.treeStructure = treeStructure;
    }

    public void updateElementOnChange(KnRootNode element, String kindToWatch) throws IOException {
        String id = TreeHelper.getId(element);
        resourceToNodeMapping.put(id, element);
        WatchHandler.get(element.getKn()).watchResource(id, kindToWatch, getUpdateRunnable(id));
    }

    private Runnable getUpdateRunnable(String id) {
        return () -> treeStructure.fireModified(resourceToNodeMapping.get(id));
    }

}
