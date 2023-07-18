package com.iogarage.ke.pennywise.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) = asyncReceiver {
        val reminderId = intent.getIntExtra("NOTIFICATION_ID", 0)

        /*val reminder = reminderRepository.getReminder(reminderId)
        reminder.numberShown = (reminder.numberShown + 1)
        reminderRepository.updateReminder(reminder)

        NotificationUtil.createNotification(context, reminder)

        // Check if new alarm needs to be set
        if (reminder.numberToShow > reminder.numberShown) {
            AlarmUtil.setNextAlarm(context, reminderRepository, reminder)
        }*/
        val updateIntent = Intent("BROADCAST_REFRESH")
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)
    }
}
