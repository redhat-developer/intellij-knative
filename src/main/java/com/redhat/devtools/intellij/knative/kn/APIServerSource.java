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

public class APIServerSource extends BaseSource {
    private final String resource;

    public APIServerSource(String name, String parent, String resource, String sink) {
        super(name, parent, sink);
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

}
