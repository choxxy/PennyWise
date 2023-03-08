package com.iogarage.ke.pennywise.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by CHOXXY on 8/15/2015.
 */
public class Util {

    public static String formatDate(Date date, boolean showYear, boolean showTime) {
        SimpleDateFormat dateFormat;
        if (showYear)
            dateFormat = new SimpleDateFormat("E, dd MMM, yyyy");
        else if (showTime)
            dateFormat = new SimpleDateFormat("E, dd MMM, HH:mm");
        else
            dateFormat = new SimpleDateFormat("E, dd MMM");

        return dateFormat.format(date);

    }

    public static String formatTime(Date date) {
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("HH:mm");

        return dateFormat.format(date);

    }


    public static String resourceString(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    public static long parseLong(Editable text) {
        long lvalue = 0;

        String svalue = text.toString();

        if (svalue.length() != 0) {
            if (TextUtils.isDigitsOnly(svalue)) {
                lvalue = Long.parseLong(svalue);
            }
        }

        return lvalue;
    }

    public static String formatCurrency(double o) {
        DecimalFormat nf = new DecimalFormat("#,##0");
        String formatted = nf.format(o);
        return formatted;
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM, y", Locale.US);
        return sdf.format(date);
    }


    public static Date parseDate(String strDate) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        try {
            date = format.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static double parseDouble(String text) {
        double lvalue = 0;
        String svalue = text;

        if (svalue.length() != 0) {
            try {
                lvalue = Double.parseDouble(svalue);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        return lvalue;
    }

    public static double parseDouble(Editable text) {
        double lvalue = 0;

        String svalue = text.toString();

        if (svalue.length() != 0) {
            if (TextUtils.isDigitsOnly(svalue)) {
                lvalue = Double.parseDouble(svalue);
            }
        }

        return lvalue;
    }

    public static float parseFloat(Editable text) {
        float fvalue = 0;

        String svalue = text.toString();

        if (svalue.length() != 0) {
            if (TextUtils.isDigitsOnly(svalue)) {
                fvalue = Float.parseFloat(svalue);
            }
        }

        return fvalue;
    }

    public static long parseLong(String text, long defaultValue) {
        long lvalue = defaultValue;

        String svalue = text;

        if (svalue.length() != 0) {
            if (TextUtils.isDigitsOnly(svalue)) {
                lvalue = Long.parseLong(svalue);
            }
        }

        return lvalue;
    }

    public static void getPhoneContacts() {

    }

    public static void getPettyCashContacts() {

    }

    public static String getText(EditText editText) {
        return editText.getText().toString();
    }



    //Find maximum (largest) value in array using loop
    public static double getMaxValue(double[] numbers) {
        double maxValue = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] > maxValue) {
                maxValue = numbers[i];
            }
        }
        return maxValue;
    }

    //Find minimum (lowest) value in array using loop
    public static double getMinValue(double[] numbers) {
        double minValue = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < minValue) {
                minValue = numbers[i];
            }
        }
        return minValue;
    }

    public static int getColor() {

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Random random = new Random(System.currentTimeMillis());
        int color = Color.rgb((random.nextInt(127) + 128),
                (random.nextInt(127) + 128),
                (random.nextInt(127) + 128));
        return color;

    }

    public static int convertAmountToCents(double pAmount) {
        BigDecimal vBigDec = new BigDecimal(pAmount);
        vBigDec = vBigDec.setScale(2, BigDecimal.ROUND_HALF_UP);
        return vBigDec.movePointRight(2).intValue();
    }

    private static final NumberFormat FORMAT_CURRENCY = DecimalFormat.getInstance();

    /**
     * Parses an amount into cents.
     *
     * @param p_value Amount formatted using the default currency.
     * @return Value as cents.
     */
    public static int parseAmountToCents(String p_value) {
        try {
            Number v_value = FORMAT_CURRENCY.parse(p_value);
            BigDecimal v_bigDec = new BigDecimal(v_value.doubleValue());
            v_bigDec = v_bigDec.setScale(2, BigDecimal.ROUND_HALF_UP);
            return v_bigDec.movePointRight(2).intValue();
        } catch (ParseException p_ex) {
            try {
                // p_value doesn't have a currency format.
                BigDecimal v_bigDec = new BigDecimal(p_value);
                v_bigDec = v_bigDec.setScale(2, BigDecimal.ROUND_HALF_UP);
                return v_bigDec.movePointRight(2).intValue();
            } catch (NumberFormatException p_ex1) {
                return -1;
            }
        }
    }

    /**
     * Formats cents into a valid amount using the default currency.
     *
     * @param p_value Value as cents
     * @return Amount formatted using a currency.
     */
    public static String formatCentsToAmount(int p_value) {
        BigDecimal v_bigDec = new BigDecimal(p_value);
        v_bigDec = v_bigDec.setScale(2, BigDecimal.ROUND_HALF_UP);
        v_bigDec = v_bigDec.movePointLeft(2);
        String v_currency = FORMAT_CURRENCY.format(v_bigDec.doubleValue());
        return v_currency.replace(FORMAT_CURRENCY.getCurrency().getSymbol(), "").replace(",", "");
    }

    /**
     * Formats cents into a valid amount using the default currency.
     *
     * @param p_value Value as cents
     * @return Amount formatted using a currency.
     */
    public static String formatCentsToCurrency(int p_value) {
        BigDecimal v_bigDec = new BigDecimal(p_value);
        v_bigDec = v_bigDec.setScale(2, BigDecimal.ROUND_HALF_UP);
        v_bigDec = v_bigDec.movePointLeft(2);
        return FORMAT_CURRENCY.format(v_bigDec.doubleValue());
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.i("SERVICES", serviceInfo.service.getClassName());
            if ("com.iogarage.ke.pennywise.alarm.SchedulingService".equals(serviceInfo.service.getClassName()))
                return true;
        }

        return false;
    }


    //get the current version number and name
    public static  String  getVersionName(Context context) {
        String versionName = "3.70";

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;

    }



    //get the current version number and name
    public static  int  getVersionCode(Context context) {

        int versionCode = -1;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return  versionCode;

    }
}
