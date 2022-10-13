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
package com.redhat.devtools.intellij.knative.actions.func;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.func.FuncActionPipelineBuilder;
import com.redhat.devtools.intellij.knative.func.FuncActionTask;
import com.redhat.devtools.intellij.knative.func.IFuncActionPipeline;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.ui.GitDialog;
import com.redhat.devtools.intellij.knative.utils.FuncUtils;
import com.redhat.devtools.intellij.knative.utils.model.GitRepoModel;
import com.redhat.devtools.intellij.knative.utils.model.ImageRegistryModel;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.GitUtil;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import org.jetbrains.plugins.github.api.data.GHRepository;
import org.jetbrains.plugins.github.authentication.GithubAuthenticationManager;
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.swing.tree.TreePath;
import java.io.IOException;

import static com.intellij.openapi.ui.Messages.showInputDialog;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class OnClusterBuildAction extends DeployAction {

    private static final Logger logger = LoggerFactory.getLogger(OnClusterBuildAction.class);

    public OnClusterBuildAction() {
        super();
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        ParentableNode node = getElement(selected);
        Function function = ((KnFunctionNode) node).getFunction();
        TelemetryMessageBuilder.ActionMessage telemetry = createTelemetry();
        Project project = anActionEvent.getProject();

        GitRepoModel gitRepo = getRepoInfo(project);
        if (gitRepo == null) {
            return;
        }

        ImageRegistryModel model = confirmAndGetRegistryImage(project, function, knCli, true, telemetry);
        if (model == null || !model.isValid()) {
            return;
        }
        if (!Strings.isNullOrEmpty(model.getImage())) {
            function.setImage(model.getImage());
        }

        IFuncActionPipeline deployPipeline = new FuncActionPipelineBuilder()
                .createDeployPipeline(project, function)
                .withTask("onClusterBuildFunc", (task) -> doDeploy(node.getName(), knCli, task, gitRepo,
                        model, telemetry))
                .build();
        knCli.getFuncActionPipelineManager().start(deployPipeline);
    }

    private GitRepoModel getRepoInfo(Project project) {
        GitDialog gitDialog = new GitDialog(project, "On-Cluster Build", "Provide the Git repository/branch where to pull the code from");
        gitDialog.show();
        if (gitDialog.isOK()) {
            return gitDialog.getGitInfo();
        }
        return null;
    }

    private void doDeploy(String name, Kn knCli, FuncActionTask funcActionTask, GitRepoModel gitRepo, ImageRegistryModel model, TelemetryMessageBuilder.ActionMessage telemetry) {
        ExecHelper.submit(() -> {
            String namespace = knCli.getNamespace();
            try {
                knCli.onClusterBuildFunc(namespace, funcActionTask.getFunction().getLocalPath(), gitRepo, model,
                        funcActionTask.getTerminalExecutionConsole(), funcActionTask.getProcessListener());
                telemetry
                        .result(anonymizeResource(name, knCli.getNamespace(), getSuccessMessage(namespace, name)))
                        .send();
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
                telemetry
                        .error(anonymizeResource(name, namespace, e.getLocalizedMessage()))
                        .send();
            }
        });
    }

    @Override
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible) {
            Kn kn = ((KnFunctionNode) selected).getRootNode().getKn();
            return FuncUtils.isTektonReady(kn);
        }
        return false;
    }

}
