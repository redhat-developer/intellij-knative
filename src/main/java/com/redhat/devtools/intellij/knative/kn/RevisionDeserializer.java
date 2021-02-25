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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;

import java.io.IOException;
import java.util.List;

public class RevisionDeserializer extends StdNodeBasedDeserializer<Revision> {
    protected RevisionDeserializer() {
        super(Revision.class);
    }

    @Override
    public Revision convert(JsonNode root, DeserializationContext deserializationContext) throws IOException {
        String name = root.get("metadata").get("name").asText();
        List<StatusCondition> conditions = DeserializerUtil.getConvertToConditions(root.get("status").get("conditions"));
        return new Revision(name, conditions);
    }
}
