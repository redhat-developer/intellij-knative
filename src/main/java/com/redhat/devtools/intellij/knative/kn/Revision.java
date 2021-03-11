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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@JsonDeserialize(using = RevisionDeserializer.class)
public class Revision {
    private String name;

    private List<StatusCondition> conditions;

    @NotNull
    private Map<String, String> annotations;

    @NotNull
    private Map<String, String> labels;

    public Revision(String name, List<StatusCondition> conditions, @NotNull Map<String, String> annotations, @NotNull Map<String, String> labels) {
        this.name = name;
        this.conditions = conditions;
        this.annotations = annotations;
        this.labels = labels;
    }

    public String getName() {
        return name;
    }

    public List<StatusCondition> getConditions() {
        return conditions;
    }

    /**
     * Annotations map, use {@link KnConstants} to get keys
     *
     * @return annotations map
     */
    @NotNull
    public Map<String, String> getAnnotations() {
        return annotations;
    }

    /**
     * Labels map, use {@link KnConstants} to get keys
     *
     * @return labels map
     */
    @NotNull
    public Map<String, String> getLabels() {
        return labels;
    }
}
