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
package com.redhat.devtools.intellij.knative.ui;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.Consumer;
import com.intellij.util.ui.JBUI;
import com.redhat.devtools.intellij.knative.utils.model.GitRepoModel;
import git4idea.GitLocalBranch;
import git4idea.GitUtil;
import git4idea.branch.GitBranchesCollection;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.knative.ui.UIConstants.RED_BORDER_SHOW_ERROR;

public class GitDialog extends BaseDialog {
    private JPanel wrapperPanel, contentPanel, panelGitRepoError;
    private JBScrollPane scrollPane;
    private TextFieldWithAutoCompletion<GitRemote> txtRemotesWithAutoCompletion;
    private TextFieldWithAutoCompletion<String> txtBranchesWithAutoCompletion;
    private JLabel lblGitRepoError;
    private Project project;
    private Border originalBorder;
    private List<GitRepository> gitRepositories;

    public GitDialog(Project project, String title, String descriptionText) {
        super(project, true);
        this.project = project;
        gitRepositories = GitUtil.getRepositoryManager(project).getRepositories();
        setTitle(title);
        buildStructure(descriptionText);
        init();
    }

    private void buildStructure(String descriptionText) {
        wrapperPanel = new JPanel(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        fillContentPanel(descriptionText);


        scrollPane = new JBScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(JBUI.Borders.empty());

        wrapperPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void fillContentPanel(String descriptionText) {
        addTopDescription(descriptionText);
        addRepoField();
        addErrorRepo();
        addBranchField();

        JPanel panel = new JPanel(new BorderLayout());
        contentPanel.add(panel);
    }

    private void addTopDescription(String descriptionText) {
        JLabel topDescription = new JLabel(descriptionText);
        topDescription.setBorder(JBUI.Borders.empty(10, 0, 10, 10));
        addComponentToContent(contentPanel, null, topDescription, null, 5);
    }

    private void addRepoField() {
        JLabel lblIDParam = createLabel("Repo:",
                "The repository to use for building and deploying.",
                null);

        List<GitRemote> gitRemotes = !gitRepositories.isEmpty()
                ? new ArrayList<>((gitRepositories.get(0).getRemotes()))
                : Collections.emptyList();
        txtRemotesWithAutoCompletion = createTextFieldAutoCompletion(
                gitRemotes,
                !gitRemotes.isEmpty() ? gitRemotes.get(0).getFirstUrl() : "",
                (item) -> {
                    if (panelGitRepoError.isVisible()) {
                        panelGitRepoError.setVisible(false);
                        txtRemotesWithAutoCompletion.setBorder(originalBorder);
                    }
                    return ((GitRemote)item).getFirstUrl();
                },
                null
        );
        originalBorder = txtRemotesWithAutoCompletion.getBorder();

        addComponentToContent(contentPanel, lblIDParam, txtRemotesWithAutoCompletion, null, 0);
    }

    private void addErrorRepo() {

        lblGitRepoError = new JLabel("Please provide a valid git repository");
        lblGitRepoError.setForeground(JBColor.RED);

        panelGitRepoError = new JPanel(new FlowLayout());
        panelGitRepoError.add(lblGitRepoError);
        panelGitRepoError.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelGitRepoError.setVisible(false);

        contentPanel.add(panelGitRepoError);
    }

    private void addBranchField() {
        JLabel lblBranchParam = createLabel("Branch:",
                "The branch to use for building and deploying. If left empty, the main branch is used.",
                null);

        GitLocalBranch gitLocalBranch = gitRepositories.get(0).getCurrentBranch();
        txtBranchesWithAutoCompletion = createTextFieldAutoCompletion(
                new ArrayList(),
                gitLocalBranch != null ? gitLocalBranch.getName() : "",
                (item) -> (String) item,
                () -> getBranchesAutoCompletion()
        );
        addComponentToContent(contentPanel, lblBranchParam, txtBranchesWithAutoCompletion, null, 5);
    }

    private <T> TextFieldWithAutoCompletion createTextFieldAutoCompletion(List<T> initialValues, String defaultValue, Function<Object, String> getLookupString, Supplier<List<LookupElement>> getLookupItems) {
        return new TextFieldWithAutoCompletion(project,
                new TextFieldWithAutoCompletionListProvider(initialValues) {
                    @Override
                    protected @NotNull String getLookupString(@NotNull Object item) {
                        return getLookupString.apply(item);
                    }

                    @Override
                    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull String prefix, @NotNull CompletionResultSet result) {
                        if (getLookupItems != null) {
                            result.addAllElements(getLookupItems.get());
                        }
                        super.fillCompletionVariants(parameters, prefix, result);
                    }
                },
                true,
                defaultValue
        ) {
            @Override
            protected EditorEx createEditor() {
                EditorEx editor = super.createEditor();
                CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(new JTextField().getBorder(), JBUI.Borders.empty(7, 7, 3, 7));
                editor.setBorder(compoundBorder);
                editor.getContentSize().setSize(200, new JTextField().getHeight());
                return editor;
            }
        };
    }

    private List<LookupElement> getBranchesAutoCompletion() {
        String currentRemoteUrl = txtRemotesWithAutoCompletion.getText();
        GitBranchesCollection gitBranchesCollection = gitRepositories.get(0).getBranches();
        return gitBranchesCollection.getRemoteBranches().stream()
                .filter(branch -> branch.getRemote().getFirstUrl().equalsIgnoreCase(currentRemoteUrl))
                .map(branch -> LookupElementBuilder.create(branch, branch.getNameForRemoteOperations())).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        GitDialog dialog = new GitDialog(null, "", "");
        dialog.pack();
        dialog.show();
        System.exit(0);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setMinimumSize(new Dimension(500, 200));
        panel.add(wrapperPanel, BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected void doOKAction() {
        if (txtRemotesWithAutoCompletion.getText().isEmpty()) {
            panelGitRepoError.setVisible(true);
            txtRemotesWithAutoCompletion.setBorder(RED_BORDER_SHOW_ERROR);
            contentPanel.invalidate();
            return;
        }
        super.doOKAction();
    }

    public GitRepoModel getGitInfo() {
        String repo = txtRemotesWithAutoCompletion.getText();
        String branch = txtBranchesWithAutoCompletion.getText();
        return new GitRepoModel(repo, branch);
    }
}

