package com.iogarage.ke.pennywise.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.iogarage.ke.pennywise.PennyApp;
import com.iogarage.ke.pennywise.entities.Reminder;
import com.iogarage.ke.pennywise.entities.ReminderDao;
import com.iogarage.ke.pennywise.util.AlarmUtil;
import com.iogarage.ke.pennywise.util.NotificationUtil;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ReminderDao reminderDao = ((PennyApp) context.getApplicationContext()).getDaoSession().getReminderDao();
        int id = intent.getIntExtra("NOTIFICATION_ID", 0);
        Reminder reminder = reminderDao.loadByRowId(id);
        reminder.setNumberShown(reminder.getNumberShown() + 1);
        reminderDao.update(reminder);

        NotificationUtil.createNotification(context, reminder);

        // Check if new alarm needs to be set
        if (reminder.getActive() || Boolean.parseBoolean(reminder.getForeverState())) {
            AlarmUtil.setNextAlarm(context, reminder, reminderDao);
        }
        Intent updateIntent = new Intent("BROADCAST_REFRESH");
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent);
    }
}