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
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.tree.LabelAndIconDescriptor;
import com.redhat.devtools.intellij.common.tree.MutableModel;
import com.redhat.devtools.intellij.common.tree.MutableModelSupport;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KnFunctionsTreeStructure extends AbstractKnTreeStructure  {

    public KnFunctionsTreeStructure(Project project) {
        super(project);
    }

    @Override
    public @NotNull Object[] getChildElements(@NotNull Object element) {
        Kn kn = root.getKn();
        if (kn != null) {
            if (element instanceof KnRootNode) {
                Object[] result = new Object[0];
                boolean hasKnativeServing = hasKnativeServing(kn);
                boolean hasKnativeEventing = hasKnativeEventing(kn);
                if (hasKnativeEventing && hasKnativeServing) {
                    result = getFunctionNodes((KnRootNode) element);
                } else {
                    // return node message saying it needs both
                }
                return result;
            }
        }
        return new Object[0];
    }

    private Object[] getFunctionNodes(KnRootNode parent) {
        List<Object> functions = new ArrayList<>();
        try {
            Kn kn = parent.getKn();
            List<Function> funcOnCluster = kn.getFunctions();
            // if current project contains new functions, adds them
            List<String> pathsWithFunc = getModulesPathsWithFunc();
            if (!pathsWithFunc.isEmpty()) {
                getLocalFunctionsFromOpenedProject(pathsWithFunc, funcOnCluster).forEach(it -> functions.add(new KnFunctionLocalNode(parent, parent, it)));
            }
        } catch (IOException e) {
            functions.add(new MessageNode<>(parent, parent, "Failed to load functions"));
        }
        return functions.toArray();
    }

    private List<Function> getLocalFunctionsFromOpenedProject(List<String> paths, List<Function> funcOnCluster) throws IOException {
        List<Function> localFunctions = new ArrayList<>();
        for(String path: paths) {
            if (hasFuncSettingsFile(path)) {
                Function functionFromLocalModule = buildFuncFromLocalFuncSettingsFile(path);
                if (functionFromLocalModule != null) {
                    boolean isOnCluster = funcOnCluster.stream()
                                            .anyMatch(func -> func.getName().equalsIgnoreCase(functionFromLocalModule.getName())
                                                                && (functionFromLocalModule.getNamespace().isEmpty() ||
                                                                    func.getNamespace().equalsIgnoreCase(functionFromLocalModule.getNamespace())));
                    functionFromLocalModule.setPushed(isOnCluster);
                    localFunctions.add(functionFromLocalModule);
                }
            }
        }
        return localFunctions;
    }

    private Function buildFuncFromLocalFuncSettingsFile(String path) throws IOException {
        URL settingFile = Paths.get(path, "func.yaml").toUri().toURL();
        String content = YAMLHelper.JSONToYAML(YAMLHelper.URLToJSON(settingFile));
        String name = YAMLHelper.getStringValueFromYAML(content, new String[] { "name" });
        String namespace = YAMLHelper.getStringValueFromYAML(content, new String[] { "namespace" });
        String runtime = YAMLHelper.getStringValueFromYAML(content, new String[] { "runtime" });
        String image = YAMLHelper.getStringValueFromYAML(content, new String[] { "image" });
        if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(runtime)) {
            return null;
        }
        return new Function(name, namespace, runtime, null, image, false, false, path);
    }

    private List<String> getModulesPathsWithFunc() {
        List<String> paths = new ArrayList<>();
        @NotNull Module[] modules = ModuleManager.getInstance(project).getModules();
        for(Module module : modules) {
            paths.addAll(getModuleRootPathsWithFunc(module));

        }
        return paths;
    }

    private List<String> getModuleRootPathsWithFunc(Module module) {
        List<String> paths = new ArrayList<>();
        VirtualFile[] roots = getModuleRoots(module);
        for (VirtualFile root: roots) {
            if (root != null && root.isDirectory() && hasFuncSettingsFile(root.getPath())) {
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
        return roots.toArray(new VirtualFile[0]);
    }

    private VirtualFile getModuleRootAsDirectory(VirtualFile root) {
        while (root != null && !root.isDirectory()) {
            root = root.getParent();
        }
        return root;
    }

    private boolean hasFuncSettingsFile(String root) {
        return Paths.get(root, "func.yaml").toFile().exists();
    }

    @Override
    public @Nullable Object getParentElement(@NotNull Object element) {
        return root;
    }

    @Override
    public @NotNull NodeDescriptor createDescriptor(@NotNull Object element, @Nullable NodeDescriptor parentDescriptor) {
        if (element instanceof KnRootNode) {
            return new LabelAndIconDescriptor<>(project, element, "Loading", CLUSTER_ICON, parentDescriptor);
        }

        if (element instanceof KnFunctionLocalNode) {
            return new KnFunctionDescriptor(project, (KnFunctionLocalNode) element, parentDescriptor);
        }
        return super.createDescriptor(element, parentDescriptor);
    }

}
