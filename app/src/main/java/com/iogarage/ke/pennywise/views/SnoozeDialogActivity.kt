package com.iogarage.ke.pennywise.views

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.databinding.NumberPickerBinding
import com.iogarage.ke.pennywise.receivers.SnoozeReceiver
import com.iogarage.ke.pennywise.util.AlarmUtil
import com.iogarage.ke.pennywise.util.NotificationUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class SnoozeDialogActivity : AppCompatActivity() {
    private lateinit var hourPicker: NumberPicker
    private lateinit var minutePicker: NumberPicker
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var  binding: NumberPickerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = layoutInflater.inflate(R.layout.number_picker, null)
        val reminderId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val builder = AlertDialog.Builder(this, R.style.Dialog)
        builder.setTitle(R.string.snooze_length)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        hourPicker = view.findViewById(R.id.picker1)
        minutePicker = view.findViewById(R.id.picker2)
        setUpHourPicker()
        setUpMinutePicker()
        builder.setPositiveButton(R.string.ok) { _, which ->
            if (hourPicker.getValue() != 0 || minutePicker.getValue() != 0) {
                NotificationUtil.cancelNotification(applicationContext, reminderId)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MINUTE, minutePicker.getValue())
                calendar.add(Calendar.HOUR, hourPicker.getValue())
                val alarmIntent = Intent(applicationContext, SnoozeReceiver::class.java)
                AlarmUtil.setAlarm(applicationContext, alarmIntent, reminderId, calendar)
                val editor = sharedPreferences.edit()
                editor.putInt("snoozeHours", hourPicker.getValue())
                editor.putInt("snoozeMinutes", minutePicker.getValue())
                editor.apply()
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
        hourPicker!!.minValue = 0
        hourPicker!!.maxValue = 24
        hourPicker!!.value = sharedPreferences!!.getInt(
            "snoozeHours",
            resources.getInteger(R.integer.default_snooze_hours)
        )
        val hourValues = arrayOfNulls<String>(25)
        for (i in hourValues.indices) {
            hourValues[i] = String.format(resources.getQuantityString(R.plurals.time_hour, i), i)
        }
        hourPicker!!.displayedValues = hourValues
    }

    private fun setUpMinutePicker() {
        minutePicker!!.minValue = 0
        minutePicker!!.maxValue = 60
        minutePicker!!.value = sharedPreferences!!.getInt(
            "snoozeMinutes",
            resources.getInteger(R.integer.default_snooze_minutes)
        )
        val minuteValues = arrayOfNulls<String>(61)
        for (i in minuteValues.indices) {
            minuteValues[i] =
                String.format(resources.getQuantityString(R.plurals.time_minute, i), i)
        }
        minutePicker!!.displayedValues = minuteValues
    }
}