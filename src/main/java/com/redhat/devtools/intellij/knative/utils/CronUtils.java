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

import com.intellij.openapi.util.Pair;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static com.redhat.devtools.intellij.knative.Constants.DAYS;
import static com.redhat.devtools.intellij.knative.Constants.HOURS;
import static com.redhat.devtools.intellij.knative.Constants.MINUTES;

public class CronUtils {
    public static String convertTimeToCronTabFormat(int value, String unit) {
        switch(unit) {
            case MINUTES: {
                return "*/" + value + " * * * *";
            }
            case HOURS: {
                return "* */" + value + " * * *";
            }
            case DAYS: {
                return "* * */" + value + " * *";
            }
            default: {
                return "";
            }
        }
    }

    public static Pair<String, String> convertCronTabFormatInTimeAndUnitPair(String value) {
        String time = "";
        String unit = "";
        Pattern pattern = Pattern.compile("\\*\\/(\\d)");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            time = matcher.group(1);
        }

        if (value.startsWith("*/")) {
            unit = MINUTES;
        } else if (value.startsWith("* */")) {
            unit = HOURS;
        } else if (value.startsWith("* * */")) {
            unit = DAYS;
        }

        return Pair.create(time, unit);
    }
}
