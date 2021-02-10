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

import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSupport;
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import com.redhat.devtools.intellij.common.utils.ConfigWatcher;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import org.apache.commons.codec.binary.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnativeTreeStructure extends AbstractTreeStructure implements MutableModel<Object>, ConfigWatcher.Listener {

    private final Project project;
    private Config config;
    private KnativeRootNode root;

    private static final Icon CLUSTER_ICON = IconLoader.findIcon("/images/knative-logo.svg", KnativeTreeStructure.class);

    private MutableModel<Object> mutableModelSupport = new MutableModelSupport<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public KnativeTreeStructure(Project project) {
        this.project = project;
        this.root = new KnativeRootNode(project);
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
        return new Object[0];
    }

    @Override
    public @Nullable Object getParentElement(@NotNull Object element) {
        return null;
    }

    @Override
    public @NotNull NodeDescriptor createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
        if (element instanceof KnativeRootNode) {
            Kn kn = ((KnativeRootNode)element).getKn();
            return new LabelAndIconDescriptor(project, element, kn != null?kn.getMasterUrl().toString():"Loading", CLUSTER_ICON, parentDescriptor);
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
                || !StringUtils.equals(currentContext.getContext().getUser(), newContext.getContext().getUser());
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
            root.load();
            mutableModelSupport.fireModified(root);
        } catch (Exception e) {
        }
    }
}
