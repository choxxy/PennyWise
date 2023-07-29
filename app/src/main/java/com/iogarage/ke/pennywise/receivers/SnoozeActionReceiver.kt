package com.iogarage.ke.pennywise.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.iogarage.ke.pennywise.views.alarm.SnoozeDialogActivity
import de.coldtea.smplr.smplralarm.apis.SmplrAlarmAPI

class SnoozeActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val requestId = intent.getIntExtra(SmplrAlarmAPI.SMPLR_ALARM_REQUEST_ID, -1)

       // val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        // Close notification tray
        notificationManager.cancel(requestId)

        /*val snoozeIntent = Intent(context, SnoozeDialogActivity::class.java)
        snoozeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        snoozeIntent.putExtra("NOTIFICATION_ID", notificationId)
        context.startActivity(snoozeIntent)*/
    }
}