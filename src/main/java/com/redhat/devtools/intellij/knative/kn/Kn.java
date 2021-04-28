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

import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

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
     * @return list of services
     * @throws IOException if communication encountered an error
     */
    List<Service> getServicesList() throws IOException;

    /**
     * Return the list of all Knative Revisions for service
     *
     * @param serviceName the Knative service name
     * @return list of revision belonging to service
     * @throws IOException if communication encountered an error
     */
    List<Revision> getRevisionsForService(String serviceName) throws IOException;

    /**
     * Return the service component
     *
     * @param name name of service
     * @return service component
     * @throws IOException if communication encountered an error
     */
    Service getService(String name) throws IOException;

    /**
     * Get the Service component as YAML
     *
     * @param name name of service
     * @return service component as YAML
     * @throws IOException if communication encountered an error
     */
    String getServiceYAML(String name) throws IOException;

    /**
     * Get the Revision component as YAML
     *
     * @param name name of revision
     * @return revision component as YAML
     * @throws IOException if communication encountered an error
     */
    String getRevisionYAML(String name) throws IOException;

    /**
     * Delete a list of services
     *
     * @param services the list of services to delete
     * @throws IOException if communication errored
     */
    void deleteServices(List<String> services) throws IOException;

    /**
     * Delete a list of revisions
     *
     * @param revisions the list of revisions to delete
     * @throws IOException if communication errored
     */
    void deleteRevisions(List<String> revisions) throws IOException;

    /**
     * Get a custom resource from the cluster which is namespaced.
     *
     * @param name name of custom resource
     * @param crdContext the custom resource definition context of the resource kind
     * @return Object as HashMap, null if no resource was found
     * @throws IOException if communication errored
     */
    Map<String, Object> getCustomResource(String name, CustomResourceDefinitionContext crdContext);

    /**
     * Edit a custom resource object which is a namespaced object
     *
     * @param name name of custom resource
     * @param crdContext the custom resource definition context of the resource kind
     * @param objectAsString new object as a JSON string
     * @throws IOException if communication errored
     */
    void editCustomResource(String name, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException;

    /**
     * Create a custom resource which is a namespaced object.
     *
     * @param crdContext the custom resource definition context of the resource kind
     * @param objectAsString new object as a JSON string
     * @throws IOException if communication errored
     */
    void createCustomResource(CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException;

    /**
     * Tag a service revision
     *
     * @param service the service which the revision belongs to
     * @param revision the revision to tag
     * @param tag the tag name
     * @throws IOException if communication errored
     */
    void tagRevision(String service, String revision, String tag) throws IOException;
}
