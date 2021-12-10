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
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.redhat.devtools.intellij.knative.utils.Scheduler;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.knative.Constants.KIND_FUNCTION;

public class KnFunctionsTreeStructure extends KnTreeStructure {

    private Logger logger = LoggerFactory.getLogger(KnFunctionsTreeStructure.class);
    private List<Pair<String, VirtualFileListener>> funcYamlListeners;

    public KnFunctionsTreeStructure(Project project) {
        super(project);
        funcYamlListeners = new ArrayList<>();
    }

    @Override
    public @NotNull Object[] getChildElements(@NotNull Object element) {
        Kn kn = root.getKn();
        if (kn != null) {
            if (element instanceof KnRootNode) {
                KnFunctionsNode functionsNode = new KnFunctionsNode(root, root);
                KnLocalFunctionsNode localFunctionsNode = new KnLocalFunctionsNode(root, root);
                clusterModelSynchronizer.updateElementOnChange(functionsNode, KIND_FUNCTION);
                return new Object[] {
                        functionsNode,
                        localFunctionsNode
                };
            }
            if (element instanceof KnFunctionsNode) {
                boolean hasKnativeServing = hasKnativeServing(kn);
                boolean hasKnativeEventing = hasKnativeEventing(kn);
                if (hasKnativeEventing && hasKnativeServing) {
                    return getFunctionNodes((KnFunctionsNode) element);
                } else {
                    return new Object[] {
                            new MessageNode(root, root,
                                    "Unable to load functions. Functions need both knative serving and eventing installed to work.")
                    };
                }
            }
            if (element instanceof KnLocalFunctionsNode) {
                return getLocalFunctionNodes((KnLocalFunctionsNode) element);
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

    private Object[] getLocalFunctionNodes(KnLocalFunctionsNode parent) {
        List<Object> functions = new ArrayList<>();
        Kn kn = parent.getRootNode().getKn();
        List<Function> funcOnCluster = Collections.emptyList();
        try {
            funcOnCluster = kn.getFunctions();
        } catch (IOException ignored) { }

        try {
            // if current project contains new functions, adds them
            List<String> pathsWithFunc = getModulesPathsWithFunc(kn);
            if (!pathsWithFunc.isEmpty()) {
                List<Function> localFunctions = getLocalFunctionsFromOpenedProject(pathsWithFunc, funcOnCluster, kn);
                setListenerToFunctions(kn, parent, localFunctions);
                localFunctions.forEach(it -> functions.add(new KnLocalFunctionNode(parent.getRootNode(), parent, it)));
            }
        }catch (IOException e) {
            functions.add(new MessageNode<>(parent.getRootNode(), parent, "Unable to load local functions. Error while parsing opened project."));
            logger.warn(e.getLocalizedMessage(), e);
        }
        return functions.toArray();
    }

    private void setListenerToFunctions(Kn kn, KnLocalFunctionsNode parent, List<Function> functions) throws IOException {
        clearOpenedListeners();
        for (Function function: functions) {
            File funcYaml = kn.getFuncFile(Paths.get(function.getLocalPath()));
            setListenerOnFile(parent, funcYaml.toPath().toString());
        }
    }

    private void clearOpenedListeners() {
        while (funcYamlListeners.size() > 0) {
            Pair<String, VirtualFileListener> entry = funcYamlListeners.remove(0);
            removeListenerOnFile(entry.getFirst(), entry.getSecond());
        }
    }

    private void removeListenerOnFile(String path, VirtualFileListener virtualFileListener) {
        VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(path);
        if (vf != null) {
            VirtualFileSystem virtualFileSystem = vf.getFileSystem();
            virtualFileSystem.removeVirtualFileListener(virtualFileListener);
        }
    }

    private void setListenerOnFile(KnLocalFunctionsNode parent, String path) {
        VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(path);
        VirtualFileListener virtualFileListener = getVirtualFileListener(parent);
        if (vf != null) {
            VirtualFileSystem virtualFileSystem = vf.getFileSystem();
            virtualFileSystem.addVirtualFileListener(virtualFileListener);
            funcYamlListeners.add(Pair.create(path, virtualFileListener));
        }
    }

    private VirtualFileListener getVirtualFileListener(KnLocalFunctionsNode parent) {
        Scheduler scheduler = new Scheduler(1000);
        return new VirtualFileListener() {
            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                scheduler.schedule(() -> TreeHelper.refreshFuncTree(project, parent));
            }

            @Override
            public void fileDeleted(@NotNull VirtualFileEvent event) {
                scheduler.schedule(() -> TreeHelper.refreshFuncTree(project, parent));
            }
        };
    }

    private List<Function> getLocalFunctionsFromOpenedProject(List<String> paths, List<Function> funcOnCluster, Kn kn) throws IOException {
        List<Function> localFunctions = new ArrayList<>();
        for(String path: paths) {
            if (hasFuncSettingsFile(path, kn)) {
                Function functionFromLocalModule = buildFuncFromLocalFuncSettingsFile(path, kn);
                if (functionFromLocalModule != null) {
                    boolean isOnCluster = funcOnCluster.stream()
                            .anyMatch(func -> func.getName().equalsIgnoreCase(functionFromLocalModule.getName())
                                    && (Strings.isNullOrEmpty(functionFromLocalModule.getNamespace()) ||
                                    func.getNamespace().equalsIgnoreCase(functionFromLocalModule.getNamespace())));
                    functionFromLocalModule.setPushed(isOnCluster);
                    localFunctions.add(functionFromLocalModule);
                }
            }
        }
        return localFunctions;
    }

    private Function buildFuncFromLocalFuncSettingsFile(String path, Kn kn) throws IOException {
        URL funcFileURL = kn.getFuncFileURL(Paths.get(path));
        String content = YAMLHelper.JSONToYAML(YAMLHelper.URLToJSON(funcFileURL));
        String name = YAMLHelper.getStringValueFromYAML(content, new String[] { "name" });
        String namespace = YAMLHelper.getStringValueFromYAML(content, new String[] { "namespace" });
        String runtime = YAMLHelper.getStringValueFromYAML(content, new String[] { "runtime" });
        String image = YAMLHelper.getStringValueFromYAML(content, new String[] { "image" });
        if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(runtime)) {
            return null;
        }
        return new Function(name, namespace, runtime, null, image, false, false, path);
    }

    private List<String> getModulesPathsWithFunc(Kn kn) {
        List<String> paths = new ArrayList<>();
        @NotNull Module[] modules = ModuleManager.getInstance(project).getModules();
        for(Module module : modules) {
            paths.addAll(getModuleRootPathsWithFunc(module, kn));

        }
        return paths;
    }

    private List<String> getModuleRootPathsWithFunc(Module module, Kn kn) {
        List<String> paths = new ArrayList<>();
        VirtualFile[] roots = getModuleRoots(module);
        for (VirtualFile root: roots) {
            if (root != null && root.isDirectory() && hasFuncSettingsFile(root.getPath(), kn)) {
                paths.add(root.getPath());
            }
        }
        return paths;
    }

    private VirtualFile[] getModuleRoots(Module module) {
        ModuleRootManager manager = ModuleRootManager.getInstance(module);
        VirtualFile[] contentRoots = manager.getContentRoots();
        List<VirtualFile> roots = new ArrayList<>();
        for(VirtualFile contentRoot: contentRoots) {
            roots.add(getModuleRootAsDirectory(contentRoot));
        }
        if (roots.isEmpty()) {
            roots.add(getModuleRootAsDirectory(LocalFileSystem.getInstance().findFileByPath(new File(module.getModuleFilePath()).getParent())));
        }
        return roots.toArray(new VirtualFile[roots.size()]);
    }

    private VirtualFile getModuleRootAsDirectory(VirtualFile root) {
        while (root != null && !root.isDirectory()) {
            root = root.getParent();
        }
        return root;
    }

    private boolean hasFuncSettingsFile(String root, Kn kn) {
        try {
            return kn.getFuncFile(Paths.get(root)).exists();
        } catch(IOException e) {
            return false;
        }
    }

    @Override
    public NodeDescriptor<?> createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
        if (element instanceof KnFunctionsNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnFunctionsNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }
        if (element instanceof KnLocalFunctionsNode) {
            return new LabelAndIconDescriptor<>(project, element, ((KnLocalFunctionsNode) element).getName(), AllIcons.Nodes.Package, parentDescriptor);
        }
        if (element instanceof KnFunctionNode) {
            return new KnFunctionDescriptor(project, (KnFunctionNode) element, parentDescriptor);
        }
        if (element instanceof KnLocalFunctionNode) {
            return new KnFunctionDescriptor(project, (KnLocalFunctionNode) element, parentDescriptor);
        }
        return super.createDescriptor(element, parentDescriptor);
    }
}
