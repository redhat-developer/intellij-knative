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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YAMLUtils {
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    public static String getSnippet(String kind) throws IOException {
        URL snippet = EditorHelper.class.getResource("/snippets/" + kind + ".yaml");
        if (snippet == null) {
            return "";
        }
        return YAMLHelper.JSONToYAML(YAML_MAPPER.readTree(snippet));
    }

    public static JsonNode editValueInYAML(String yamlAsString, String[] fieldnames, String value) throws IOException {
        if (yamlAsString == null) {
            return null;
        } else {
            JsonNode node = YAML_MAPPER.readTree(yamlAsString);
            JsonNode tmp = node;
            Pattern arrayPattern = Pattern.compile("(\\w+)(\\[(\\d)\\])*");

            for(int i = 0; i < fieldnames.length; ++i) {
                String fieldname = fieldnames[i];
                int index = -1;
                Matcher match = arrayPattern.matcher(fieldname);
                if (match.matches() && match.group(3) != null) {
                    fieldname = match.group(1);
                    index = Integer.parseInt(match.group(3));
                }
                if (!node.has(fieldname) ||
                        (index != -1 && !node.get(fieldname).has(index))) {
                    return null;
                }

                if (i == fieldnames.length - 1) {
                    ((ObjectNode) node).put(fieldname, value);
                } else {
                    node = node.get(fieldname);
                    if (index != -1) {
                        node = node.get(index);
                    }
                }
            }

            return tmp;
        }
    }
}
