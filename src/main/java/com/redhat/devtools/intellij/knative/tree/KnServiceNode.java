/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.tree;

import com.redhat.devtools.intellij.knative.kn.Service;

public class KnServiceNode extends ParentableNode<KnServingNode> {
    private final Service service;

    public KnServiceNode(KnRootNode rootNode, KnServingNode parent, Service service) {
        super(rootNode, parent, service.getName());
        this.service = service;
    }

    public Service getService() {
        return service;
    }
}
