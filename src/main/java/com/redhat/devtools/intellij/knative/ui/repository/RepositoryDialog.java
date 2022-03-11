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

import com.intellij.openapi.keymap.impl.ui.KeymapListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.CollectionListModel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListItemEditor;
import com.intellij.util.ui.ListModelEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

public class RepositoryDialog extends DialogWrapper {

    public static final String EMPTY = "empty";
    public static final String PANEL = "panel";

    private final ListItemEditor<Repository> itemEditor = new ListItemEditor<Repository>() {

        @NotNull
        @Override
        public Class<Repository> getItemClass() {
            return Repository.class;
        }

        @Override
        public Repository clone(@NotNull Repository item, boolean forInPlaceEditing) {
            return new Repository(item.getName(), item.getUrl());
        }

        @Override
        public boolean isEmpty(@NotNull Repository item) {
            return item.getName().isEmpty() && item.getUrl().isEmpty();
        }

        @NotNull
        @Override
        public String getName(@NotNull Repository item) {
            return item.getName();
        }

        @Override
        public boolean isRemovable(@NotNull Repository item) {
            return true;
        }
    };

    private final RepositoryModelEditor editor = new RepositoryModelEditor(itemEditor);

    private JComponent component;
    private JPanel itemPanelWrapper;
    private List<Repository> repositories;
    private JTextField txtName, txtUrl;

    public RepositoryDialog(@Nullable Project project, List<Repository> repositories) {
        super(project, false);
        this.repositories = repositories;
        setTitle("Repository");
        setOKButtonText("Apply");
        buildStructure(repositories);
        init();
    }

    public void buildStructure(List<Repository> repositories) {
        final CardLayout cardLayout = new CardLayout();
        editor.getModel().add(repositories);

        // doesn't make any sense (and in any case scheme manager cannot preserve order)
        editor.disableUpDownActions();

        editor.getList().addListSelectionListener(e -> {
            Repository item = editor.getSelected();
            if (item == null) {
                cardLayout.show(itemPanelWrapper, EMPTY);
            }
            else {
                txtName.setText(item.getName());
                txtUrl.setText(item.getUrl());
                cardLayout.show(itemPanelWrapper, PANEL);
            }
        });

        itemPanelWrapper = new JPanel(cardLayout);

        JLabel descLabel = new JLabel("<html>Repositories allow you to add and use new templates when creating a new Function.</html>");
        descLabel.setBorder(JBUI.Borders.empty(0, 25));

        itemPanelWrapper.add(descLabel, EMPTY);
        txtName = new JTextField();
        txtUrl = new JTextField();
        itemPanelWrapper.add(RepositoryUtils.createPanelRepository(txtName, txtUrl), PANEL);


        Splitter splitter = new Splitter(false, 0.3f);
        splitter.setFirstComponent(editor.createComponent());
        splitter.setSecondComponent(itemPanelWrapper);
        component = splitter;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(component, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(550, 300));
        return wrapper;
    }

    public CollectionListModel<Repository> getModel() {
        return editor.getModel();
    }
}
