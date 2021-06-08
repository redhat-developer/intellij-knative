/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.knative;

import com.intellij.openapi.util.Key;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;

public class Constants {
    public static final String STRUCTURE_PROPERTY = Constants.class.getPackage().getName() + ".structure";
    public static final String TOOLBAR_PLACE = Constants.class.getPackage().getName() + ".view.toolbar";
    public static final Key<String> KNATIVE = Key.create("com.redhat.devtools.intellij.knative");

    public static final String NOTIFICATION_ID = "Knative";

    public static final String[] YAML_API_VERSION_PATH = new String[] { "apiVersion" };
    public static final String[] YAML_KIND_PATH = new String[] { "kind" };
    public static final String[] YAML_NAME_PATH = new String[] { "metadata", "name" };
    public static final String[] YAML_FIRST_IMAGE_PATH = new String[] { "spec", "template", "spec", "containers[0]", "image" };
    public static final String[] YAML_PING_SOURCE_SCHEDULE = new String[] { "spec", "schedule" };
    public static final String[] YAML_PING_SOURCE_CONTENT_TYPE = new String[] { "spec", "contentType" };
    public static final String[] YAML_PING_SOURCE_DATA = new String[] { "spec", "data" };
    public static final String[] YAML_API_SOURCE_SERVICE_ACCOUNT = new String[] { "spec", "serviceAccountName" };
    public static final String[] YAML_SOURCE_SINK = new String[] { "spec", "sink", "ref", "name" };

    public static final String API_SOURCE = "ApiSource";
    public static final String PING_SOURCE = "PingSource";
    public static final String CUSTOM_SOURCE = "CustomSource";

    public static final String MINUTES = "minutes";
    public static final String HOURS = "hours";
    public static final String DAYS = "days";
}
