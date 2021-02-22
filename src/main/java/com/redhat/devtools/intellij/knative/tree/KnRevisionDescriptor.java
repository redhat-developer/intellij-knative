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
import com.intellij.ui.SimpleTextAttributes;
import com.redhat.devtools.intellij.knative.kn.ServiceTraffic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class KnRevisionDescriptor extends PresentableNodeDescriptor<KnRevisionNode> {
    private final Icon nodeIcon;
    private KnRevisionNode element;

    public KnRevisionDescriptor(Project project, KnRevisionNode element, Icon nodeIcon, @Nullable NodeDescriptor parentDescriptor) {
        super(project, parentDescriptor);
        this.element = element;
        this.nodeIcon = nodeIcon;
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        if (nodeIcon != null) {
            presentation.setIcon(nodeIcon);
        }
        presentation.setTooltip("Revision: " + element.getName());
        presentation.addText(element.getName() + " ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        if (element.getParent().getService().getStatus().getTraffic() != null) {
            StringBuilder tag = new StringBuilder();
            int percent = 0;
            for (ServiceTraffic st : element.getParent().getService().getStatus().getTraffic()) {
                if (st.getRevisionName().equals(element.getName())) {
                    percent += Math.max(st.getPercent(), 0);
                    tag.append(st.getLatestRevision() != null ? "latest" : "");
                    tag.append(st.getTag() != null ? st.getTag() : "");
                }
            }
            presentation.addText(percent + "%", SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES);
            presentation.addText(" " + tag.toString(), SimpleTextAttributes.GRAY_ATTRIBUTES);


        }
    }

    @Override
    public KnRevisionNode getElement() {
        return element;
    }
}
