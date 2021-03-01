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
package com.redhat.devtools.intellij.knative.utils;

import com.redhat.devtools.intellij.knative.kn.Kn;
import com.redhat.devtools.intellij.knative.tree.KnServiceNode;
import com.redhat.devtools.intellij.knative.tree.ParentableNode;
import java.io.IOException;

public class KnHelper {

    public static String getYamlFromNode(ParentableNode node) throws IOException {
        Kn knCli = node.getRootNode().getKn();
        String content = "";
        if (node instanceof KnServiceNode) {
            content = knCli.getServiceYAML(node.getName());
        }
        return content;
    }
}
