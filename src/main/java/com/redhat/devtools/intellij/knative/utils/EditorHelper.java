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
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditorHelper {
    private static Logger logger = LoggerFactory.getLogger(EditorHelper.class);

    public static void openKnComponentInEditor(ParentableNode node) {
        if (node == null) {
            return;
        }

        try {
            String yaml = KnHelper.getYamlFromNode(node);
            if (!yaml.isEmpty()) {
                openVirtualFileInEditor(node.getRootNode().getProject(), node.getName() + ".yaml", yaml);
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
            UIHelper.executeInUI(() -> Messages.showErrorDialog("Error while opening knative component " + node.getName() + ": " + e.getLocalizedMessage(), "Error"));
        }
    }

    public static void openVirtualFileInEditor(Project project, String name, String content) throws IOException {
        Optional<FileEditor> editor = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors())
                                            .filter(fileEditor -> fileEditor.getFile().getName().startsWith(name))
                                            .findFirst();
        if (!editor.isPresent()) {
            VirtualFile virtualFile = createVirtualFile(name, content);
            FileEditorManager.getInstance(project).openFile(virtualFile, true);
        } else {
            Editor openedEditor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, editor.get().getFile()), true);
            updateVirtualFile(openedEditor.getDocument(), content);
        }
    }

    public static VirtualFile createVirtualFile(String name, String content) throws IOException {
        VirtualFile vf = new LightVirtualFile(name, content);
        vf.setWritable(false);
        return vf;
    }

    public static void updateVirtualFile(Document document, String newContent) {
        /*if (document.getText().equalsIgnoreCase(newContent)) {
            return;
        }*/
        ApplicationManager.getApplication().runWriteAction(() -> {
            document.setReadOnly(false);
            document.setText(newContent);
            if (!document.isWritable()) {
                document.setReadOnly(true);
            }
        });
    }
}
