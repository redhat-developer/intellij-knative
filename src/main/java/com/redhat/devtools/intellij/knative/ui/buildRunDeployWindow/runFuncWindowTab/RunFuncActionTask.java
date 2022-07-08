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
package com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.runFuncWindowTab;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.Key;
import com.intellij.ui.AnimatedIcon;
import com.intellij.util.Consumer;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.FuncActionPipeline;
import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.FuncActionTask;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.function.Supplier;

public class RunFuncActionTask extends FuncActionTask {
    private Runnable callbackWhenListeningReady;

    public RunFuncActionTask(FuncActionPipeline actionFuncHandler, Consumer<FuncActionTask> doExecute, int stepIndex) {
        super(actionFuncHandler, "runFunc", doExecute, stepIndex);
    }

    public void setCallbackWhenListeningReady(Runnable callbackWhenListeningReady) {
        this.callbackWhenListeningReady = callbackWhenListeningReady;
    }

    protected ProcessListener buildProcessListener() {
        Supplier<FuncActionTask> thisSupplier = () -> this;
        return new ProcessAdapter() {
            @Override
            public void startNotified(@NotNull ProcessEvent event) {
                startTime = System.currentTimeMillis();
                stateIcon = new Icon[]{new AnimatedIcon.FS()};
                state = new String[]{""};
                actionFuncHandler.fireChangeRunningStep();
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
                actionFuncHandler.fireTerminatedStep(thisSupplier);
                setEndTime();
            }

            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                if (event.getText().contains("Function started on port")) {
                    if (callbackWhenListeningReady != null) {
                        callbackWhenListeningReady.run();
                    }
                }
            }
        };
    }
}
