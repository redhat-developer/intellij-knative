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

import com.redhat.devtools.intellij.knative.kn.Function;
import org.jetbrains.annotations.NotNull;

public class KnLocalFunctionNode extends ParentableNode<KnLocalFunctionsNode> implements IKnFunctionNode {
    private Function function;
    public KnLocalFunctionNode(@NotNull KnRootNode rootNode, @NotNull KnLocalFunctionsNode parent, Function function) {
        super(rootNode, parent, function.getName());
        this.function = function;
    }

    public Function getFunction() {
        return this.function;
    }

}
