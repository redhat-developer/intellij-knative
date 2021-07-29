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

public class Constants {
    public static final String STRUCTURE_PROPERTY = Constants.class.getPackage().getName() + ".structure";
    public static final String TOOLBAR_PLACE = Constants.class.getPackage().getName() + ".view.toolbar";
    public static final Key<String> KNATIVE = Key.create("com.redhat.devtools.intellij.knative");

    public static final String NOTIFICATION_ID = "Knative";

    public static final String KNATIVE_TOOL_WINDOW_ID = "Knative";
    public static final String KNATIVE_FUNC_TOOL_WINDOW_ID = "KnativeFunction";

    public static final String[] YAML_NAME_PATH = new String[] { "metadata", "name" };
    public static final String[] YAML_FIRST_IMAGE_PATH = new String[] { "spec", "template", "spec", "containers[0]", "image" };
}
