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
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import com.redhat.devtools.intellij.common.utils.ConfigWatcher;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.kn.Service;
import com.redhat.devtools.intellij.knative.utils.WatchHandler;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Icon;
import org.apache.commons.codec.binary.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnTreeStructure extends AbstractKnTreeStructure implements ConfigWatcher.Listener {

    private static final Icon SERVICE_ICON = IconLoader.findIcon("/images/service.svg");
    private static final Icon REVISION_ICON = IconLoader.findIcon("/images/revision.svg");
    private static final Icon SOURCE_ICON = IconLoader.findIcon("/images/source.svg");
    private static final Icon CHANNEL_ICON = IconLoader.findIcon("/images/channel.svg");
    private static final Icon BROKER_ICON = IconLoader.findIcon("/images/broker.svg");
    private static final Icon SUBSCRIPTION_ICON = IconLoader.findIcon("/images/subscription.svg");
    private static final Icon TRIGGER_ICON = IconLoader.findIcon("/images/trigger.svg");

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private Config config;
    protected ClusterModelSynchronizer clusterModelSynchronizer;

    public KnTreeStructure(Project project) {
        super(project);
        this.config = loadConfig();
        this.clusterModelSynchronizer = new ClusterModelSynchronizer(this);
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
                if (hasKnativeServing(kn)) {
                    result = ArrayUtil.append(result, new KnServingNode(root, root));
                }
                if (hasKnativeEventing(kn)) {
                    result = ArrayUtil.append(result, new KnEventingNode(root, root));
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
        }

        return new Object[0];
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

    private Function<Boolean, Service> getService(Kn kn, Service service) {
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
            return new KnRootNodeDescriptor(project, (KnRootNode) element, kn != null ? kn.getNamespace() : "Loading", CLUSTER_ICON, parentDescriptor);
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

        if (element instanceof KnEventingBrokerNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingBrokerNode) element).getName(), BROKER_ICON, parentDescriptor);
        }

        if (element instanceof KnEventingChannelsNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingChannelsNode) element).getName(), CHANNEL_ICON, parentDescriptor);
        }
        if (element instanceof KnEventingSourcesNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingSourcesNode) element).getName(), SOURCE_ICON, parentDescriptor);
        }

        if (element instanceof KnEventingSubscriptionsNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingSubscriptionsNode) element).getName(), SUBSCRIPTION_ICON, parentDescriptor);
        }

        if (element instanceof KnEventingTriggersNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnEventingTriggersNode) element).getName(), TRIGGER_ICON, parentDescriptor);
        }

        if (element instanceof KnSourceNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnSourceNode) element).getName(), SOURCE_ICON, parentDescriptor);
        }

        if (element instanceof KnSinkNode) {
            return new KnSinkDescriptor(project, (KnSinkNode) element, parentDescriptor);
        }
        return super.createDescriptor(element, parentDescriptor);
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
            WatchHandler.get(null).removeAll();
            root.load().whenComplete((kn, err) -> {
                mutableModelSupport.fireModified(root);
            });
        } catch (Exception e) {
        }
    }
}
