/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.kn;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface Kn {
    /**
     * Check if the cluster is Knative serving aware.
     *
     * @return true if Knative Serving is installed on cluster false otherwise
     */
    boolean isKnativeServingAware() throws IOException;

    /**
     * Check if the cluster is Knative eventing aware.
     *
     * @return true if Knative Eventing is installed on cluster false otherwise
     */
    boolean isKnativeEventingAware() throws IOException;

    URL getMasterUrl();

    /**
     * Get current k8's namespace
     *
     * @return the namespace name
     */
    String getNamespace();

    /**
     * Fetch the Service data
     *
     * @return
     */
    List<Service> getServicesList() throws IOException;

    /**
     * Return the list of all Knative Revisions for service
     *
     * @param serviceName the Knative service name
     */
    List<Revision> getRevisionsForService(String serviceName) throws IOException;
}
