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

@JsonDeserialize(using = SourceDeserializer.class)
public class Source {

    private final String name;
    private final String kind;
    private final BaseSource sinkSource;

    public Source(String name, String kind, BaseSource sinkSource) {
        this.name = name;
        this.kind = kind;
        this.sinkSource = sinkSource;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public BaseSource getSinkSource() {
        return sinkSource;
    }
}
