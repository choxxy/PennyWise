package com.iogarage.ke.pennywise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.iogarage.ke.pennywise.PennyApp;
import com.iogarage.ke.pennywise.entities.Reminder;
import com.iogarage.ke.pennywise.entities.ReminderDao;
import com.iogarage.ke.pennywise.util.AlarmUtil;
import com.iogarage.ke.pennywise.util.DateAndTimeUtil;

import java.util.Calendar;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ReminderDao reminderDao = ((PennyApp) context.getApplicationContext()).getDaoSession().getReminderDao();

        List<Reminder> reminderList = reminderDao.queryBuilder()
                .where(ReminderDao.Properties.Active.eq("true"))
                .list();

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);

        for (Reminder reminder : reminderList) {
            Calendar calendar = DateAndTimeUtil.parseDateAndTime(reminder.getDateAndTime());
            calendar.set(Calendar.SECOND, 0);
            AlarmUtil.setAlarm(context, alarmIntent, reminder.getId().intValue(), calendar);
        }
    }
}