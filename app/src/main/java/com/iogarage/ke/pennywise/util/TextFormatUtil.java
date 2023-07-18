package com.iogarage.ke.pennywise.util;

import android.content.Context;

import com.iogarage.ke.pennywise.R;
import com.iogarage.ke.pennywise.domain.entity.Reminder;

import java.util.Arrays;

public class TextFormatUtil {

    public static String formatDaysOfWeekText(Context context, boolean[] daysOfWeek) {
        final String[] shortWeekDays = DateAndTimeUtil.getShortWeekDays();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getString(R.string.repeats_on));
        stringBuilder.append(" ");
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (daysOfWeek[i]) {
                stringBuilder.append(shortWeekDays[i]);
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    public static boolean[] fromDatsOfWeekText(String value) {
        final String[] shortWeekDays = DateAndTimeUtil.getShortWeekDays();
        final String[] tokens = value.split(" ");
        boolean[] daysOfWeek = new boolean[7];

        for (int i = 0; i < shortWeekDays.length; i++) {
            if (Arrays.asList(tokens).contains(shortWeekDays[i]))
                daysOfWeek[i] = true;
        }

        return daysOfWeek;
    }
}