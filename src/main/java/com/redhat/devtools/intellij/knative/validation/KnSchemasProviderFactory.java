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
package com.redhat.devtools.intellij.knative.validation;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KnSchemasProviderFactory implements JsonSchemaProviderFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(KnSchemasProviderFactory.class);

    private List<KnSchemaProvider> providers = new ArrayList<>();

    public KnSchemasProviderFactory() {
        load();
    }

    private void load() {
        try (InputStream list = KnSchemasProviderFactory.class.getResourceAsStream("/schemas/index.properties")) {
            if (list != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(list, StandardCharsets.UTF_8))) {
                    reader.lines().filter(line -> StringUtils.isNotBlank(line)).forEach(line -> {
                        loadSchema(line);
                    });
                }
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    private void loadSchema(String schema) {
        URL url = KnSchemasProviderFactory.class.getResource("/schemas/" + schema);
        if (url != null) {
            KubernetesTypeInfo info = KubernetesTypeInfo.fromFileName(schema);
            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(VfsUtil.convertFromUrl(url));
            providers.add(new KnSchemaProvider(info, file));
        }
    }

    @NotNull
    @Override
    public List<JsonSchemaFileProvider> getProviders(@NotNull Project project) {
        return providers.stream().map(provider -> provider.withProject(project)).collect(Collectors.toList());
    }
}
