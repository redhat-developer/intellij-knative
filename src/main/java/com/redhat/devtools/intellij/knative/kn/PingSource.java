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

public class PingSource extends BaseSource {
    private final String schedule;
    private final String data;

    public PingSource(String name, String parent, String schedule, String data, String sink) {
        super(name, parent, sink);
        this.schedule = schedule;
        this.data = data;
    }

    public String getSchedule() {
        return schedule;
    }

    public String getData() {
        return data;
    }
}
