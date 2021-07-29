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

import com.google.common.base.Strings;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSupport;
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import com.redhat.devtools.intellij.common.utils.ConfigWatcher;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.Service;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import org.apache.commons.codec.binary.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Icon;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class KnTreeStructure extends AbstractKnTreeStructure implements ConfigWatcher.Listener {
    private static Logger logger = LoggerFactory.getLogger(KnTreeStructure.class);

    private static final Icon SERVICE_ICON = IconLoader.findIcon("/images/service.svg");
    private static final Icon REVISION_ICON = IconLoader.findIcon("/images/revision.svg");
    private static final Icon SOURCE_ICON = IconLoader.findIcon("/images/source-generic.svg");

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private Config config;

    public KnTreeStructure(Project project) {
        super(project);
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
                Object[] result = new Object[0];
                boolean hasKnativeServing = hasKnativeServing(kn);
                boolean hasKnativeEventing = hasKnativeEventing(kn);
                if (hasKnativeServing) {
                    result = ArrayUtil.append(result, new KnServingNode(root, root));
                }
                if (hasKnativeEventing) {
                    result = ArrayUtil.append(result, new KnEventingNode(root, root));
                }
                if (hasKnativeEventing && hasKnativeServing) {
                    result = ArrayUtil.append(result, new KnFunctionsNode(root, root));
                }
                return result;
            }

            if (element instanceof KnServingNode) {
                return getServiceNodes((KnServingNode) element);
            }

            if (element instanceof KnServiceNode) {
                return getRevisionNodes((KnServiceNode) element);
            }

            if (element instanceof KnEventingNode) {
                return getEventingNodes((KnEventingNode) element);
            }

            if (element instanceof KnEventingSourcesNode) {
                return getEventingSources((KnEventingSourcesNode) element);
            }

            if (element instanceof KnSourceNode) {
                return getSinkForSource((KnSourceNode) element);
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

    private Object[] getSinkForSource(KnSourceNode element) {
        if (element.getSource().getSinkSource() != null) {
            return new Object[]{new KnSinkNode(root, element, element.getSource().getSinkSource())};
        }
        return new Object[0];
    }

    private Object[] getEventingNodes(KnEventingNode parent) {
        return new Object[]{
                new KnEventingBrokerNode(root, parent),
                new KnEventingChannelsNode(root, parent),
                new KnEventingSourcesNode(root, parent),
                new KnEventingSubscriptionsNode(root, parent),
                new KnEventingTriggersNode(root, parent)
        };
    }

    private Object[] getEventingSources(KnEventingSourcesNode element) {
        List<Object> sources = new ArrayList<>();
        try {
            Kn kn = element.getRootNode().getKn();
            kn.getSources().forEach(it -> sources.add(new KnSourceNode(element.getRootNode(), element, it)));
        } catch (IOException e) {
            sources.add(new MessageNode<>(element.getRootNode(), element, "Failed to load sources"));
        }
        return sources.toArray();
    }

    private Object[] getRevisionNodes(KnServiceNode element) {
        List<Object> revisions = new ArrayList<>();
        try {
            Kn kn = element.getRootNode().getKn();
            kn.getRevisionsForService(element.getName()).forEach(it -> revisions.add(new KnRevisionNode(element.getRootNode(), element, it)));
        } catch (IOException e) {
            revisions.add(new MessageNode<>(element.getRootNode(), element, "Failed to load revisions"));
        }
        return revisions.toArray();
    }

    private Object[] getServiceNodes(KnServingNode element) {
        List<Object> services = new ArrayList<>();
        try {
            Kn kn = element.getRootNode().getKn();
            kn.getServicesList().stream().forEach(it -> services.add(new KnServiceNode(element.getRootNode(), element, getService(kn, it))));
        } catch (IOException e) {
            services.add(new MessageNode<>(element.getRootNode(), element, "Failed to load services"));
        }
        return services.toArray();
    }

    private com.intellij.util.Function<Boolean, Service> getService(Kn kn, Service service) {
        AtomicReference<Service> serviceObj = new AtomicReference<>(service);
        return (toUpdate) -> {
            if (!toUpdate) {
                return serviceObj.get();
            }
            try {
                serviceObj.set(kn.getService(service.getName()));
                return serviceObj.get();
            } catch (IOException e) {
                return null;
            }
        };
    }

    @Override
    public @Nullable Object getParentElement(@NotNull Object element) {
        if (element instanceof ParentableNode) {
            return ((ParentableNode<?>) element).getParent();
        }
        return null;
    }

    @Override
    public NodeDescriptor<?> createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
        if (element instanceof KnRootNode) {
            Kn kn = ((KnRootNode) element).getKn();
            return new LabelAndIconDescriptor<>(project, element, kn != null ? kn.getNamespace() : "Loading", CLUSTER_ICON, parentDescriptor);
        }

        if (element instanceof MessageNode) {
            return new LabelAndIconDescriptor<>(project, element, ((MessageNode<?>) element).getName(), AllIcons.Nodes.EmptyNode, parentDescriptor);
        }

        if (element instanceof KnServingNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnServingNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }
        if (element instanceof KnEventingNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }
        if (element instanceof KnFunctionsNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnFunctionsNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }

        if (element instanceof KnServiceNode) {
            return new KnServiceDescriptor(project, (KnServiceNode) element, SERVICE_ICON, parentDescriptor);
        }

        if (element instanceof KnRevisionNode) {
            return new KnRevisionDescriptor(project, (KnRevisionNode) element, REVISION_ICON, parentDescriptor);
        }

        if (element instanceof KnEventingBrokerNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingBrokerNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }

        if (element instanceof KnEventingChannelsNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingChannelsNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }
        if (element instanceof KnEventingSourcesNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingSourcesNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }

        if (element instanceof KnEventingSubscriptionsNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingSubscriptionsNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }

        if (element instanceof KnEventingTriggersNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingTriggersNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }

        if (element instanceof KnSourceNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnSourceNode) element).getName(), SOURCE_ICON, parentDescriptor);
        }

        if (element instanceof KnSinkNode) {
            return new KnSinkDescriptor(project, (KnSinkNode) element, parentDescriptor);
        }

        if (element instanceof KnFunctionNode) {
            return new KnFunctionDescriptor(project, (KnFunctionNode) element, parentDescriptor);
        }

        //if we can present node we try to do that
        if (element instanceof ParentableNode) {
            logger.warn("There are no descriptor for " + element.getClass().getName() + ", using default.");
            return new LabelAndIconDescriptor<>(project, element, ((ParentableNode<?>) element).getName(), AllIcons.Nodes.ErrorIntroduction, parentDescriptor);
        }
        throw new RuntimeException("Can't find NodeDescriptor for " + element.getClass().getName());
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
