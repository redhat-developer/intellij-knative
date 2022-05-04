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

import com.intellij.execution.process.ProcessListener;
import com.intellij.terminal.TerminalExecutionConsole;

public class BuildFuncHandler {

    private String funcName;
    private TerminalExecutionConsole terminalExecutionConsole;
    private ProcessListener processListener;
    private long startTime;
    private long endTime;

    public BuildFuncHandler(String funcName){
        this.funcName = funcName;
        this.startTime = System.currentTimeMillis();
        this.endTime = -1;
    }

    public String getFuncName() {
        return funcName;
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

    public void dispose() {
        if (terminalExecutionConsole != null) {
            terminalExecutionConsole.dispose();
        }
    }
}
