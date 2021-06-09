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
package com.redhat.devtools.intellij.knative.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.common.editor.AllowNonProjectEditing;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.VirtualFileHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.common.CommonConstants.PROJECT;
import static com.redhat.devtools.intellij.common.CommonConstants.TARGET_NODE;
import static com.redhat.devtools.intellij.knative.Constants.KNATIVE;
import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;

public class EditorHelper {
    private static Logger logger = LoggerFactory.getLogger(EditorHelper.class);

    public static void openKnComponentInEditor(TreePath path) {
        Object node = StructureTreeAction.getElement(path.getLastPathComponent());
        if(node instanceof ParentableNode){
            openKnComponentInEditor((ParentableNode<?>) node);
        }
        // else silently ignore non parentable nodes
    }

    public static void openKnComponentInEditor(ParentableNode<?> node) {
        if (node == null) return;

        try {
            String yaml = KnHelper.getYamlFromNode(node);
            if (!yaml.isEmpty()) {
                openVirtualFileInEditor(node.getRootNode().getProject(), node.getName() + ".yaml", yaml, KnHelper.isWritable(node), node);
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error while opening knative component " + node.getName() + ": " + e.getLocalizedMessage(), "Error"));
        }
    }

    private static void openVirtualFileInEditor(Project project, String name, String content, boolean isWritable, ParentableNode<?> targetNode) throws IOException {
        Optional<FileEditor> editor = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors())
                                            .filter(fileEditor -> fileEditor.getFile().getName().startsWith(name))
                                            .findFirst();
        if (!editor.isPresent()) {
            VirtualFile virtualFile = createVirtualFile(project, name, content, isWritable, targetNode);
            FileEditorManager.getInstance(project).openFile(virtualFile, true);
        } else {
            Editor openedEditor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, editor.get().getFile()), true);
            updateVirtualFile(openedEditor.getDocument(), content);
        }
    }

    private static VirtualFile createVirtualFile(Project project, String name, String content, boolean isWritable, ParentableNode<?> targetNode) throws IOException {
        VirtualFile vf;

        if (isWritable) {
            vf = VirtualFileHelper.createTempFile(name, content);
            vf.putUserData(PROJECT, project);
            vf.putUserData(KNATIVE, NOTIFICATION_ID);
            vf.putUserData(AllowNonProjectEditing.ALLOW_NON_PROJECT_EDITING, true);
            if (targetNode != null) vf.putUserData(TARGET_NODE, targetNode);
        } else {
            vf = new LightVirtualFile(name, content);
            vf.setWritable(false);
        }
        return vf;
    }

    private static void updateVirtualFile(Document document, String newContent) {
        if (document.getText().equalsIgnoreCase(newContent)) {
            return;
        }
        ApplicationManager.getApplication().runWriteAction(() -> {
            document.setReadOnly(false);
            document.setText(newContent);
            if (!document.isWritable()) {
                document.setReadOnly(true);
            }
        });
    }

    public static String getSnippet(String kind) throws IOException {
        URL snippet = EditorHelper.class.getResource("/snippets/" + kind + ".yaml");
        if (snippet == null) {
            return "";
        }
        return YAMLHelper.JSONToYAML(YAMLHelper.URLToJSON(snippet));
    }
}
