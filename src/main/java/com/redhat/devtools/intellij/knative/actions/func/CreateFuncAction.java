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
package com.redhat.devtools.intellij.knative.actions.func;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnFunctionLocalNode;
import com.redhat.devtools.intellij.knative.tree.KnFunctionsNode;
import com.redhat.devtools.intellij.knative.tree.KnFunctionsTreeStructure;
import com.redhat.devtools.intellij.knative.ui.createFunc.CreateFuncDialog;
import com.redhat.devtools.intellij.knative.ui.createFunc.CreateFuncModel;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.File;
import java.io.IOException;
import javax.swing.tree.TreePath;
import org.jetbrains.annotations.NotNull;

public class CreateFuncAction extends KnAction {
    public CreateFuncAction() {
        super(KnFunctionsNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        CreateFuncDialog createDialog = new CreateFuncDialog("Create new Function", getEventProject(anActionEvent));
        createDialog.setModal(true);
        createDialog.show();

        if (!createDialog.isOK()) {
            return;
        }

        CreateFuncModel model = createDialog.getModel();
        ExecHelper.submit(() -> {
            try {
                knCli.createFunc(model);
                if (model.isImportInProject()) {
                    addSourceFolder(getEventProject(anActionEvent), model.getPath(), model.getName());
                    TreeHelper.refreshFunc(getEventProject(anActionEvent));
                }
            } catch (IOException e) {
                UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Error"));
            }
        });
    }

    private void addSourceFolder(Project project, String path, String name) {
        File file = new File(path, name + ".iml");
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        ModifiableModuleModel model = ApplicationManager.getApplication().runReadAction((Computable<ModifiableModuleModel>) moduleManager::getModifiableModel);
        @NotNull Module module = model.newModule(file.getPath(), WebModuleType.WEB_MODULE);
        WriteCommandAction.runWriteCommandAction(project, model::commit);

        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        ModifiableRootModel modifiableRootModel = ApplicationManager.getApplication().runReadAction((Computable<ModifiableRootModel>) moduleRootManager::getModifiableModel);
        File directory = new File(path);
        VirtualFile dir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(directory);
        if (dir != null) {
            modifiableRootModel.addContentEntry(dir);
            WriteCommandAction.runWriteCommandAction(project, modifiableRootModel::commit);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        if (Constants.TOOLBAR_PLACE.equals(e.getPlace())) {
            e.getPresentation().setVisible(true);
        } else {
            super.update(e);
        }
    }
}
