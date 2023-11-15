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

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class KnSinkDescriptor extends PresentableNodeDescriptor<KnSinkNode> {
    private static final Icon SERVICE_ICON = IconLoader.findIcon("/images/service.svg", KnSinkDescriptor.class);

    private final KnSinkNode node;

    protected KnSinkDescriptor(Project project, KnSinkNode node, @Nullable NodeDescriptor parentDescriptor) {
        super(project, parentDescriptor);
        this.node = node;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        if (node.getName() == null) {
            presentation.clear();
            presentation.addText("Sink Not Found", SimpleTextAttributes.ERROR_ATTRIBUTES);
        } else {
            presentation.setPresentableText(node.getName());
            presentation.setIcon(SERVICE_ICON);
        }

    }

    @Override
    public KnSinkNode getElement() {
        return node;
    }
}
