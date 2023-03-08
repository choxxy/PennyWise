package com.iogarage.ke.pennywise.util;

import android.content.Context;

import com.iogarage.ke.pennywise.R;
import com.iogarage.ke.pennywise.entities.Reminder;

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


    public static String formatAdvancedRepeatText(Context context, int repeatType, int interval) {
        String typeText;
        switch (repeatType) {
            default:
            case Reminder.HOURLY:
                typeText = context.getResources().getQuantityString(R.plurals.hour, interval);
                break;
            case Reminder.DAILY:
                typeText = context.getResources().getQuantityString(R.plurals.day, interval);
                break;
            case Reminder.WEEKLY:
                typeText = context.getResources().getQuantityString(R.plurals.week, interval);
                break;
            case Reminder.MONTHLY:
                typeText = context.getResources().getQuantityString(R.plurals.month, interval);
                break;
            case Reminder.YEARLY:
                typeText = context.getResources().getQuantityString(R.plurals.year, interval);
                break;
        }
        return context.getString(R.string.repeats_every, interval, typeText);
    }
}