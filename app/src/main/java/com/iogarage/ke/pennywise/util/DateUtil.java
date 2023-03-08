package com.iogarage.ke.pennywise.util;

import android.content.Context;

import com.iogarage.ke.pennywise.R;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by choxxy on 22/05/2017.
 */

public class DateUtil {

    private static final SimpleDateFormat DATE_AND_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());
    private static final SimpleDateFormat DATE_AND_TIME_WITH_SECONDS_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    private static final SimpleDateFormat READABLE_DAY_MONTH_FORMAT = new SimpleDateFormat("d MMMM", Locale.getDefault());
    private static final SimpleDateFormat READABLE_DAY_MONTH_YEAR_FORMAT = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat READABLE_TIME_24_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat READABLE_TIME_FORMAT = new SimpleDateFormat("h:mm a", Locale.getDefault());
    private static final SimpleDateFormat WEEK_DAYS_FORMAT = new SimpleDateFormat("EEEE", Locale.getDefault());
    private static final SimpleDateFormat SHORT_WEEK_DAYS_FORMAT = new SimpleDateFormat("E", Locale.getDefault());


    public  static Date plusDays(Date date, int days){
        // convert date to calendar
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        // manipulate date
        c.add(Calendar.DATE, days); //same with c.add(Calendar.DAY_OF_MONTH, 1);

        // convert calendar to date
        Date newDate = c.getTime();

        return newDate;

    }


    public   static Date plusMinutes(Date date, int minutes){


        // convert date to calendar
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        // manipulate date
        c.add(Calendar.MINUTE, minutes); //same with c.add(Calendar.DAY_OF_MONTH, 1);

        // convert calendar to date
        Date newDate = c.getTime();

        return newDate;
    }


    public  static Date plusMonths(Date date, int months){


        // convert date to calendar
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        // manipulate date
        c.add(Calendar.MONTH, months); //same with c.add(Calendar.DAY_OF_MONTH, 1);

        // convert calendar to date
        Date newDate = c.getTime();

        return newDate;
    }


    public static String parseDate(long dateInMillis) {

        Date dt = new Date(dateInMillis);
        DateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = fmt.format(dt);
        return dateString;
    }

    public static String parseDate(Date date) {


        DateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = fmt.format(date);
        return dateString;
    }

    public static String parseTime(Date date) {
        DateFormat fmt = new SimpleDateFormat("hh:mm");
        String timeString = fmt.format(date);
        return timeString;
    }

    public static String parseLongDate(long dateInMillis) {

        Date dt = new Date(dateInMillis);
        DateFormat fmt = new SimpleDateFormat();
        String dateString = fmt.format(dt);
        return dateString;
    }

    public static LocalDate date2LocalDate(Date date){


        ZoneId defaultZoneId = ZoneId.systemDefault();
        System.out.println("System Default TimeZone : " + defaultZoneId);

        //1. Convert Date -> Instant
        Instant instant = DateTimeUtils.toInstant(date);
        System.out.println("instant : " + instant); //Zone : UTC+0

        //2. Instant + system default time zone + toLocalDate() = LocalDate
        LocalDate localDate = instant.atZone(defaultZoneId).toLocalDate();

        return localDate;
    }

    public static LocalDateTime date2LocalDateTime(Date date){


        ZoneId defaultZoneId = ZoneId.systemDefault();
        System.out.println("System Default TimeZone : " + defaultZoneId);

        //1. Convert Date -> Instant
        Instant instant = DateTimeUtils.toInstant(date);
        System.out.println("instant : " + instant); //Zone : UTC+0

        LocalDateTime localDateTime = instant.atZone(defaultZoneId).toLocalDateTime();

        return localDateTime;
    }

    public static String getAppropriateDateFormat(Context context, Calendar calendar) {
        if (isThisYear(calendar)) {
            if (isThisMonth(calendar) && isThisDayOfMonth(calendar)) {
                return context.getString(R.string.date_today);
            } else {
                return READABLE_DAY_MONTH_FORMAT.format(calendar.getTime());
            }
        } else {
            return READABLE_DAY_MONTH_YEAR_FORMAT.format(calendar.getTime());
        }
    }

    private static Boolean isThisYear(Calendar calendar) {
        Calendar nowCalendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR);
    }

    private static Boolean isThisMonth(Calendar calendar) {
        Calendar nowCalendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == nowCalendar.get(Calendar.MONTH);
    }

    private static Boolean isThisDayOfMonth(Calendar calendar) {
        Calendar nowCalendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH) == nowCalendar.get(Calendar.DAY_OF_MONTH);
    }

    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
