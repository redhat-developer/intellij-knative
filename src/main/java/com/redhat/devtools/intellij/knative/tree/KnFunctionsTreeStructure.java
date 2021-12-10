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
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.utils.Scheduler;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                Pair<Object[], List<String>> children = getFunctionNodes(root);
                root.showWarnings(children.getSecond());
                clusterModelSynchronizer.updateElementOnChange(root, KIND_FUNCTION);
                return children.getFirst();
            }
        }

        return new Object[0];
    }

    private Pair<Object[], List<String>> getFunctionNodes(KnRootNode parent) {
        Kn kn = parent.getKn();
        List<String> warnings = new ArrayList<>();
        List<Function> functions = new ArrayList<>();
        addFunctionsOnCluster(kn, functions, warnings);
        addLocalFunctions(kn, functions, warnings);

        List<Object> functionNodes = new ArrayList<>();
        functions.forEach(f -> functionNodes.add(new KnFunctionNode(parent, parent, f)));
        return Pair.create(functionNodes.toArray(), warnings);
    }

    private void addFunctionsOnCluster(Kn kn, List<Function> functions, List<String> warnings) {
        if (hasKnativeEventing(kn) && hasKnativeServing(kn)) {
            try {
                functions.addAll(kn.getFunctions());
            } catch (IOException e) {
                warnings.add("Unable to load deployed functions. Check logs for more infos.");
                logger.warn(e.getLocalizedMessage(), e);
            }
        } else {
            warnings.add("Unable to load deployed functions. Functions need both knative serving and eventing installed to work.");
        }
    }

    private void addLocalFunctions(Kn kn, List<Function> functions, List<String> warnings) {
        try {
            // if current project contains new functions, adds them
            List<String> pathsWithFunc = getModulesPathsWithFunc(kn);
            if (!pathsWithFunc.isEmpty()) {
                addLocalFunctionsFromOpenedProject(pathsWithFunc, kn, functions, warnings);
                setListenerToLocalFunctions(kn, functions);
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    private void setListenerToLocalFunctions(Kn kn, List<Function> functions) throws IOException {
        clearOpenedListeners();
        for (Function function: functions) {
            if (!Strings.isNullOrEmpty(function.getLocalPath())) {
                File funcYaml = kn.getFuncFile(Paths.get(function.getLocalPath()));
                setListenerOnFile(funcYaml.toPath().toString());
            }
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

    private void setListenerOnFile(String path) {
        VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(path);
        VirtualFileListener virtualFileListener = getVirtualFileListener();
        if (vf != null) {
            VirtualFileSystem virtualFileSystem = vf.getFileSystem();
            virtualFileSystem.addVirtualFileListener(virtualFileListener);
            funcYamlListeners.add(Pair.create(path, virtualFileListener));
        }
    }

    private VirtualFileListener getVirtualFileListener() {
        Scheduler scheduler = new Scheduler(1000);
        return new VirtualFileListener() {
            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                scheduler.schedule(() -> TreeHelper.refreshFuncTree(project));
            }

            @Override
            public void fileDeleted(@NotNull VirtualFileEvent event) {
                scheduler.schedule(() -> TreeHelper.refreshFuncTree(project));
            }
        };
    }

    private void addLocalFunctionsFromOpenedProject(List<String> paths, Kn kn, List<Function> functionsOnCluster, List<String> warnings)  {
        for(String path: paths) {
            try {
                if (hasFuncSettingsFile(path, kn)) {
                    Function functionFromLocalModule = buildFuncFromLocalFuncSettingsFile(path, kn);
                    if (functionFromLocalModule != null) {
                        Optional<Function> functionOnCluster = functionsOnCluster.stream()
                                .filter(func -> func.getName().equalsIgnoreCase(functionFromLocalModule.getName())
                                        && func.getRuntime().equalsIgnoreCase(functionFromLocalModule.getRuntime())
                                        && (Strings.isNullOrEmpty(functionFromLocalModule.getNamespace()) ||
                                        func.getNamespace().equalsIgnoreCase(functionFromLocalModule.getNamespace())))
                                .findFirst();
                        if (functionOnCluster.isPresent()) {
                            functionOnCluster.get().setImage(functionFromLocalModule.getImage());
                            functionOnCluster.get().setLocalPath(functionFromLocalModule.getLocalPath());
                        } else {
                            functionsOnCluster.add(functionFromLocalModule);
                        }
                    }
                }
            } catch (IOException e) {
                warnings.add("Unable to load local functions. Check logs for more infos.");
                logger.warn(e.getLocalizedMessage(), e);
            }
        }
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
        if (element instanceof KnFunctionNode) {
            return new KnFunctionDescriptor(project, (KnFunctionNode) element, parentDescriptor);
        }
        return super.createDescriptor(element, parentDescriptor);
    }
}
