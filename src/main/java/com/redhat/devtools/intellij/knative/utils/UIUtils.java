/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.knative.utils;

import com.intellij.openapi.ui.Divider;
import com.intellij.ui.OnePixelSplitter;

import java.awt.Color;

import static com.intellij.ide.plugins.PluginManagerConfigurable.SEARCH_FIELD_BORDER_COLOR;

public class UIUtils {
    public static OnePixelSplitter createSplitter(boolean vertical, float proportion) {
        return createSplitter(vertical, proportion, SEARCH_FIELD_BORDER_COLOR);
    }

    public static OnePixelSplitter createSplitter(boolean vertical, float proportion, Color background) {
        return new OnePixelSplitter(vertical, proportion) {
            protected Divider createDivider() {
                Divider divider = super.createDivider();
                divider.setBackground(background);
                return divider;
            }
        };
    }
}
