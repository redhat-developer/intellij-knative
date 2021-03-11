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

public final class KnConstants {
    /**
     * Label
     */
    public static final String CONFIGURATION_GENERATION = "serving.knative.dev/configurationGeneration";
    /**
     * Label
     */
    public static final String CONFIGURATION = "serving.knative.dev/configuration";
    /**
     * Label
     */
    public static final String ROUTING_STATE = "serving.knative.dev/routingState";
    /**
     * Label
     */
    public static final String SERVICE = "serving.knative.dev/service";
    /**
     * Annotation
     */
    public static final String USER_IMAGE = "client.knative.dev/user-image";
    /**
     * Annotation
     */
    public static final String CREATOR = "serving.knative.dev/creator";
    /**
     * Annotation
     */
    public static final String ROUTES = "serving.knative.dev/routes";
    /**
     * Annotation
     */
    public static final String ROUTING_STATE_MODIFIED = "serving.knative.dev/routingStateModified";

    private KnConstants() {
    }
}
