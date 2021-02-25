/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.kn;

import org.jetbrains.annotations.Nullable;

public class StatusCondition {
    private final String lastTransitionTime;
    private final String status;
    private final String type;
    @Nullable
    private final String reason;
    @Nullable
    private final String message;

    public StatusCondition(String lastTransitionTime, String status, String type) {
        this(lastTransitionTime, status, type, null, null);
    }

    public StatusCondition(String lastTransitionTime, String status, String type, @Nullable String reason, @Nullable String message) {
        this.lastTransitionTime = lastTransitionTime;
        this.status = status;
        this.type = type;
        this.reason = reason;
        this.message = message;
    }

    public String getLastTransitionTime() {
        return lastTransitionTime;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    @Nullable
    public String getMessage() {
        return message;
    }
}
