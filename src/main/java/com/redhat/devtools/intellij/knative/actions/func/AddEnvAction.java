package com.redhat.devtools.intellij.knative.actions.func;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.actions.KnAction;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.FuncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;
import java.io.IOException;

public class AddEnvAction extends KnAction {
    private static final Logger logger = LoggerFactory.getLogger(AddEnvAction.class);

    public AddEnvAction() {
        super(KnFunctionNode.class);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Kn knCli) {
        ParentableNode node = getElement(selected);
        String localPathFunc = ((KnFunctionNode) node).getFunction().getLocalPath();
        if (localPathFunc.isEmpty()) {
            return;
        }

        ExecHelper.submit(() -> {
            try {
                knCli.addEnv(localPathFunc);
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage(), e);
            }
        });
    }

    @Override
    public boolean isVisible(Object selected) {
        // addEnv action is enabled only if the user works with a func opened locally and have access to a cluster
        // this is because addEnv action can work with secrets and configmaps
        if (selected instanceof KnFunctionNode) {
            Kn kn = ((KnFunctionNode) selected).getRootNode().getKn();
            return !((KnFunctionNode) selected).getFunction().getLocalPath().isEmpty()
                    && FuncUtils.isKnativeReady(kn);
        }
        return false;
    }
}
