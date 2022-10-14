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
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.JBColor;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.vcs.log.Hash;
import com.redhat.devtools.intellij.knative.utils.model.GitRepoModel;
import git4idea.GitLocalBranch;
import git4idea.GitUtil;
import git4idea.branch.GitBranchesCollection;
import git4idea.index.GitFileStatus;
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
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private GitRepository gitRepository;

    public GitDialog(Project project, String title, String descriptionText, GitRepository gitRepository) {
        super(project, true);
        this.project = project;
        this.gitRepository = gitRepository;
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
        Pair<String, String> currentRemoteAndBranch = getCurrentRemoteAndBranch();
        addTopWarningMessage(currentRemoteAndBranch.getFirst());
        addTopDescription(descriptionText);


        addRepoField(currentRemoteAndBranch.getFirst());
        addErrorRepo();
        addBranchField(currentRemoteAndBranch.getSecond());

        JPanel panel = new JPanel(new BorderLayout());
        contentPanel.add(panel);
    }

    private void addTopWarningMessage(String remote) {
        String message = "";
        if (gitRepository == null) {
            message = "Your project is not a git repository. Please initialize it before to proceed building it on cluster.";
        } else {
            List<GitFileStatus> gitFileStatuses = gitRepository.getStagingAreaHolder().getAllRecords();
            if (remote.isEmpty()) {
                message = "Your local branch is not present remotely. Push it before to proceed building it on cluster";
            } else if (!gitFileStatuses.isEmpty()) {
                message = "Your local branch contains some uncommitted changes. Push them before to proceed building it on cluster";
            }
        }
        if (!message.isEmpty()) {
            JLabel topDescription = new JLabel(message, AllIcons.General.BalloonWarning, SwingConstants.LEFT);
            topDescription.setBorder(JBUI.Borders.empty(10, 0, 0, 10));
            addComponentToContent(contentPanel, null, topDescription, null, 5);
        }
    }

    private Pair<String, String> getCurrentRemoteAndBranch() {
        if (gitRepository == null) {
            return Pair.create("", "");
        }
        GitLocalBranch localBranch = gitRepository.getCurrentBranch();
        if (localBranch == null) {
            List<GitRemote> gitRemotes = new ArrayList<>((gitRepository.getRemotes()));
            String currentRemoteUrl = !gitRemotes.isEmpty() ? gitRemotes.get(0).getFirstUrl() : "";
            return Pair.create(currentRemoteUrl, "");
        }
        Hash localBranchHash = gitRepository.getBranches().getHash(localBranch);
        Optional<String> remoteUrl = gitRepository.getBranches().getRemoteBranches().stream()
                .filter(gitRemoteBranch -> gitRemoteBranch.getNameForRemoteOperations().equalsIgnoreCase(localBranch.getName())
                            && Objects.equals(gitRepository.getBranches().getHash(gitRemoteBranch), localBranchHash))
                .map(gitRemoteBranch -> gitRemoteBranch.getRemote().getFirstUrl()).findFirst();
        return remoteUrl.map(s -> Pair.create(s, localBranch.getName())).orElse(Pair.create("", ""));
    }

    private void addTopDescription(String descriptionText) {
        JLabel topDescription = new JLabel(descriptionText);
        topDescription.setBorder(JBUI.Borders.empty(10, 0, 10, 10));
        addComponentToContent(contentPanel, null, topDescription, null, 5);
    }

    private void addRepoField(String remote) {
        JLabel lblIDParam = createLabel("Repo:",
                "The repository to use for building and deploying.",
                null);

        List<GitRemote> gitRemotes = gitRepository != null
                ? new ArrayList<>((gitRepository.getRemotes()))
                : Collections.emptyList();
        txtRemotesWithAutoCompletion = createTextFieldAutoCompletion(
                gitRemotes,
                remote,
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

    private void addBranchField(String branch) {
        JLabel lblBranchParam = createLabel("Branch:",
                "The branch to use for building and deploying. If left empty, the main branch is used.",
                null);

        txtBranchesWithAutoCompletion = createTextFieldAutoCompletion(
                new ArrayList(),
                branch,
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
        GitBranchesCollection gitBranchesCollection = gitRepository.getBranches();
        return gitBranchesCollection.getRemoteBranches().stream()
                .filter(branch -> Objects.requireNonNull(branch.getRemote().getFirstUrl()).equalsIgnoreCase(currentRemoteUrl))
                .map(branch -> LookupElementBuilder.create(branch, branch.getNameForRemoteOperations())).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        GitDialog dialog = new GitDialog(null, "", "", null);
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

