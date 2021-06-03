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

public abstract class BaseSource {
    private final String name;

    private final String parent;

    private final String sink;

    public BaseSource(String name, String parent, String sink) {
        this.name = name;
        this.parent = parent;
        this.sink = sink;
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public String getSink() {
        return sink;
    }
}
