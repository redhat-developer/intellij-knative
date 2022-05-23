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
    public static final Key<String> RUNTIME_FUNCTION_KEY = Key.create(Constants.class.getPackage().getName() + ".runtime");
    public static final Key<String> TEMPLATE_FUNCTION_KEY = Key.create(Constants.class.getPackage().getName() + ".template");

    public static final String NOTIFICATION_ID = "Knative";

    public static final String KNATIVE_TOOL_WINDOW_ID = "Knative";
    public static final String KNATIVE_FUNC_TOOL_WINDOW_ID = "KnativeFunction";
    public static final String KNATIVE_LOCAL_FUNC_TOOL_WINDOW_ID = "KnativeLocalFunction";
    public static final String BUILDFUNC_TOOLWINDOW_ID = "BuildFunc";

    public static final String BUILDFUNC_CONTENT_NAME = "Build Output";
    public static final String RUNFUNC_CONTENT_NAME = "Run Output";

    public static final String[] YAML_NAME_PATH = new String[] { "metadata", "name" };
    public static final String[] YAML_FIRST_IMAGE_PATH = new String[] { "spec", "template", "spec", "containers[0]", "image" };

    public static final String GO_MODULE_TYPE_ID = "GO_MODULE";
    public static final String PYTHON_MODULE_TYPE_ID = "PYTHON_MODULE";
    public static final String RUST_MODULE_TYPE_ID = "RUST_MODULE";

    public static final String NODE_RUNTIME = "node";
    public static final String GO_RUNTIME = "go";
    public static final String PYTHON_RUNTIME = "python";
    public static final String QUARKUS_RUNTIME = "quarkus";
    public static final String RUST_RUNTIME = "rust";
    public static final String SPRINGBOOT_RUNTIME = "springboot";
    public static final String TYPESCRIPT_RUNTIME = "typescript";

    public static final String LOCAL_FUNCTIONS_ACTION_GROUP_ID = "com.redhat.devtools.intellij.knative.tree.localfunctions";
    public static final String LOCAL_FUNCTIONS_TOOLBAR_ACTION_GROUP_ID = "com.redhat.devtools.intellij.knative.view.actionsLocalFunctionToolbar";
    public static final String FUNCTIONS_ACTION_GROUP_ID = "com.redhat.devtools.intellij.knative.tree.functions";
    public static final String FUNCTIONS_TOOLBAR_ACTION_GROUP_ID = "com.redhat.devtools.intellij.knative.view.actionsFunctionToolbar";
    public static final String KNATIVE_ACTION_GROUP_ID = "com.redhat.devtools.intellij.knative.tree";
    public static final String KNATIVE_TOOLBAR_ACTION_GROUP_ID = "com.redhat.devtools.intellij.knative.view.actionsToolbar";

    public static final String FUNCTIONS = "functions";
    public static final String FUNCTON_LABEL_KEY = "boson.dev/function";


    public static final String KIND_FUNCTIONS = "functions";
    public static final String KIND_FUNCTION = "function";

}
