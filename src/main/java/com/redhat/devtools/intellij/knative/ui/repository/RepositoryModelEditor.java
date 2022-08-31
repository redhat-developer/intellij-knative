/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.ui.repository;

import com.intellij.ui.ListUtil;
import com.intellij.ui.ScrollingUtil;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.ListItemEditor;
import com.intellij.util.ui.ListModelEditor;
import com.intellij.util.ui.ListModelEditorBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.util.List;


public class RepositoryModelEditor extends ListModelEditorBase<Repository> {
    private final ToolbarDecorator toolbarDecorator;

    private final JBList<Repository> list = new JBList<>(model);

    public RepositoryModelEditor(@NotNull ListItemEditor<Repository> itemEditor) {
        super(itemEditor);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(SimpleListCellRenderer.create("", itemEditor::getName));

        toolbarDecorator = ToolbarDecorator.createDecorator(list, model)
                .setAddAction(action -> {
                    Repository item = createElement();
                    if (item != null) {
                        model.add(item);
                        itemEditor.clone(item, false);
                        ScrollingUtil.selectItem(list, ContainerUtil.indexOfIdentity(model.getItems(), item));
                    }
                })
                .setRemoveAction(action -> {
                    Repository repositoryToDelete = list.getSelectedValue();
                    if (itemEditor.isRemovable(repositoryToDelete)) {
                        model.remove(repositoryToDelete);
                    }
                });
    }

    @Override
    public Repository createElement() {
        Repository repository = new Repository();
        CreateRepositoryDialog createRepositoryDialog = new CreateRepositoryDialog(repository);
        createRepositoryDialog.show();
        if (createRepositoryDialog.isOK()) {
            return repository;
        }
        return null;
    }

    @NotNull
    public RepositoryModelEditor disableUpDownActions() {
        toolbarDecorator.disableUpDownActions();
        return this;
    }

    @NotNull
    public JComponent createComponent() {
        return toolbarDecorator.createPanel();
    }

    @NotNull
    public JBList getList() {
        return list;
    }

    @Nullable
    public Repository getSelected() {
        return list.getSelectedValue();
    }

    @Override
    protected void removeEmptyItem(int i) {
        ListUtil.removeIndices(list, new int[]{i});
    }
}
