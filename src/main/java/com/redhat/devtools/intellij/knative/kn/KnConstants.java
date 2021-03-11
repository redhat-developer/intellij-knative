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

public interface KnConstants {
    /**
     * Label
     */
    String CONFIGURATION_GENERATION = "serving.knative.dev/configurationGeneration";

    /**
     * Label
     */
    String CONFIGURATION = "serving.knative.dev/configuration";

    /**
     * Label
     */
    String ROUTING_STATE = "serving.knative.dev/routingState";

    /**
     * Label
     */
    String SERVICE = "serving.knative.dev/service";

    /**
     * Annotation
     */
    String USER_IMAGE = "client.knative.dev/user-image";

    /**
     * Annotation
     */
    String CREATOR = "serving.knative.dev/creator";

    /**
     * Annotation
     */
    String ROUTES = "serving.knative.dev/routes";

    /**
     * Annotation
     */
    String ROUTING_STATE_MODIFIED = "serving.knative.dev/routingStateModified";
}
