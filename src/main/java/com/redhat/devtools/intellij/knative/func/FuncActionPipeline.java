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
package com.redhat.devtools.intellij.knative.func;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.AnimatedIcon;
import com.redhat.devtools.intellij.knative.kn.Function;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.BuildRunDeployFuncPanel;

import javax.swing.Icon;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import static com.redhat.devtools.intellij.knative.Constants.BUILDFUNC_TOOLWINDOW_ID;

public abstract class FuncActionPipeline implements IFuncActionPipeline {

    protected final Project project;
    private final Function function;
    private final String actionName;
    private final long startTime;
    private long endTime;
    private final Icon[] stateIcon;
    private final String[] state;
    private List<FuncActionTask> actionTasks;
    private final List<ActionFuncHandlerListener> listenerList;
    private FuncActionTask runningStep;

    public FuncActionPipeline(String name, Project project, Function function){
        this.actionName = name;
        this.project = project;
        this.function = function;
        this.startTime = System.currentTimeMillis();
        this.endTime = -1;
        this.actionTasks = new ArrayList<>();
        this.stateIcon = new Icon[]{new AnimatedIcon.FS()};
        this.state = new String[]{"run tasks ..."};
        this.listenerList = new ArrayList<>();
    }

    public void start() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(BUILDFUNC_TOOLWINDOW_ID);
        BuildRunDeployFuncPanel panel = (BuildRunDeployFuncPanel) toolWindow.getContentManager().findContent(getTabName());
        panel.drawFuncAction(this);
        runningStep.doExecute();
    }

    protected abstract String getTabName();

    public void setTasks(List<FuncActionTask> tasks) {
        actionTasks = tasks;
        tasks.forEach(task -> task.init(this));
        runningStep = actionTasks.get(0);
    }

    public void removeTask(int index) {
        if (actionTasks.size() == 1) {
            return;
        }
        actionTasks.remove(index);
        runningStep = actionTasks.get(0);
    }

    public List<FuncActionTask> getSteps() {
        return actionTasks;
    }

    public String getActionName() {
        return actionName;
    }

    @Override
    public void stop() {
        runningStep.stop();
    }

    public Project getProject() {
        return project;
    }

    public String getFuncName() {
        return function.getName();
    }

    public Function getFunction() {
        return function;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime() {
        this.endTime = System.currentTimeMillis();
    }

    public Icon getStateIcon() {
        return stateIcon[0];
    }

    public String getState() {
        return state[0];
    }

    public String getStartingDate() {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date(startTime);
        return formatter.format(date);
    }

    public boolean isFinished() {
        return endTime != -1;
    }

    public boolean isSuccessfullyCompleted() {
        return state[0].equals("successful");
    }

    public void fireChangeRunningStep() {
        notifyListeners();
    }

    public void fireTerminatedStep(Supplier<FuncActionTask> stepHandlerSupplier) {
        // if last step is terminated or any step failed set end time and update icon
        FuncActionTask stepHandler = stepHandlerSupplier.get();
        if (actionTasks.get(actionTasks.size() - 1).equals(stepHandler)
                || !stepHandler.isSuccessfullyCompleted()) {
            stateIcon[0] = stepHandler.getStateIcon();
            state[0] = stepHandler.getState();
            setEndTime();
            skipNextSteps(getTaskIndex(stepHandler));
        } else {
            FuncActionTask nextStepHandler = actionTasks.get(getTaskIndex(stepHandler) + 1);
            runningStep = nextStepHandler;
            nextStepHandler.doExecute();
        }
    }

    private int getTaskIndex(FuncActionTask task) {
        int index = -1;
        for (FuncActionTask taskInPipeline: actionTasks) {
            ++index;
            if (taskInPipeline.equals(task)) {
                return index;
            }
        }
        return index;
    }

    private void skipNextSteps(int currentStep) {
        actionTasks.stream().skip(currentStep + 1).forEach(task -> {
            task.setState(new String[]{ "skipped" });
            task.setStateIcon(new Icon[] { AllIcons.RunConfigurations.TestSkipped });
        });
    }

    private void notifyListeners() {
        for (ActionFuncHandlerListener listener: listenerList) {
            listener.fireModified(this);
        }
    }

    public void addStepChangeListener(ActionFuncHandlerListener listener) {
        listenerList.add(listener);
    }

    public FuncActionTask getRunningStep() {
        return runningStep;
    }
}
