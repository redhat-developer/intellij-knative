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

public class KnEventingSubscriptionsNode extends ParentableNode<KnEventingNode> {
    protected KnEventingSubscriptionsNode(KnRootNode rootNode, KnEventingNode parent) {
        super(rootNode, parent, "Subscriptions");
    }
}
