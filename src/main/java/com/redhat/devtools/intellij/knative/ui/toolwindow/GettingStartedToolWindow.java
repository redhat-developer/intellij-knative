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
package com.redhat.devtools.intellij.knative.ui.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.openapi.wm.impl.ToolWindowManagerImpl;
import com.intellij.ui.content.ContentManager;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedContent;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedCourse;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedCourseBuilder;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedGroupLessons;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedLesson;
import com.redhat.devtools.intellij.knative.settings.SettingsState;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class GettingStartedToolWindow implements ToolWindowFactory {

    private GettingStartedCourse course;
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setIcon(AllIcons.Toolwindows.Documentation);
        toolWindow.setStripeTitle("Getting Started");
        ContentManager manager = toolWindow.getContentManager();
        project.getMessageBus().connect(manager).subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
            @Override
            public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
                if (hasToShowToolWindow()) {
                    toolWindow.show();
                }
            }
        });
    }

    private boolean hasToShowToolWindow() {
        String version = course.getVersion();
        if (SettingsState.getInstance().courseVersion.equals(version)) {
            return false;
        }
        SettingsState.getInstance().courseVersion = version;
        return true;
    }

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        final String version = "1.0";
        course = new GettingStartedCourseBuilder()
                .createGettingStartedCourse(
                        version,
                        "Learn IDE Features for Knative",
                        "Start deploying, running, and managing serverless applications on OpenShift and Kubernetes",
                        getFeedbackURL())
                .withGroupLessons(buildKnativeFuncLessons())
                .build();
        GettingStartedContent content = new GettingStartedContent(toolWindow, "", course);
        toolWindow.getContentManager().addContent(content);
    }

    private URL getFeedbackURL() {
        URL feedbackUrl = null;
        try {
            feedbackUrl = new URL("https://github.com/redhat-developer/intellij-knative");
        } catch (MalformedURLException ignored) { }
        return feedbackUrl;
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    private GettingStartedGroupLessons buildKnativeFuncLessons() {
        URL gifNewFuncLesson = getLessonGif("create-new-func.gif");
        GettingStartedLesson createFuncLesson = new GettingStartedLesson(
                "Create new function",
                "<html><p>Users can create a new function by using the native IDEA's New Project wizard.</p>" +
                        "<p>Click on the Function section, select the runtime/template, and create it." +
                        "The new function is now visible in the Functions tree.</p></html>",
                Collections.emptyList(),
                gifNewFuncLesson
        );

        URL gifBuildFuncLesson = getLessonGif("build-func-lesson.gif");
        GettingStartedLesson buildFuncLesson = new GettingStartedLesson(
                "Build function",
                "<html><p>Building a function allows to create an image from the source code and be able to run and/or deploy it. " +
                        "The result will be a container image that is pushed to a registry.</p>" +
                        "<p>Only a function which has the source code opened in the IDE can be built. Right click on the function you want to build" +
                        "(look for the right node in the Functions tree), " +
                        "Open the context menu (right-click on the node) and click on \"Build\". " +
                        "The Build tool window will show up where you can see the logs or stop the build execution</p></html>",
                Collections.emptyList(),
                gifBuildFuncLesson
        );

        URL gifRunFuncLesson = getLessonGif("run-func-lesson.gif");
        GettingStartedLesson runFuncLesson = new GettingStartedLesson(
                "Run function",
                "<html><p>Run the function locally on the current directory opened in the IDE.</p>" +
                        "<p>Open the context menu (right-click on the node) and click on \"Run\". " +
                        "The Run tool window will show up where you can see the logs or stop the run execution</p></html>",
                Collections.emptyList(),
                gifRunFuncLesson
        );

        URL gifDeployFuncLesson = getLessonGif("deploy-func-lesson.gif");
        GettingStartedLesson deployFuncLesson = new GettingStartedLesson(
                "Deploy function",
                "<html><p>Deploys a function to the currently configured Knative-enabled cluster.</p>" +
                        "<p>Only a function which has the source code opened in the IDE can be pushed. " +
                        "Right click on the function you want to deploy (look for its node in the Functions tree), open the context menu (right-click on the node) " +
                        "and click on \"Deploy\"." +
                        "The Deploy tool window will show up where you can see the logs or stop the deploy execution</p></html>",
                Collections.emptyList(),
                gifDeployFuncLesson
        );

        GettingStartedGroupLessons groupLessons = new GettingStartedGroupLessons(
                "Knative Functions",
                "Create, build, run and deploy your serverless function without leaving your preferred IDE",
                createFuncLesson,
                buildFuncLesson,
                runFuncLesson,
                deployFuncLesson);
        return groupLessons;
    }

    private URL getLessonGif(String name) {
        return GettingStartedToolWindow.class.getResource("/gettingstarted/course-function/" + name);
    }
}
