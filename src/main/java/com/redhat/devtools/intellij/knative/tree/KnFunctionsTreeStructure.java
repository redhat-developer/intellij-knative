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

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtil;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.knative.kn.Kn;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import static com.redhat.devtools.intellij.knative.Constants.KIND_FUNCTION;

public class KnFunctionsTreeStructure extends KnTreeStructure {
    public KnFunctionsTreeStructure(Project project) {
        super(project);
    }

    @Override
    public @NotNull Object[] getChildElements(@NotNull Object element) {
        Kn kn = root.getKn();
        if (kn != null) {
            if (element instanceof KnRootNode) {
                Object[] result = new Object[0];
                boolean hasKnativeServing = hasKnativeServing(kn);
                boolean hasKnativeEventing = hasKnativeEventing(kn);
                if (hasKnativeEventing && hasKnativeServing) {
                    KnFunctionsNode functionsNode = new KnFunctionsNode(root, root);
                    clusterModelSynchronizer.updateElementOnChange(functionsNode, KIND_FUNCTION);
                    result = ArrayUtil.append(result, functionsNode);
                }
                return result;
            }
            if (element instanceof KnFunctionsNode) {
                return getFunctionNodes((KnFunctionsNode) element);
            }
        }

        return new Object[0];
    }

    private Object[] getFunctionNodes(KnFunctionsNode parent) {
        List<Object> functions = new ArrayList<>();
        try {
            Kn kn = parent.getRootNode().getKn();
            kn.getFunctions().forEach(it -> functions.add(new KnFunctionNode(parent.getRootNode(), parent, it)));
        } catch (IOException e) {
            functions.add(new MessageNode<>(parent.getRootNode(), parent, "Failed to load revisions"));
        }
        return functions.toArray();
    }

    @Override
    public NodeDescriptor<?> createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
        if (element instanceof KnFunctionsNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnFunctionsNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }
        if (element instanceof KnFunctionNode) {
            return new KnFunctionDescriptor(project, (KnFunctionNode) element, parentDescriptor);
        }
        return super.createDescriptor(element, parentDescriptor);
    }
}
