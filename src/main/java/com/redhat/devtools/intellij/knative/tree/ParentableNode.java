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

public abstract class ParentableNode<T> {
    private final KnRootNode rootNode;
    private final T parent;
    private final String name;

    protected ParentableNode(KnRootNode rootNode, T parent, String name) {
        this.rootNode = rootNode;
        this.parent = parent;
        this.name = name;
    }

    public KnRootNode getRootNode() {
        return rootNode;
    }

    public T getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }
}
