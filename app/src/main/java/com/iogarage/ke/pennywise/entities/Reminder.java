package com.iogarage.ke.pennywise.entities;


import android.content.Context;

import com.iogarage.ke.pennywise.util.TextFormatUtil;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;

import java.util.Date;

@Entity(nameInDb = "reminders")
public class Reminder {

    // Reminder types
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 2;

    // Repetition types
    public static final int NO_REMINDER = 0;
    public static final int HOURLY = 1;
    public static final int DAILY = 2;
    public static final int WEEKLY = 3;
    public static final int MONTHLY = 4;
    public static final int YEARLY = 5;
    public static final int SPECIFIC_DAYS = 6;
    public static final int ADVANCED = 7;

    @Id(autoincrement = true)
    private Long id;
    private String title;
    private String content;
    private String dateAndTime;
    private int repeatType;
    private String foreverState;
    private boolean active;
    private int numberShown;
    private String daysOfWeek;
    private int interval;


    @Generated(hash = 4427342)
    public Reminder() {
    }


    @Generated(hash = 188317530)
    public Reminder(Long id, String title, String content, String dateAndTime,
                    int repeatType, String foreverState, boolean active, int numberShown,
                    String daysOfWeek, int interval) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.dateAndTime = dateAndTime;
        this.repeatType = repeatType;
        this.foreverState = foreverState;
        this.active = active;
        this.numberShown = numberShown;
        this.daysOfWeek = daysOfWeek;
        this.interval = interval;
    }


    @Keep
    public Reminder setId(Long id) {
        this.id = id;
        return this;
    }

    @Keep
    public Reminder setTitle(String title) {
        this.title = title;
        return this;
    }

    @Keep
    public Reminder setContent(String content) {
        this.content = content;
        return this;
    }

    @Keep
    public Reminder setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
        return this;
    }

    @Keep
    public Reminder setRepeatType(int repeatType) {
        this.repeatType = repeatType;
        return this;
    }

    @Keep
    public Reminder setForeverState(String foreverState) {
        this.foreverState = foreverState;
        return this;
    }

    @Keep
    public Reminder setActive(boolean active) {
        this.active = active;
        return this;
    }

    @Keep
    public Reminder setNumberShown(int numberShown) {
        this.numberShown = numberShown;
        return this;
    }

    @Keep
    public Reminder setDaysOfWeek(Context context, boolean[] daysOfWeek) {
        this.daysOfWeek = TextFormatUtil.formatDaysOfWeekText(context, daysOfWeek);
        return this;
    }

    @Keep
    public Reminder setInterval(int interval) {
        this.interval = interval;
        return this;
    }


    public Long getId() {
        return this.id;
    }


    public String getTitle() {
        return this.title;
    }


    public String getContent() {
        return this.content;
    }


    public String getDateAndTime() {
        return this.dateAndTime;
    }


    public int getRepeatType() {
        return this.repeatType;
    }


    public String getForeverState() {
        return this.foreverState;
    }


    public boolean getActive() {
        return this.active;
    }


    public int getNumberShown() {
        return this.numberShown;
    }


    public String getDaysOfWeek() {
        return this.daysOfWeek;
    }


    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }


    public int getInterval() {
        return this.interval;
    }


}
