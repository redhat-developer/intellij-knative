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
package com.redhat.devtools.intellij.knative.ui;

import com.intellij.ui.JBColor;

import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import java.awt.Dimension;

public class UIConstants {
    public static final Color RED = new JBColor(new Color(229, 77, 4), new Color(229, 77, 4));

    public static final Border RED_BORDER_SHOW_ERROR = new MatteBorder(1, 1, 1, 1, RED);
    public static final Dimension ROW_DIMENSION = new Dimension(Integer.MAX_VALUE, 40);
}
