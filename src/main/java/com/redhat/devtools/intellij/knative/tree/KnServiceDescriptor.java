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

import com.google.common.base.Strings;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import com.redhat.devtools.intellij.knative.kn.StatusCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class KnServiceDescriptor extends PresentableNodeDescriptor<KnServiceNode> {

    private final KnServiceNode element;
    private final Icon nodeIcon;

    protected KnServiceDescriptor(Project project, KnServiceNode element, Icon nodeIcon, @Nullable NodeDescriptor parentDescriptor) {
        super(project, parentDescriptor);
        this.element = element;
        this.nodeIcon = nodeIcon;
        this.myName = element.getName();
    }

    @Override
    protected void update(@NotNull PresentationData presentation) {
        if (nodeIcon != null) {
            presentation.setIcon(nodeIcon);
        }
        String errorMessage = "";
        if (element.getService().getStatus() != null) {
            for (StatusCondition condition : element.getService().getStatus().getConditions()) {
                if ("False".equals(condition.getStatus()) && "Ready".equals(condition.getType())) {
                    errorMessage = condition.getMessage();
                    break;
                }
            }
        }

        if (Strings.isNullOrEmpty(errorMessage)) {
            presentation.addText(element.getName() + " ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        } else {
            presentation.setTooltip(errorMessage);
            presentation.addText(element.getName() + " ", SimpleTextAttributes.ERROR_ATTRIBUTES);
        }
    }

    @Override
    public KnServiceNode getElement() {
        return element;
    }
}
