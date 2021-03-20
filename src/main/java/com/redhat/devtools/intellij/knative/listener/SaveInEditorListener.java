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
package com.redhat.devtools.intellij.knative.listener;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentSynchronizationVetoer;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.tree.KnTreeStructure;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.KnHelper;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.common.CommonConstants.LAST_MODIFICATION_STAMP;
import static com.redhat.devtools.intellij.common.CommonConstants.PROJECT;
import static com.redhat.devtools.intellij.knative.Constants.KNATIVE;
import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;
import static com.redhat.devtools.intellij.knative.Constants.TARGET_NODE;

public class SaveInEditorListener extends FileDocumentSynchronizationVetoer {

    private static final Logger logger = LoggerFactory.getLogger(SaveInEditorListener.class);

    @Override
    public boolean maySaveDocument(@NotNull Document document, boolean isSaveExplicit) {
        VirtualFile vf = FileDocumentManager.getInstance().getFile(document);
        Project project = vf.getUserData(PROJECT);
        Long lastModificationStamp = vf.getUserData(LAST_MODIFICATION_STAMP);
        Long currentModificationStamp = document.getModificationStamp();
        if (project == null ||
                !isFileToPush(project, vf) ||
                currentModificationStamp.equals(lastModificationStamp)
        ) {
            return true;
        }

        vf.putUserData(LAST_MODIFICATION_STAMP, currentModificationStamp);
        if (save(document, project)) {
            notify(document);
            TreeHelper.refresh(project, vf.getUserData(TARGET_NODE));
        }
        return false;
    }

    private void notify(Document document) {
        try {
            String kind = YAMLHelper.getStringValueFromYAML(document.getText(), new String[] { "kind" });
            String name = YAMLHelper.getStringValueFromYAML(document.getText(), new String[] { "metadata", "name" });
            Notification notification = new Notification(NOTIFICATION_ID, "Save Successful", kind + " " + name + " has been saved!", NotificationType.INFORMATION);
            Notifications.Bus.notify(notification);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
    }

    private boolean save(Document document, Project project) {
        try {
            return KnHelper.saveOnCluster(project, document.getText());
        } catch (IOException e) {
            Notification notification = new Notification(NOTIFICATION_ID, "Error", "An error occurred while saving \n" + e.getLocalizedMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification);
            logger.warn("Error: " + e.getLocalizedMessage(), e);
            return false;
        }
    }

    private boolean isFileToPush(Project project, VirtualFile vf) {
        FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor();
        // if file is not the one selected, skip it
        if (selectedEditor == null || !selectedEditor.getFile().equals(vf)) return false;
        // if file is not related to tekton, skip it
        if (vf == null || vf.getUserData(KNATIVE) == null || !vf.getUserData(KNATIVE).equalsIgnoreCase(NOTIFICATION_ID)) {
            return false;
        }
        return true;
    }
}
