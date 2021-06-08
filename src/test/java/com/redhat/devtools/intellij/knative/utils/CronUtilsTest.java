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
import org.junit.Test;


import static com.redhat.devtools.intellij.knative.Constants.DAYS;
import static com.redhat.devtools.intellij.knative.Constants.HOURS;
import static com.redhat.devtools.intellij.knative.Constants.MINUTES;
import static org.junit.Assert.assertEquals;

public class CronUtilsTest {

    @Test
    public void ConvertTimeToCronTabFormat_UnitIsMinutesAndValueIs1_1MinuteInCronTabFormat() {
        assertEquals("*/1 * * * *", CronUtils.convertTimeToCronTabFormat(1, MINUTES));
    }

    @Test
    public void ConvertTimeToCronTabFormat_UnitIsHoursAndValueIs4_4HoursInCronTabFormat() {
        assertEquals("* */4 * * *", CronUtils.convertTimeToCronTabFormat(4, HOURS));
    }

    @Test
    public void ConvertTimeToCronTabFormat_UnitIsDaysAndValueIs5_5DaysInCronTabFormat() {
        assertEquals("* * */5 * *", CronUtils.convertTimeToCronTabFormat(5, DAYS));
    }

    @Test
    public void ConvertTimeToCronTabFormat_UnitIsUnknownAndValueIs2_EmptyString() {
        assertEquals("", CronUtils.convertTimeToCronTabFormat(2, "unknown"));
    }

    @Test
    public void ConvertCronTabFormatInTimeAndUnitPair_ValueContains8MinutesInCronTabFormat_PairWith8AndMinutes() {
        Pair<String, String> timeUnitPair = CronUtils.convertCronTabFormatInTimeAndUnitPair("*/8 * * * *");
        assertEquals("8", timeUnitPair.getFirst());
        assertEquals(MINUTES, timeUnitPair.getSecond());
    }

    @Test
    public void ConvertCronTabFormatInTimeAndUnitPair_ValueContains3HoursInCronTabFormat_PairWith3AndHours() {
        Pair<String, String> timeUnitPair = CronUtils.convertCronTabFormatInTimeAndUnitPair("* */3 * * *");
        assertEquals("3", timeUnitPair.getFirst());
        assertEquals(HOURS, timeUnitPair.getSecond());
    }

    @Test
    public void ConvertCronTabFormatInTimeAndUnitPair_ValueContains1DaysInCronTabFormat_PairWith1AndDays() {
        Pair<String, String> timeUnitPair = CronUtils.convertCronTabFormatInTimeAndUnitPair("* * */1 * *");
        assertEquals("1", timeUnitPair.getFirst());
        assertEquals(DAYS, timeUnitPair.getSecond());
    }

    @Test
    public void ConvertCronTabFormatInTimeAndUnitPair_ValueContains4UnknownInCronTabFormat_PairWith4AndEmptyString() {
        Pair<String, String> timeUnitPair = CronUtils.convertCronTabFormatInTimeAndUnitPair("* * * */4 *");
        assertEquals("4", timeUnitPair.getFirst());
        assertEquals("", timeUnitPair.getSecond());
    }

    @Test
    public void ConvertCronTabFormatInTimeAndUnitPair_ValueContainsUnknownMinutesInCronTabFormat_PairWithEmptyStringAndMinutes() {
        Pair<String, String> timeUnitPair = CronUtils.convertCronTabFormatInTimeAndUnitPair("*/test * * * *");
        assertEquals("", timeUnitPair.getFirst());
        assertEquals(MINUTES, timeUnitPair.getSecond());
    }

    @Test
    public void ConvertCronTabFormatInTimeAndUnitPair_ValueContainsUnknownTimendUnknownUnitInCronTabFormat_PairWithBothEmptyString() {
        Pair<String, String> timeUnitPair = CronUtils.convertCronTabFormatInTimeAndUnitPair("* * * */test *");
        assertEquals("", timeUnitPair.getFirst());
        assertEquals("", timeUnitPair.getSecond());
    }

    @Test
    public void ConvertCronTabFormatInTimeAndUnitPair_ValueIsNull_PairWithBothEmptyString() {
        Pair<String, String> timeUnitPair = CronUtils.convertCronTabFormatInTimeAndUnitPair(null);
        assertEquals("", timeUnitPair.getFirst());
        assertEquals("", timeUnitPair.getSecond());
    }
}
