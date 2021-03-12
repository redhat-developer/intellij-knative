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
package com.redhat.devtools.intellij.knative.ui;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class UIConstants {
    public static final Border BORDER_LABEL_NAME = new EmptyBorder(10, 0, 0, 0);
    public static final Font TIMES_PLAIN_14 = new Font(Font.DIALOG, Font.PLAIN, Font.PLAIN);
    public static final Dimension ROW_DIMENSION = new Dimension(400, 33);
}
