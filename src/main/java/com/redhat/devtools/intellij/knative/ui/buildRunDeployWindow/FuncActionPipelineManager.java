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
package com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow;

import com.redhat.devtools.intellij.knative.ui.buildRunDeployWindow.runFuncWindowTab.RunFuncActionPipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FuncActionPipelineManager {

    private static FuncActionPipelineManager INSTANCE;
    private final Map<String, List<IFuncActionPipeline>> pipelines;

    private FuncActionPipelineManager(){
        pipelines = new HashMap<>();
    }

    public static FuncActionPipelineManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FuncActionPipelineManager();
        }
        return INSTANCE;
    }

    public boolean start(IFuncActionPipeline pipeline) {
        if (canBeStarted(pipeline)) {
            ((FuncActionPipeline)pipeline).start();
            return true;
        }
        return false;
    }

    public void stopAndRerun(IFuncActionPipeline pipeline) {
        stop(pipeline);
        start(pipeline);
    }

    private void stop(IFuncActionPipeline pipeline) {
        List<IFuncActionPipeline> pipelinesFunction = pipelines.getOrDefault(pipeline.getFuncName(), new ArrayList<>());
        Optional<IFuncActionPipeline> existingActionPipeline = pipelinesFunction.stream()
                .filter(funcActionPipeline -> funcActionPipeline.getActionName().equals(pipeline.getActionName()))
                .findFirst();
        if (existingActionPipeline.isPresent()) {
            pipelinesFunction.remove(existingActionPipeline.get());
            existingActionPipeline.get().stop();
            pipelines.put(pipeline.getFuncName(), pipelinesFunction);
        }
    }

    private boolean canBeStarted(IFuncActionPipeline pipeline) {
        List<IFuncActionPipeline> pipelinesFunction = pipelines.getOrDefault(pipeline.getFuncName(), new ArrayList<>());
        Optional<IFuncActionPipeline> existingActionPipeline = pipelinesFunction.stream()
                .filter(funcActionPipeline -> funcActionPipeline.getActionName().equals(pipeline.getActionName()))
                .findFirst();
        if (existingActionPipeline.isPresent()) {
            if (existingActionPipeline.get() instanceof RunFuncActionPipeline
                && !existingActionPipeline.get().isFinished()) {
                return false;
            }
            pipelinesFunction.remove(existingActionPipeline.get());
        }
        pipelinesFunction.add(pipeline);
        pipelines.put(pipeline.getFuncName(), pipelinesFunction);
        return true;
    }
}
