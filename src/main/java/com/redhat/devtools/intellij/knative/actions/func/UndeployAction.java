package com.redhat.devtools.intellij.knative.actions.func;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.knative.actions.DeleteAction;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UndeployAction extends DeleteAction {
    public UndeployAction() {
        super(true, KnFunctionNode.class);
    }

    @Override
    protected String getActionName(boolean firstLetterCapital) {
        String u = firstLetterCapital ? "U" : "u";
        return u + "ndeploy";
    }

    @Override
    protected void doDelete(Project project, Kn kncli, Class type, Map<Class, List<ParentableNode>> resourcesByClass) throws IOException {
        deleteResources(type, resourcesByClass, kncli);
        TreeHelper.refreshFuncTree(project);
    }

    private void deleteResources(Class type, Map<Class, List<ParentableNode>> resourcesByClass, Kn kncli) throws IOException {
        List<String> resources = resourcesByClass.get(type).stream().map(x -> x.getName()).collect(Collectors.toList());
        if (type.equals(KnFunctionNode.class)) {
            kncli.deleteFunctions(resources);
        }
    }

    @Override
    public boolean isVisible(Object selected) {
        if (selected instanceof KnFunctionNode) {
            return ((KnFunctionNode) selected).getFunction().isPushed();
        }
        return false;
    }
}
