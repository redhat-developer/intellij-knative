package com.redhat.devtools.intellij.knative.actions.func;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.knative.actions.DeleteAction;
import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnFunctionNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import com.redhat.devtools.intellij.knative.utils.WatchHandler;
import io.fabric8.kubernetes.client.Watcher;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.redhat.devtools.intellij.knative.Constants.KIND_FUNCTION;
import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;

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
        List<String> resources = resourcesByClass.get(type).stream().map(ParentableNode::getName).collect(Collectors.toList());
        if (type.equals(KnFunctionNode.class)) {
            kncli.deleteFunctions(resources);
            notifyUndeploy(resources);
            listenToUndeployComplete(kncli, resources);
        }
    }

    private void listenToUndeployComplete(Kn kn, List<String> resources) {
        String id = "Undeploy" + System.currentTimeMillis();
        AtomicInteger cont = new AtomicInteger();
        WatchHandler.get(kn).watchResource(id, KIND_FUNCTION, (action, service) -> {
            if (action == Watcher.Action.DELETED
                    && resources.stream().anyMatch(res -> res.equalsIgnoreCase(service.getMetadata().getName()))) {
                int resourcesUndeployed = cont.incrementAndGet();
                if (resourcesUndeployed >= resources.size()) {
                    notifyUndeployComplete(resources);
                    WatchHandler.get(kn).remove(id);
                }
            }
        });
    }

    private void notifyUndeployComplete(List<String> resources) {
        String content = (resources.size() > 1 ? resources.size() + " functions" : "Function " + resources.get(0)) + " successfully undeployed.";
        notifyUndeploy(content);
    }

    private void notifyUndeploy(List<String> resources) {
        String content = "Undeploying " + (resources.size() > 1 ? resources.size() + " functions" : "function " + resources.get(0)) + " ...";
        notifyUndeploy(content);
    }

    private void notifyUndeploy(String content) {
        ExecHelper.submit(() -> {
            Notification notification = new Notification(NOTIFICATION_ID, "Undeploy functions", content, NotificationType.INFORMATION);
            Notifications.Bus.notify(notification);
        });
    }

    @Override
    public boolean isVisible(Object selected) {
        if (selected instanceof KnFunctionNode) {
            return ((KnFunctionNode) selected).getFunction().isPushed();
        }
        return false;
    }
}
