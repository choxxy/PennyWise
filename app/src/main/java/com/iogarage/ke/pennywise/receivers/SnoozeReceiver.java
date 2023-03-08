package com.iogarage.ke.pennywise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.iogarage.ke.pennywise.PennyApp;
import com.iogarage.ke.pennywise.entities.Reminder;
import com.iogarage.ke.pennywise.entities.ReminderDao;
import com.iogarage.ke.pennywise.util.NotificationUtil;


public class SnoozeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ReminderDao reminderDao = ((PennyApp) context.getApplicationContext()).getDaoSession().getReminderDao();
        int reminderId = intent.getIntExtra("NOTIFICATION_ID", 0);


        if (reminderId != 0) {

            Reminder reminder = reminderDao.queryBuilder()
                    .where(ReminderDao.Properties.Id.eq(reminderId))
                    .unique();

            if (reminder != null)
                NotificationUtil.createNotification(context, reminder);
        }
    }
}