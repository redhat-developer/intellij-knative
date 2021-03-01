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
import com.intellij.util.ArrayUtil;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSupport;
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import com.redhat.devtools.intellij.common.utils.ConfigWatcher;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.Revision;
import com.redhat.devtools.intellij.knative.kn.Service;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import org.apache.commons.codec.binary.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class KnTreeStructure extends AbstractTreeStructure implements MutableModel<Object>, ConfigWatcher.Listener {

    private static final Icon CLUSTER_ICON = IconLoader.findIcon("/images/knative-logo.svg", KnTreeStructure.class);
    private static final Icon SERVICE_ICON = IconLoader.findIcon("/images/service.svg");
    private static final Icon REVISION_ICON = IconLoader.findIcon("/images/revision.svg");

    private final Project project;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private Config config;
    private final KnRootNode root;
    private final MutableModel<Object> mutableModelSupport = new MutableModelSupport<>();

    public KnTreeStructure(Project project) {
        this.project = project;
        this.root = new KnRootNode(project);
        this.config = loadConfig();
        initConfigWatcher();
    }

    private void initConfigWatcher() {
        ExecHelper.submit(new ConfigWatcher(Paths.get(ConfigHelper.getKubeConfigPath()), this));
    }

    protected Config loadConfig() {
        return ConfigHelper.safeLoadKubeConfig();
    }

    @Override
    public @NotNull Object getRootElement() {
        if (!initialized.getAndSet(true)) {
            root.initializeKn().thenAccept(kn -> fireModified(root));
        }
        return root;
    }

    @Override
    public @NotNull Object[] getChildElements(@NotNull Object element) {
        Kn kn = root.getKn();
        if (kn != null) {
            if (element instanceof KnRootNode) {
                Object[] result = new Object[2];
                try {
                    if (kn.isKnativeServingAware()) {
                        result = ArrayUtil.append(result, new KnServingNode(root, root));
                    }
                } catch (IOException e) {
                    // ignore
                }
                try {
                    if (kn.isKnativeEventingAware()) {
                        result = ArrayUtil.append(result, new KnEventingNode(root, root));
                    }
                } catch (IOException e) {
                    // ignore
                }

                return result;
            }

            if (element instanceof KnServingNode) {
                return getServiceNodes((KnServingNode) element);
            }

            if (element instanceof KnServiceNode) {
                return getRevisionNodes((KnServiceNode) element);
            }
        }

        return new Object[0];
    }

    private Object[] getRevisionNodes(KnServiceNode element) {
        Kn kn = element.getRootNode().getKn();
        try {
            List<Revision> revisions = kn.getRevisionsForService(element.getName());
            return revisions.stream().map(it -> new KnRevisionNode(element.getRootNode(), element, it)).toArray(KnRevisionNode[]::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Object[0];
    }

    private Object[] getServiceNodes(KnServingNode element) {
        Kn kn = element.getRootNode().getKn();
        try {
            List<Service> servicesList = kn.getServicesList();
            return servicesList.stream().map(it -> new KnServiceNode(element.getRootNode(), element, it)).toArray(KnServiceNode[]::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Object[0];
    }

    @Override
    public @Nullable Object getParentElement(@NotNull Object element) {
        if (element instanceof ParentableNode) {
            return ((ParentableNode<?>) element).getParent();
        }
        return null;
    }

    @Override
    public @NotNull NodeDescriptor<?> createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
        if (element instanceof KnRootNode) {
            Kn kn = ((KnRootNode) element).getKn();
            return new LabelAndIconDescriptor<>(project, element, kn != null ? kn.getNamespace() : "Loading", CLUSTER_ICON, parentDescriptor);
        }

        if (element instanceof KnServingNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnServingNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }
        if (element instanceof KnEventingNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }

        if (element instanceof KnServiceNode) {
            return new KnServiceDescriptor(project, (KnServiceNode) element, SERVICE_ICON, parentDescriptor);
        }

        if (element instanceof KnRevisionNode) {
            return new KnRevisionDescriptor(project, (KnRevisionNode) element, REVISION_ICON, parentDescriptor);
        }
        return null;
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

    @Override
    public void onUpdate(ConfigWatcher source, Config config) {
        if (hasContextChanged(config, this.config)) {
            refresh();
        }
        this.config = config;
    }

    private boolean hasContextChanged(Config newConfig, Config currentConfig) {
        NamedContext currentContext = KubeConfigUtils.getCurrentContext(currentConfig);
        NamedContext newContext = KubeConfigUtils.getCurrentContext(newConfig);
        return hasServerChanged(newContext, currentContext)
                || hasNewToken(newContext, newConfig, currentContext, currentConfig);
    }

    private boolean hasServerChanged(NamedContext newContext, NamedContext currentContext) {
        return newContext == null
                || currentContext == null
                || !StringUtils.equals(currentContext.getContext().getCluster(), newContext.getContext().getCluster())
                || !StringUtils.equals(currentContext.getContext().getUser(), newContext.getContext().getUser())
                || !StringUtils.equals(currentContext.getContext().getNamespace(), newContext.getContext().getNamespace());
    }

    private boolean hasNewToken(NamedContext newContext, Config newConfig, NamedContext currentContext, Config currentConfig) {
        if (newContext == null) {
            return false;
        }
        if (currentContext == null) {
            return true;
        }
        String newToken = KubeConfigUtils.getUserToken(newConfig, newContext.getContext());
        if (newToken == null) {
            // logout, do not refresh, LogoutAction already refreshes
            return false;
        }
        String currentToken = KubeConfigUtils.getUserToken(currentConfig, currentContext.getContext());
        return !StringUtils.equals(newToken, currentToken);
    }

    protected void refresh() {
        try {
            root.load().whenComplete((kn, err) -> {
                mutableModelSupport.fireModified(root);
            });
        } catch (Exception e) {
        }
    }
}
