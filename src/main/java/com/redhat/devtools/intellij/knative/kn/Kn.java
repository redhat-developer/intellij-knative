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

import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.model.ProcessHandlerInput;
import com.redhat.devtools.intellij.common.utils.CommonTerminalExecutionConsole;
import com.redhat.devtools.intellij.common.utils.ExecProcessHandler;
import com.redhat.devtools.intellij.knative.ui.createFunc.CreateFuncModel;
import com.redhat.devtools.intellij.knative.utils.model.InvokeModel;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
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
     * Return the list of all Knative Functions in current namespace
     *
     * @return list of functions
     * @throws IOException if communication encountered an error
     */
    List<Function> getFunctions() throws IOException;

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
     * Delete/Undeploy a list of functions
     *
     * @param functions the list of functions to delete/undeploy
     * @throws IOException if communication errored
     */
    void deleteFunctions(List<String> functions) throws IOException;

    /**
     * Get a custom resource from the cluster which is namespaced.
     *
     * @param name       name of custom resource
     * @param crdContext the custom resource definition context of the resource kind
     * @return Object as HashMap, null if no resource was found
     * @throws IOException if communication errored
     */
    Map<String, Object> getCustomResource(String name, CustomResourceDefinitionContext crdContext);

    /**
     * Edit a custom resource object which is a namespaced object
     *
     * @param name           name of custom resource
     * @param crdContext     the custom resource definition context of the resource kind
     * @param objectAsString new object as a JSON string
     * @throws IOException if communication errored
     */
    void editCustomResource(String name, CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException;

    /**
     * Create a custom resource which is a namespaced object.
     *
     * @param crdContext     the custom resource definition context of the resource kind
     * @param objectAsString new object as a JSON string
     * @throws IOException if communication errored
     */
    void createCustomResource(CustomResourceDefinitionContext crdContext, String objectAsString) throws IOException;

    /**
     * Return the list of all Knative Event Sources
     *
     * @return list of sources
     * @throws IOException if communication errored
     */
    List<Source> getSources() throws IOException;

    /**
     * Tag a service revision
     *
     * @param service  the service which the revision belongs to
     * @param revision the revision to tag
     * @param tag      the tag name
     * @throws IOException if communication errored
     */
    void tagRevision(String service, String revision, String tag) throws IOException;

    /**
     * Return the func.yaml file
     *
     * @param root the path where to look for the func.yaml file
     * @return the func.yaml file
     * @throws IOException if func.yaml doesn't exist
     */
    File getFuncFile(Path root) throws IOException;

    /**
     * Return the func.yaml file url
     *
     * @param root the path where to look for the func.yaml file
     * @return the func.yaml file url
     * @throws IOException if func.yaml doesn't exist
     */
    URL getFuncFileURL(Path root) throws IOException;

    /**
     * Create a new function
     *
     * @param model model representing the function to be created
     * @throws IOException if communication errored
     */
    void createFunc(CreateFuncModel model) throws IOException;

    /**
     * Build a function from path
     *
     * @param path     path where the source code is stored
     * @param registry registry to use
     * @param image    image name. This option takes precedence over registry which can be omitted
     * @param terminalExecutionConsole terminal tab to be used to run the command. If null a new tab will be created
     * @throws IOException if communication errored
     */
    void buildFunc(String path, String registry, String image, ConsoleView terminalExecutionConsole,
                   java.util.function.Function<ProcessHandlerInput, ExecProcessHandler> processHandlerFunction,
                   ProcessListener processListener) throws IOException;

    /**
     * Deploy a function from path
     *
     * @param namespace namespace where to deploy
     * @param path      path where the source code is stored
     * @param registry  registry to use
     * @param image     image name. This option takes precedence over registry which can be omitted
     * @throws IOException if communication errored
     */
    void deployFunc(String namespace, String path, String registry, String image) throws IOException;

    /**
     * Invokes the Function by sending a test request to the currently running
     * Function instance, either locally or remote
     *
     * @param model model representing the function to be invoked
     * @return id generated for invoke call made
     * @throws IOException if communication errored
     */
    String invokeFunc(InvokeModel model) throws IOException;

    /**
     * Run a function locally
     *
     * @param path path where the function is stored
     * @param terminalExecutionConsole terminal tab to be used to run the command. If null a new tab will be created
     * @param processListener
     * @throws IOException if communication errored
     */
    void runFunc(String path, ConsoleView terminalExecutionConsole,
                 java.util.function.Function<ProcessHandlerInput, ExecProcessHandler> processHandlerFunction,
                 ProcessListener processListener) throws IOException;

    /**
     * Add environment variable to the function configuration
     *
     * @param path path where the function is stored
     * @throws IOException if communication errored
     */
    void addEnv(String path) throws IOException;

    /**
     * Remove environment variable from function configuration
     *
     * @param path path where the function is stored
     * @throws IOException if communication errored
     */
    void removeEnv(String path) throws IOException;

    /**
     * Add volume to the function configuration
     *
     * @param path path where the function is stored
     * @throws IOException if communication errored
     */
    void addVolume(String path) throws IOException;

    /**
     * Remove volume from the function configuration
     *
     * @param path path where the function is stored
     * @throws IOException if communication errored
     */
    void removeVolume(String path) throws IOException;

    /**
     * Set a watch on Service resource with label
     *
     * @param key     key label
     * @param value   value label
     * @param watcher the watcher to call when a new event is received
     * @return the watch object
     * @throws IOException if communication errored
     */
    Watch watchServiceWithLabel(String key, String value, Watcher<io.fabric8.knative.serving.v1.Service> watcher) throws IOException;

    /**
     * Create a terminal console to be used to run multiple commands in same tab
     *
     * @return a terminal console
     */
    CommonTerminalExecutionConsole createTerminalTabToReuse();
}
