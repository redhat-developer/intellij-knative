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
package com.redhat.devtools.intellij.knative.ui.brdWindowTabs;

import com.intellij.openapi.project.Project;
import com.intellij.ui.AnimatedIcon;
import com.redhat.devtools.intellij.knative.kn.Function;

import javax.swing.Icon;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ActionFuncHandler {

    private final Project project;
    private final Function function;
    private final String actionName;
    private final long startTime;
    private long endTime;
    private final Icon[] stateIcon;
    private final String[] state;
    private final List<ActionFuncStepHandler> actionSteps;
    private final List<ActionFuncHandlerListener> listenerList;
    private ActionFuncStepHandler runningStep;

    public ActionFuncHandler(String name, Project project, Function function, List<String> steps){
        this.actionName = name;
        this.project = project;
        this.function = function;
        this.startTime = System.currentTimeMillis();
        this.actionSteps = new ArrayList<>();
        this.stateIcon = new Icon[]{new AnimatedIcon.FS()};
        this.state = new String[]{"running ..."};
        this.listenerList = new ArrayList<>();
        addSteps(steps);
    }

    public void addSteps(List<String> steps) {
        for (String step: steps) {
            actionSteps.add(actionSteps.size(), new ActionFuncStepHandler(this, step, actionSteps.size() + 1));
        }
        runningStep = actionSteps.get(0);
    }

    public ActionFuncStepHandler getStep(String name) {
        Optional<ActionFuncStepHandler> actionFuncStepHandlerOptional = actionSteps.stream()
                .filter(step -> step.getActionName().equals(name)).findFirst();
        return actionFuncStepHandlerOptional.orElse(null);
    }

    public List<ActionFuncStepHandler> getSteps() {
        return actionSteps;
    }

    public ActionFuncStepHandler getFirstStep() {
        return actionSteps.get(0);
    }

    public String getActionName() {
        return actionName;
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

    public void dispose() {

    }

    public void fireChangeRunningStep(ActionFuncStepHandler stepHandler) {
        runningStep = stepHandler;
        notifyListeners();
    }

    public void fireTerminatedStep(ActionFuncStepHandler stepHandler) {
        // if last step is terminated or any step failed set end time and update icon
        if (actionSteps.size() == stepHandler.getStepIndex()
            || !stepHandler.isSuccessfullyCompleted()) {
            stateIcon[0] = stepHandler.getStateIcon();
            state[0] = stepHandler.getState();
            setEndTime();
            //notifyListeners();
        }
    }

    private void notifyListeners() {
        for (ActionFuncHandlerListener listener: listenerList) {
            listener.fireModified(this);
        }
    }

    public void addStepChangeListener(ActionFuncHandlerListener listener) {
        listenerList.add(listener);
    }

    public ActionFuncStepHandler getRunningStep() {
        return runningStep;
    }
}
