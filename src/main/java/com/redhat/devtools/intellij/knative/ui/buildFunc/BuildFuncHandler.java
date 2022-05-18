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
package com.redhat.devtools.intellij.knative.ui.buildFunc;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.terminal.TerminalExecutionConsole;
import com.intellij.ui.AnimatedIcon;
import com.redhat.devtools.intellij.knative.kn.Function;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BuildFuncHandler {

    private Project project;
    private Function function;
    private TerminalExecutionConsole terminalExecutionConsole;
    private ProcessListener processListener;
    private long startTime;
    private long endTime;
    private Icon[] stateIcon;
    private String[] state;

    public BuildFuncHandler(Project project, Function function){
        this.project = project;
        this.function = function;
        this.startTime = System.currentTimeMillis();
        this.endTime = -1;
        init(project);
    }

    private void init(Project project) {
        stateIcon = new Icon[]{new AnimatedIcon.FS()};
        state = new String[]{"running ..."};
        TerminalExecutionConsole commonTerminalExecutionConsole = new TerminalExecutionConsole(project, null);
        ProcessListener processListener = new ProcessAdapter() {
            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                if (event.getExitCode() == 0) {
                    stateIcon[0] = AllIcons.RunConfigurations.TestPassed;
                    state[0] =  "successful";
                } else {
                    stateIcon[0] = AllIcons.General.BalloonError;
                    state[0] = "failed";
                }
                setEndTime();
            }
        };
        setProcessListener(processListener);
        setTerminalExecutionConsole(commonTerminalExecutionConsole);
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

    public TerminalExecutionConsole getTerminalExecutionConsole() {
        return terminalExecutionConsole;
    }

    public void setTerminalExecutionConsole(TerminalExecutionConsole terminalExecutionConsole) {
        this.terminalExecutionConsole = terminalExecutionConsole;
    }

    public ProcessListener getProcessListener() {
        return processListener;
    }

    public void setProcessListener(ProcessListener processListener) {
        this.processListener = processListener;
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
        if (terminalExecutionConsole != null) {
            terminalExecutionConsole.dispose();
        }
    }
}
