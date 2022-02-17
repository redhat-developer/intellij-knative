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
package com.redhat.devtools.intellij.knative.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.Constants;
import com.redhat.devtools.intellij.knative.actions.func.DeployAction;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.tree.TreePath;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class DeployActionTest extends ActionTest {
    private Function function;
    private Path pathFuncFile;
    private JsonNode jsonNode;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        function = mock(Function.class);
        pathFuncFile = mock(Path.class);
        jsonNode = mock(JsonNode.class);
    }

    @Test
    public void ActionPerformed_SelectedHasNotLocalPath_DoNothing() throws IOException, InterruptedException {
        AnAction action = new DeployAction();
        AnActionEvent anActionEvent = createDeployActionEvent();
        when(function.getLocalPath()).thenReturn("");
        try (MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            when(kn.getNamespace()).thenReturn("namespace");
            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any(Project.class))).thenReturn(kn);
            action.actionPerformed(anActionEvent);
            Thread.sleep(1000);
            verify(kn, times(0)).deployFunc(anyString(), anyString(), anyString(), anyString());
        }
    }

    @Test
    public void ActionPerformed_SelectedHasLocalPathAndImageSpecified_DoDeploy() throws IOException, InterruptedException {
        AnAction action = new DeployAction();
        AnActionEvent anActionEvent = createDeployActionEvent();
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try (MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
                try (MockedStatic<YAMLHelper> yamlHelperMockedStatic = mockStatic(YAMLHelper.class)) {
                    try(MockedStatic<Messages> messagesMockedStatic = mockStatic(Messages.class)) {
                        treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
                        when(kn.getFuncFileURL(any())).thenReturn(mock(URL.class));
                        pathsMockedStatic.when(() -> Paths.get(anyString(), anyString())).thenReturn(pathFuncFile);
                        yamlHelperMockedStatic.when(() -> YAMLHelper.URLToJSON(any())).thenReturn(jsonNode);
                        yamlHelperMockedStatic.when(() -> YAMLHelper.JSONToYAML(any())).thenReturn("image: test");
                        yamlHelperMockedStatic.when(() -> YAMLHelper.getStringValueFromYAML(anyString(), any(String[].class))).thenReturn("").thenReturn("test");
                        messagesMockedStatic.when(() -> Messages.showOkCancelDialog(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(Messages.OK);
                        action.actionPerformed(anActionEvent);
                        Thread.sleep(1000);
                        verify(kn, times(1)).deployFunc("namespace", "path", "", "test");
                    }
                }
            }
        }

    }

    @Test
    public void ActionPerformed_SelectedHasLocalPathAndImageSpecifiedButDeployIsNotConfirmed_DoDeploy() throws IOException, InterruptedException {
        AnAction action = new DeployAction();
        AnActionEvent anActionEvent = createDeployActionEvent();
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try (MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
                try (MockedStatic<YAMLHelper> yamlHelperMockedStatic = mockStatic(YAMLHelper.class)) {
                    try(MockedStatic<Messages> messagesMockedStatic = mockStatic(Messages.class)) {
                        treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
                        pathsMockedStatic.when(() -> Paths.get(anyString(), anyString())).thenReturn(pathFuncFile);
                        when(kn.getFuncFileURL(any())).thenReturn(mock(URL.class));
                        yamlHelperMockedStatic.when(() -> YAMLHelper.URLToJSON(any())).thenReturn(jsonNode);
                        yamlHelperMockedStatic.when(() -> YAMLHelper.JSONToYAML(any())).thenReturn("image: test");
                        yamlHelperMockedStatic.when(() -> YAMLHelper.getStringValueFromYAML(anyString(), any(String[].class))).thenReturn("").thenReturn("test");
                        messagesMockedStatic.when(() -> Messages.showOkCancelDialog(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(Messages.CANCEL);
                        action.actionPerformed(anActionEvent);
                        Thread.sleep(1000);
                        verify(kn, times(0)).deployFunc("namespace", "path", "", "test");
                    }
                }
            }
        }

    }

    @Test
    public void ActionPerformed_SelectedHasLocalPathAndRegistrySpecified_DoDeploy() throws IOException, InterruptedException {
        AnAction action = new DeployAction();
        AnActionEvent anActionEvent = createDeployActionEvent();
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try (MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
                try (MockedStatic<YAMLHelper> yamlHelperMockedStatic = mockStatic(YAMLHelper.class)) {
                    try(MockedStatic<Messages> messagesMockedStatic = mockStatic(Messages.class)) {
                        treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
                        pathsMockedStatic.when(() -> Paths.get(anyString(), anyString())).thenReturn(pathFuncFile);
                        when(kn.getFuncFileURL(any())).thenReturn(mock(URL.class));
                        yamlHelperMockedStatic.when(() -> YAMLHelper.URLToJSON(any())).thenReturn(jsonNode);
                        yamlHelperMockedStatic.when(() -> YAMLHelper.JSONToYAML(any())).thenReturn("registry: test");
                        yamlHelperMockedStatic.when(() -> YAMLHelper.getStringValueFromYAML(anyString(), any(String[].class))).thenReturn("test").thenReturn("");
                        messagesMockedStatic.when(() -> Messages.showOkCancelDialog(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(Messages.OK);
                        action.actionPerformed(anActionEvent);
                        Thread.sleep(1000);
                        verify(kn, times(1)).deployFunc("namespace", "path", "test", "");
                    }
                }
            }
        }
    }

    @Test
    public void ActionPerformed_SelectedHasLocalPathAndHasFuncFileWithoutRegistryAndImageSpecified_UIAskForImage() throws IOException, InterruptedException {
        AnAction action = new DeployAction();
        AnActionEvent anActionEvent = createDeployActionEvent();

        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try (MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
                try (MockedStatic<YAMLHelper> yamlHelperMockedStatic = mockStatic(YAMLHelper.class)) {
                    try(MockedConstruction<Messages.InputDialog> inputDialogMockedConstruction = mockConstruction(Messages.InputDialog.class,
                            (mock, context) -> {
                                when(mock.isOK()).thenReturn(true);
                                when(mock.getInputString()).thenReturn("image");
                            })) {
                        try(MockedStatic<Messages> messagesMockedStatic = mockStatic(Messages.class)) {

                            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
                            pathsMockedStatic.when(() -> Paths.get(anyString(), anyString())).thenReturn(pathFuncFile);
                            when(kn.getFuncFileURL(any())).thenReturn(mock(URL.class));
                            yamlHelperMockedStatic.when(() -> YAMLHelper.URLToJSON(any())).thenReturn(jsonNode);
                            yamlHelperMockedStatic.when(() -> YAMLHelper.JSONToYAML(any())).thenReturn("");
                            yamlHelperMockedStatic.when(() -> YAMLHelper.getStringValueFromYAML(anyString(), any(String[].class))).thenReturn("").thenReturn("");
                            messagesMockedStatic.when(() -> Messages.showOkCancelDialog(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(Messages.OK);
                            action.actionPerformed(anActionEvent);
                            Thread.sleep(1000);
                            verify(kn, times(1)).deployFunc("namespace", "path", "", "image");
                        }
                    }
                }
            }
        }
    }

    @Test
    public void ActionPerformed_SelectedHasLocalPathAndHasNoFuncFile_UIAskForImage() throws IOException, InterruptedException {
        AnAction action = new DeployAction();
        AnActionEvent anActionEvent = createDeployActionEvent();

        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try (MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
                try(MockedConstruction<Messages.InputDialog> inputDialogMockedConstruction = mockConstruction(Messages.InputDialog.class,
                        (mock, context) -> {
                            when(mock.isOK()).thenReturn(true);
                            when(mock.getInputString()).thenReturn("image");
                        })) {
                    try(MockedStatic<Messages> messagesMockedStatic = mockStatic(Messages.class)) {
                        treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
                        pathsMockedStatic.when(() -> Paths.get(anyString(), anyString())).thenReturn(pathFuncFile);
                        messagesMockedStatic.when(() -> Messages.showOkCancelDialog(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(Messages.OK);
                        action.actionPerformed(anActionEvent);
                        Thread.sleep(1000);
                        verify(kn, times(1)).deployFunc("namespace", "path", null, "image");
                    }
                }
            }

        }
    }

    @Test
    public void ActionPerformed_SelectedHasLocalPathAndFailOpeningFuncFile_UIAskForImage() throws IOException, InterruptedException {
        AnAction action = new DeployAction();
        AnActionEvent anActionEvent = createDeployActionEvent();

        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try (MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
                try (MockedStatic<YAMLHelper> yamlHelperMockedStatic = mockStatic(YAMLHelper.class)) {
                    try(MockedConstruction<Messages.InputDialog> inputDialogMockedConstruction = mockConstruction(Messages.InputDialog.class,
                            (mock, context) -> {
                                when(mock.isOK()).thenReturn(true);
                                when(mock.getInputString()).thenReturn("image");
                            })) {
                        try(MockedStatic<Messages> messagesMockedStatic = mockStatic(Messages.class)) {
                            treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
                            pathsMockedStatic.when(() -> Paths.get(anyString(), anyString())).thenReturn(pathFuncFile);
                            yamlHelperMockedStatic.when(() -> YAMLHelper.URLToJSON(any())).thenThrow(new IOException("error"));
                            messagesMockedStatic.when(() -> Messages.showOkCancelDialog(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(Messages.OK);
                            action.actionPerformed(anActionEvent);
                            Thread.sleep(1000);
                            verify(kn, times(0)).deployFunc("namespace", "path", "", "image");
                        }
                    }
                }
            }
        }
    }

    @Test
    public void ActionPerformed_SelectedHasLocalPathAndHasFuncFileWithoutRegistryAndImageSpecifiedAndImageInsertedByUserIsEmptyString_doNothing() throws IOException, InterruptedException {
        AnAction action = new DeployAction();
        AnActionEvent anActionEvent = createDeployActionEvent();

        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try (MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
                try (MockedStatic<YAMLHelper> yamlHelperMockedStatic = mockStatic(YAMLHelper.class)) {
                    try(MockedConstruction<Messages.InputDialog> inputDialogMockedConstruction = mockConstruction(Messages.InputDialog.class,
                            (mock, context) -> {
                                when(mock.isOK()).thenReturn(true);
                                when(mock.getInputString()).thenReturn("");
                            })) {

                        treeHelperMockedStatic.when(() -> TreeHelper.getKn(any())).thenReturn(kn);
                        pathsMockedStatic.when(() -> Paths.get(anyString(), anyString())).thenReturn(pathFuncFile);
                        yamlHelperMockedStatic.when(() -> YAMLHelper.URLToJSON(any())).thenReturn(jsonNode);
                        yamlHelperMockedStatic.when(() -> YAMLHelper.JSONToYAML(any())).thenReturn("");
                        yamlHelperMockedStatic.when(() -> YAMLHelper.getStringValueFromYAML(anyString(), any(String[].class))).thenReturn("").thenReturn("");
                        action.actionPerformed(anActionEvent);
                        Thread.sleep(1000);
                        verify(kn, times(0)).deployFunc("namespace","path", "", "image");
                    }
                }
            }
        }
    }

    private AnActionEvent createDeployActionEvent() throws IOException {
        AnActionEvent anActionEvent = mock(AnActionEvent.class);
        when(anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT)).thenReturn(tree);
        when(tree.getClientProperty(Constants.STRUCTURE_PROPERTY)).thenReturn(knFunctionsTreeStructure);
        when(knFunctionNode.getRootNode()).thenReturn(knRootNode);
        when(knFunctionNode.getName()).thenReturn("name");
        when(knRootNode.getKn()).thenReturn(kn);
        when(kn.getNamespace()).thenReturn("namespace");
        when(anActionEvent.getProject()).thenReturn(project);
        when(model.getSelectionPath()).thenReturn(path3);
        when(model.getSelectionPaths()).thenReturn(new TreePath[] {path3});
        when(knFunctionNode.getFunction()).thenReturn(function);

        when(function.getLocalPath()).thenReturn("path");
        pathFuncFile = mock(Path.class);
        File funcFile = mock(File.class);
        URI uriFuncFile = mock(URI.class);
        URL urlFuncFile = mock(URL.class);
        jsonNode = mock(JsonNode.class);
        when(uriFuncFile.toURL()).thenReturn(urlFuncFile);
        when(pathFuncFile.toFile()).thenReturn(funcFile);
        when(funcFile.exists()).thenReturn(true);
        when(funcFile.toURI()).thenReturn(uriFuncFile);

        return anActionEvent;
    }
}
