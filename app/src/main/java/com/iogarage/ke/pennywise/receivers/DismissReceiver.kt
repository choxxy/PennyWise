package com.iogarage.ke.pennywise.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) = asyncReceiver {
        /* val reminderId = intent.getIntExtra("NOTIFICATION_ID", 0)
         NotificationUtil.cancelNotification(context, reminderId)

         val reminder = reminderRepository.getReminder(reminderId)
         reminder.numberShown = (reminder.numberShown + 1)
         reminder.status = ReminderStatus.TAKEN
         reminder.dateAndTimeTake = DateAndTimeUtil.toStringDateAndTime(Calendar.getInstance())
         reminderRepository.updateReminder(reminder)

         // Check if new alarm needs to be set
         if (reminder.numberToShow > reminder.numberShown) {
             AlarmUtil.setNextAlarm(context, reminderRepository, reminder)
         }*/
    }

}