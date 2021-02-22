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

public class ServiceCondition {
    private String lastTransitionTime;
    private String status;
    private String type;

    public ServiceCondition(String lastTransitionTime, String status, String type) {
        this.lastTransitionTime = lastTransitionTime;
        this.status = status;
        this.type = type;
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
}
