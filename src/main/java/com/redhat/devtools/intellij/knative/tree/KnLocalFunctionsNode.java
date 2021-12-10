package com.redhat.devtools.intellij.knative.tree;

import org.jetbrains.annotations.NotNull;

public class KnLocalFunctionsNode extends ParentableNode<KnRootNode> {
    protected KnLocalFunctionsNode(@NotNull KnRootNode rootNode, @NotNull KnRootNode parent) {
        super(rootNode, parent, "Local Functions");
    }
}
