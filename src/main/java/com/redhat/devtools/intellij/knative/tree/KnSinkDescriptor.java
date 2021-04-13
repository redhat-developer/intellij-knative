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
import com.redhat.devtools.intellij.knative.kn.BaseSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class KnSinkDescriptor extends PresentableNodeDescriptor<KnSinkNode> {
    private static final Icon BROKER_ICON = IconLoader.findIcon("/images/broker.svg");
    private static final Icon CHANNEL_ICON = IconLoader.findIcon("/images/channel.svg");
    private static final Icon SERVICE_ICON = IconLoader.findIcon("/images/service.svg");
    private static final Icon LINK_ICON = IconLoader.findIcon("/images/link.svg");

    private final KnSinkNode node;

    protected KnSinkDescriptor(Project project, KnSinkNode node, @Nullable NodeDescriptor parentDescriptor) {
        super(project, parentDescriptor);
        this.node = node;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        presentation.setPresentableText(node.getName());
        presentation.setIcon(getIcon(node.getSource()));
    }

    private Icon getIcon(BaseSource source) {
//        if(source instanceof APIServerSource) {
//            return SERVICE_ICON;
//        }
//        if(source instanceof PingSource) {
//
//        }
//        if(source instanceof BindingSource) {
//            return
//        }

        return SERVICE_ICON;
    }

    @Override
    public KnSinkNode getElement() {
        return node;
    }
}
