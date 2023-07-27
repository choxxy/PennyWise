package com.iogarage.ke.pennywise.views.alarm

import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.util.AppPreferences
import com.iogarage.ke.pennywise.util.NotificationUtil
import dagger.hilt.android.AndroidEntryPoint
import de.coldtea.smplr.smplralarm.smplrAlarmUpdate
import javax.inject.Inject

@AndroidEntryPoint
class SnoozeDialogActivity : AppCompatActivity() {
    @Inject
    lateinit var appPreferences: AppPreferences

    private lateinit var hourPicker: NumberPicker
    private lateinit var minutePicker: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = layoutInflater.inflate(R.layout.number_picker, null)
        val reminderId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val builder = AlertDialog.Builder(this, R.style.Dialog)
        builder.setTitle(R.string.snooze_length)
        hourPicker = view.findViewById(R.id.picker1)
        minutePicker = view.findViewById(R.id.picker2)
        setUpHourPicker()
        setUpMinutePicker()
        builder.setPositiveButton(R.string.ok) { _, _ ->
            val hours = hourPicker.value
            val minutes = minutePicker.value
            if (hours != 0 || minutes != 0) {
                NotificationUtil.cancelNotification(applicationContext, reminderId)

                appPreferences.putInt("snoozeHours", hours)
                appPreferences.putInt("snoozeMinutes", minutes)

                smplrAlarmUpdate(this) {
                    requestCode { reminderId }
                    hour { hours }
                    min { minutes }
                }
            }
            finish()
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, which ->
            dialog.cancel()
            finish()
        }
        builder.setOnCancelListener { finish() }
        builder.setView(view).create().show()
    }

    private fun setUpHourPicker() {
        hourPicker.minValue = 0
        hourPicker.maxValue = 24
        hourPicker.value = appPreferences.getInt(
            "snoozeHours",
            resources.getInteger(R.integer.default_snooze_hours)
        )
        val hourValues = arrayOfNulls<String>(25)
        for (i in hourValues.indices) {
            hourValues[i] = String.format(resources.getQuantityString(R.plurals.time_hour, i), i)
        }
        hourPicker.displayedValues = hourValues
    }

    private fun setUpMinutePicker() {
        minutePicker.minValue = 0
        minutePicker.maxValue = 60
        minutePicker.value = appPreferences.getInt(
            "snoozeMinutes",
            resources.getInteger(R.integer.default_snooze_minutes)
        )
        val minuteValues = arrayOfNulls<String>(61)
        for (i in minuteValues.indices) {
            minuteValues[i] =
                String.format(resources.getQuantityString(R.plurals.time_minute, i), i)
        }
        minutePicker.displayedValues = minuteValues
    }
}