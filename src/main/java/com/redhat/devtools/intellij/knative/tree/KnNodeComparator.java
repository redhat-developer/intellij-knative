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
package com.redhat.devtools.intellij.knative.tree;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.redhat.devtools.intellij.knative.kn.KnConstants;

import java.util.Comparator;

public class KnNodeComparator<T extends NodeDescriptor<?>> implements Comparator<T> {

    @Override
    public int compare(T o1, T o2) {
        if (o1.equals(o2)) {
            return 0;
        }

        if (o1 instanceof KnRevisionDescriptor && o2 instanceof KnRevisionDescriptor) {
            // we comparing string witch contains number
            return ((KnRevisionDescriptor) o2).getElement().getRevision().getLabels().get(KnConstants.CONFIGURATION_GENERATION).compareTo(
                    ((KnRevisionDescriptor) o1).getElement().getRevision().getLabels().get(KnConstants.CONFIGURATION_GENERATION)
            );
        }
        return 0;
    }
}
