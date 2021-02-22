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

public class ServiceTraffic {
    private final String tag;
    private final String revisionName;
    private final String configurationName;
    private final String latestRevision;
    private final int percent;
    private final String url;

    public ServiceTraffic(String tag, String revisionName, String configurationName, String latestRevision, int percent, String url) {
        this.tag = tag;
        this.revisionName = revisionName;
        this.configurationName = configurationName;
        this.latestRevision = latestRevision;
        this.percent = percent;
        this.url = url;
    }

    public String getTag() {
        return tag;
    }

    public String getRevisionName() {
        return revisionName;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public String getLatestRevision() {
        return latestRevision;
    }

    public int getPercent() {
        return percent;
    }

    public String getUrl() {
        return url;
    }
}
