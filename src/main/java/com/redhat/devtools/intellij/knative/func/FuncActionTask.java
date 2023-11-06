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

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.terminal.TerminalExecutionConsole;
import com.intellij.ui.AnimatedIcon;
import com.redhat.devtools.intellij.common.model.ProcessHandlerInput;
import com.redhat.devtools.intellij.common.utils.ExecProcessHandler;
import com.redhat.devtools.intellij.knative.kn.Function;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FuncActionTask implements IFuncAction {
    protected FuncActionPipeline pipeline;
    private final String actionName;
    private TerminalExecutionConsole terminalExecutionConsole;
    private ExecProcessHandler runHandler;
    protected java.util.function.Function<ProcessHandlerInput, ExecProcessHandler> processHandlerFunction;
    private ProcessListener processListener;
    protected long startTime;
    protected long endTime;
    protected Icon[] stateIcon;
    protected String[] state;
    private final Consumer<FuncActionTask> doExecute;

    public FuncActionTask(String actionName, Consumer<FuncActionTask> doExecute) {
        this.actionName = actionName;
        this.startTime = -1;
        this.endTime = -1;
        this.doExecute = doExecute;
    }

    public void doExecute() {
        this.doExecute.accept(this);
    }

    public void init(FuncActionPipeline pipeline) {
        this.pipeline = pipeline;
        stateIcon = new Icon[]{ AllIcons.Actions.Profile };
        state = new String[]{"Waiting to start"};
        TerminalExecutionConsole commonTerminalExecutionConsole = new TerminalExecutionConsole(pipeline.getProject(), null);
        setProcessListener(buildProcessListener());
        setTerminalExecutionConsole(commonTerminalExecutionConsole);
        setProcessHandlerFunction();
    }

    protected ProcessListener buildProcessListener() {
        Supplier<FuncActionTask> thisSupplier = () -> this;
        return new ProcessAdapter() {
            @Override
            public void startNotified(@NotNull ProcessEvent event) {
                startTime = System.currentTimeMillis();
                stateIcon = new Icon[]{new AnimatedIcon.FS()};
                state = new String[]{""};
                FuncActionTask.this.pipeline.fireChangeRunningStep();
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
                FuncActionTask.this.pipeline.fireTerminatedStep(thisSupplier);
                setEndTime();
            }
        };
    }

    public void stop() {
        if (runHandler != null && !isFinished()) {
            runHandler.destroyProcess();
        }
    }

    public String getActionName() {
        return actionName;
    }

    public FuncActionPipeline getPipeline() {
        return pipeline;
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

    @Override
    public Project getProject() {
        return pipeline.getProject();
    }

    @Override
    public String getFuncName() {
        return pipeline.getFuncName();
    }

    public void setEndTime() {
        this.endTime = System.currentTimeMillis();
    }

    public Icon getStateIcon() {
        if (stateIcon == null) {
            return null;
        }
        return stateIcon[0];
    }

    public String getState() {
        if (state == null) {
            return null;
        }
        return state[0];
    }

    public void setStateIcon(Icon[] stateIcon) {
        this.stateIcon = stateIcon;
    }

    public void setState(String[] state) {
        this.state = state;
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

    @Override
    public Function getFunction() {
        return pipeline.getFunction();
    }

    private void setProcessHandlerFunction() {
        processHandlerFunction = processHandlerInput -> {
                                        runHandler = new ExecProcessHandler(processHandlerInput.getProcess(),
                                                processHandlerInput.getCommandLine(),
                                                processHandlerInput.getCharset());
                                        return runHandler;
                                    };
    }

    public ExecProcessHandler getRunHandler() {
        return runHandler;
    }

    public java.util.function.Function<ProcessHandlerInput, ExecProcessHandler> getProcessHandlerFunction() {
        return processHandlerFunction;
    }
}
