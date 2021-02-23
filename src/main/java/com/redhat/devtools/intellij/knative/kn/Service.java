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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jetbrains.annotations.Nullable;

@JsonDeserialize(using = ServiceDeserializer.class)
public class Service {

    private final String name;
    @Nullable
    private final ServiceStatus status;

    public Service(String name, @Nullable ServiceStatus status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public ServiceStatus getStatus() {
        return status;
    }
}
