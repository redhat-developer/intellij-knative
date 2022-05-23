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

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.icons.AllIcons;
import com.intellij.terminal.TerminalExecutionConsole;
import com.intellij.ui.AnimatedIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActionFuncStepHandler {
    private final ActionFuncHandler actionFuncHandler;
    private final String actionName;
    private TerminalExecutionConsole terminalExecutionConsole;
    private ProcessListener processListener;
    private long startTime;
    private long endTime;
    private Icon[] stateIcon;
    private String[] state;
    private final int stepIndex;

    public ActionFuncStepHandler(ActionFuncHandler actionFuncHandler, String actionName, int stepIndex){
        this.actionFuncHandler = actionFuncHandler;
        this.actionName = actionName;
        this.stepIndex = stepIndex;
        init();
    }

    private void init() {
        ActionFuncStepHandler that = this;
        stateIcon = new Icon[]{new AnimatedIcon.FS()};
        state = new String[]{"running ..."};
        TerminalExecutionConsole commonTerminalExecutionConsole = new TerminalExecutionConsole(actionFuncHandler.getProject(), null);
        ProcessListener processListener = new ProcessAdapter() {
            @Override
            public void startNotified(@NotNull ProcessEvent event) {
                startTime = System.currentTimeMillis();
                actionFuncHandler.fireChangeRunningStep(that);
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                if (event.getExitCode() == 0) {
                    stateIcon[0] = AllIcons.RunConfigurations.TestPassed;
                    state[0] =  "successful";
                } else {
                    stateIcon[0] = AllIcons.General.BalloonError;
                    state[0] = "failed";
                }
                actionFuncHandler.fireTerminatedStep(that);
                setEndTime();
            }
        };
        setProcessListener(processListener);
        setTerminalExecutionConsole(commonTerminalExecutionConsole);
    }

    public String getActionName() {
        return actionName;
    }

    public ActionFuncHandler getActionFuncHandler() {
        return actionFuncHandler;
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

    public int getStepIndex() {
        return stepIndex;
    }
}
