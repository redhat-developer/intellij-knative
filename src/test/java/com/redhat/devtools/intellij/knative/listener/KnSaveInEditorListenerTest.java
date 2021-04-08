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
package com.redhat.devtools.intellij.knative.listener;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.knative.BaseTest;
import com.redhat.devtools.intellij.knative.tree.KnRootNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import com.redhat.devtools.intellij.knative.utils.KnHelper;
import com.redhat.devtools.intellij.knative.utils.TreeHelper;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


import static com.redhat.devtools.intellij.knative.Constants.KNATIVE;
import static com.redhat.devtools.intellij.knative.Constants.NOTIFICATION_ID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KnSaveInEditorListenerTest extends BaseTest {

    private CodeInsightTestFixture myFixture;
    private KnSaveInEditorListener knSaveInEditorListener;
    private static final String RESOURCE_PATH = "listener/knSaveInEditorListener/";
    private Project project;
    private ParentableNode parentableNode;
    private KnRootNode knRootNode;

    @Before
    public void before() throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = factory.createLightFixtureBuilder();
        IdeaProjectTestFixture fixture = fixtureBuilder.getFixture();
        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture);
        myFixture.setUp();
        knSaveInEditorListener = new KnSaveInEditorListener();
        project = mock(Project.class);
        parentableNode = mock(ParentableNode.class);
        knRootNode = mock(KnRootNode.class);

    }

    @After
    public void tearDown() throws Exception {
        myFixture.tearDown();
    }

    @Test
    public void Notify_ValidDocument_SendNotification() throws IOException {
        String yaml = load(RESOURCE_PATH + "service.yaml");
        Document document = createDocument(yaml);
        Application application = mock(Application.class);
        when(application.isUnitTestMode()).thenReturn(true);
        try(MockedStatic<YAMLHelper> yamlHelperMockedStatic = mockStatic(YAMLHelper.class)) {
            knSaveInEditorListener.notify(document);
            yamlHelperMockedStatic.verify(times(2), () -> YAMLHelper.getStringValueFromYAML(anyString(), any()));
        }
    }

    @Test
    public void Refresh_NodeIsNull_Nothing() {
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            knSaveInEditorListener.refresh(project, null);
            treeHelperMockedStatic.verify(times(0), () -> TreeHelper.refresh(any(), any()));
        }
    }

    @Test
    public void Refresh_NodeIsUnknownType_Nothing() {
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            knSaveInEditorListener.refresh(project, knRootNode);
            treeHelperMockedStatic.verify(times(0), () -> TreeHelper.refresh(any(), any()));
        }
    }

    @Test
    public void Refresh_NodeIsParentableNode_Refresh() {
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            knSaveInEditorListener.refresh(null, parentableNode);
            treeHelperMockedStatic.verify(times(1), () -> TreeHelper.refresh(any(), any()));
        }
    }

    @Test
    public void Save_SaveOnClusterIsFalse_False() throws IOException {
        String yaml = load(RESOURCE_PATH + "service.yaml");
        Document document = createDocument(yaml);
        try(MockedStatic<KnHelper> knHelperMockedStatic = mockStatic(KnHelper.class)) {
            knHelperMockedStatic.when(() -> KnHelper.saveOnCluster(any(), anyString(), anyBoolean())).thenReturn(false);
            boolean result = knSaveInEditorListener.save(document, project);
            //knHelperMockedStatic.verify(times(1), () -> KnHelper.saveOnCluster(eq(project), eq(yaml), any()));
            assertFalse(result);
        }
    }

    @Test
    public void Save_SaveOnClusterIsTrue_True() throws IOException {
        String yaml = load(RESOURCE_PATH + "service.yaml");
        Document document = createDocument(yaml);
        try(MockedStatic<KnHelper> knHelperMockedStatic = mockStatic(KnHelper.class)) {
            knHelperMockedStatic.when(() -> KnHelper.saveOnCluster(any(), anyString(), anyBoolean())).thenReturn(true);
            boolean result = knSaveInEditorListener.save(document, project);
            //knHelperMockedStatic.verify(times(1), () -> KnHelper.saveOnCluster(eq(project), eq(yaml), any()));
            assertTrue(result);
        }
    }

    @Test
    public void Save_SaveOnClusterThrows_False() throws IOException {
        String yaml = load(RESOURCE_PATH + "service.yaml");
        Document document = createDocument(yaml);
        try(MockedStatic<KnHelper> knHelperMockedStatic = mockStatic(KnHelper.class)) {
            knHelperMockedStatic.when(() -> KnHelper.saveOnCluster(any(), anyString(), anyBoolean())).thenThrow(new IOException("text"));
            //knHelperMockedStatic.when(() -> Notifications.Bus.notify(any()));
            boolean result = knSaveInEditorListener.save(document, project);
            //knHelperMockedStatic.verify(times(1), () -> KnHelper.saveOnCluster(eq(project), eq(yaml), any()));
            assertFalse(result);
        }
    }

    @Test
    public void IsFileToPush_VirtualFileIsNull_False() {
        assertFalse(knSaveInEditorListener.isFileToPush(project, null));
    }

    @Test
    public void IsFileToPush_VirtualFileWithMissingKnativeProperty_False() throws IOException {
        String yaml = load(RESOURCE_PATH + "service.yaml");
        VirtualFile virtualFile = new LightVirtualFile("test", yaml);
        assertFalse(knSaveInEditorListener.isFileToPush(project, virtualFile));

    }

    @Test
    public void IsFileToPush_VirtualFileWithInvalidKnativeProperty_False() throws IOException {
        String yaml = load(RESOURCE_PATH + "service.yaml");
        VirtualFile virtualFile = new LightVirtualFile("test", yaml);
        virtualFile.putUserData(KNATIVE, "fake");
        assertFalse(knSaveInEditorListener.isFileToPush(project, virtualFile));
    }

    @Test
    public void IsFileToPush_VirtualFileIsValid_ParentMethodCalled() throws IOException {
        String yaml = load(RESOURCE_PATH + "service.yaml");
        VirtualFile virtualFile = new LightVirtualFile("test", yaml);
        virtualFile.putUserData(KNATIVE, NOTIFICATION_ID);
        FileEditorManager fileEditorManager = mock(FileEditorManager.class);
        try(MockedStatic<FileEditorManager> fileEditorManagerMockedStatic = mockStatic(FileEditorManager.class)) {
            fileEditorManagerMockedStatic.when(() -> FileEditorManager.getInstance(any())).thenReturn(fileEditorManager);
            knSaveInEditorListener.isFileToPush(project, virtualFile);
            verify(fileEditorManager).getSelectedEditor();
        }
    }

    private Document createDocument(String text) {
        return new Document() {
            @Override
            public @NotNull String getText() {
                return text;
            }

            @Override
            public @NotNull CharSequence getImmutableCharSequence() {
                return null;
            }

            @Override
            public int getLineCount() {
                return 0;
            }

            @Override
            public int getLineNumber(int offset) {
                return 0;
            }

            @Override
            public int getLineStartOffset(int line) {
                return 0;
            }

            @Override
            public int getLineEndOffset(int line) {
                return 0;
            }

            @Override
            public void insertString(int offset, @NotNull CharSequence s) {

            }

            @Override
            public void deleteString(int startOffset, int endOffset) {

            }

            @Override
            public void replaceString(int startOffset, int endOffset, @NotNull CharSequence s) {

            }

            @Override
            public boolean isWritable() {
                return false;
            }

            @Override
            public long getModificationStamp() {
                return 0;
            }

            @Override
            public @NotNull RangeMarker createRangeMarker(int startOffset, int endOffset, boolean surviveOnExternalChange) {
                return null;
            }

            @Override
            public @NotNull RangeMarker createGuardedBlock(int startOffset, int endOffset) {
                return null;
            }

            @Override
            public void setText(@NotNull CharSequence text) {

            }

            @Override
            public <T> @Nullable T getUserData(@NotNull Key<T> key) {
                return null;
            }

            @Override
            public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

            }
        };
    }
}
