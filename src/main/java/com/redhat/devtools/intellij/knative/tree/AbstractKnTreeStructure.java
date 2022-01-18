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
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSupport;
import com.redhat.devtools.intellij.knative.kn.Kn;
import java.io.IOException;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractKnTreeStructure extends AbstractTreeStructure implements MutableModel<Object> {
    private static Logger logger = LoggerFactory.getLogger(AbstractKnTreeStructure.class);
    protected static final Icon CLUSTER_ICON = IconLoader.findIcon("/images/knative-logo.svg", AbstractKnTreeStructure.class);
    protected Project project;
    protected KnRootNode root;
    protected final MutableModel<Object> mutableModelSupport = new MutableModelSupport<>();

    public AbstractKnTreeStructure(Project project) {
        this.project = project;
        this.root = KnRootNode.getInstance(project);
    }

    @Override
    public @NotNull Object getRootElement() {
        return root;
    }

    @Override
    public @NotNull Object[] getChildElements(@NotNull Object element) {
        return new Object[0];
    }

    @Override
    public @Nullable Object getParentElement(@NotNull Object element) {
        return null;
    }

    @Override
    public @NotNull NodeDescriptor createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
        if (element instanceof MessageNode) {
            return new LabelAndIconDescriptor<>(project, element, ((MessageNode<?>) element).getName(), AllIcons.Nodes.EmptyNode, parentDescriptor);
        }
        if (element instanceof ParentableNode) {
            logger.warn("There are no descriptor for " + element.getClass().getName() + ", using default.");
            return new LabelAndIconDescriptor<>(project, element, ((ParentableNode<?>) element).getName(), AllIcons.Nodes.ErrorIntroduction, parentDescriptor);
        }
        throw new RuntimeException("Can't find NodeDescriptor for " + element.getClass().getName());
    }

    @Override
    public void commit() {

    }

    @Override
    public boolean hasSomethingToCommit() {
        return false;
    }


    @Override
    public void fireAdded(Object element) {
        mutableModelSupport.fireAdded(element);
    }

    @Override
    public void fireModified(Object element) {
        mutableModelSupport.fireModified(element);
    }

    @Override
    public void fireRemoved(Object element) {
        mutableModelSupport.fireRemoved(element);
    }

    @Override
    public void addListener(Listener<Object> listener) {
        mutableModelSupport.addListener(listener);
    }

    @Override
    public void removeListener(Listener<Object> listener) {
        mutableModelSupport.removeListener(listener);
    }

    protected boolean hasKnativeServing(Kn kn) {
        try {
            return kn.isKnativeServingAware();
        } catch (IOException e) {
            return false;
        }
    }

    protected boolean hasKnativeEventing(Kn kn) {
        try {
            return kn.isKnativeEventingAware();
        } catch (IOException e) {
            return false;
        }
    }
}
