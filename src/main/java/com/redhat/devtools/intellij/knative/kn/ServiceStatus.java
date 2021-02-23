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

import java.util.List;

public class ServiceStatus {

    private final String url;
    private final int observedGeneration;
    private final String latestReadyRevisionName;
    private final String latestCreatedRevisionName;
    private final String addressUrl;
    private final List<ServiceTraffic> traffic;
    private final List<ServiceCondition> conditions;

    public ServiceStatus(String url, int observedGeneration, String latestReadyRevisionName, String latestCreatedRevisionName, String addressUrl, List<ServiceTraffic> traffic, List<ServiceCondition> conditions) {
        this.url = url;
        this.observedGeneration = observedGeneration;
        this.latestReadyRevisionName = latestReadyRevisionName;
        this.latestCreatedRevisionName = latestCreatedRevisionName;
        this.addressUrl = addressUrl;
        this.traffic = traffic;
        this.conditions = conditions;
    }

    public String getUrl() {
        return url;
    }

    public int getObservedGeneration() {
        return observedGeneration;
    }

    public String getLatestReadyRevisionName() {
        return latestReadyRevisionName;
    }

    public String getLatestCreatedRevisionName() {
        return latestCreatedRevisionName;
    }

    public String getAddressUrl() {
        return addressUrl;
    }

    public List<ServiceTraffic> getTraffic() {
        return traffic;
    }

    public List<ServiceCondition> getConditions() {
        return conditions;
    }
}
