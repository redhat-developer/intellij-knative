package com.redhat.devtools.intellij.knative.actions;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.knative.actions.func.UndeployAction;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UndeployActionTest extends ActionTest {

    private static final String FUNCTION = "function";

    @Test
    public void ExecuteDelete_OneKnFunctionNodeSelected_DeleteOneFunction() throws IOException {
        Map<String, Integer> typePerTimesCalled = new HashMap<>();
        typePerTimesCalled.put(FUNCTION, 1);
        executeUndeployAction(new ParentableNode[] {knFunctionNode}, typePerTimesCalled);
    }

    private void executeUndeployAction(ParentableNode[] fakeSelectedNodesToUndeployed, Map<String, Integer> typePerTimesCalled) throws IOException {
        UndeployAction action = new UndeployAction();
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            when(kn.getNamespace()).thenReturn("namespace");
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any(Project.class))).thenReturn(kn);
            treeHelperMockedStatic.when(() -> TreeHelper.getKnFunctionsTreeStructure(any())).thenReturn(null);
            action.executeDelete(project, kn, fakeSelectedNodesToUndeployed);
            verify(kn, times(typePerTimesCalled.getOrDefault(FUNCTION, 0))).deleteFunctions(any());
        }
    }
}
