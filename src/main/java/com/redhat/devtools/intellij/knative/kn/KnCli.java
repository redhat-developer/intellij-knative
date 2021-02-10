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
package com.redhat.devtools.intellij.knative.kn;

import com.intellij.openapi.project.Project;
import com.redhat.devtools.intellij.common.utils.NetworkUtils;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class KnCli implements Kn{

    private String command;
    private final Project project;
    private final KubernetesClient client;
    private Map<String, String> envVars;

    public KnCli(Project project, String command) {
        this.command = command;
        this.project = project;
        this.client = new DefaultKubernetesClient(new ConfigBuilder().build());
        try {
            this.envVars = NetworkUtils.buildEnvironmentVariables(client.getMasterUrl().toString());
        } catch (URISyntaxException e) {
            this.envVars = Collections.emptyMap();
        }
    }

    @Override
    public boolean isKnativeServingAware() throws IOException {
        try {
            return client.rootPaths().getPaths().stream().filter(path -> path.endsWith("serving.knative.dev")).findFirst().isPresent();
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isKnativeEventingAware() throws IOException {
        try {
            return client.rootPaths().getPaths().stream().filter(path -> path.endsWith("eventing.knative.dev")).findFirst().isPresent();
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    @Override
    public URL getMasterUrl() {
        return client.getMasterUrl();
    }
}
