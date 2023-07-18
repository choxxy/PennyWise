package com.iogarage.ke.pennywise.views.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.transition.Explode
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionSet
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.iogarage.ke.pennywise.views.PennyMain
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.views.transactions.TransactionView
import com.iogarage.ke.pennywise.databinding.ActivityViewBinding
import com.iogarage.ke.pennywise.domain.entity.Reminder
import com.iogarage.ke.pennywise.receivers.AlarmReceiver
import com.iogarage.ke.pennywise.receivers.DismissReceiver
import com.iogarage.ke.pennywise.receivers.SnoozeReceiver
import com.iogarage.ke.pennywise.util.AlarmUtil
import com.iogarage.ke.pennywise.util.DateAndTimeUtil
import com.iogarage.ke.pennywise.util.NotificationUtil
import com.iogarage.ke.pennywise.util.TextFormatUtil
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Boolean.parseBoolean
import java.util.Calendar

@AndroidEntryPoint
class ReminderActivity : AppCompatActivity() {


    private lateinit var reminder: Reminder
    private var hideMarkAsDone = false
    private var reminderChanged = false

    private lateinit var binding: ActivityViewBinding
    private val viewModel: ReminderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupTransitions()
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(null)

        ViewCompat.setElevation(binding.header, resources.getDimension(R.dimen.toolbar_elevation))

        val intent: Intent = intent
        val mReminderId: Int = intent.getIntExtra("NOTIFICATION_ID", 0)

        // Arrived to activity from notification on click
        // Cancel notification and nag alarm
        if (intent.getBooleanExtra("NOTIFICATION_DISMISS", false)) {
            val dismissIntent: Intent = Intent().setClass(this, DismissReceiver::class.java)
            dismissIntent.putExtra("NOTIFICATION_ID", mReminderId)
            sendBroadcast(dismissIntent)
        }

       /* viewModel.getReminder(mReminderId).observe(this) { reminder ->
            // Check if notification has been deleted
            if (reminder == null) {
                returnHome()
            }
            this.reminder = reminder
        }*/
    }

    private fun assignReminderValues() {
       // val calendar: Calendar = DateAndTimeUtil.parseDateAndTime(reminder.dateAndTime)
        binding.notificationLayout.notificationTitle.text = reminder.title
        binding.notificationLayout.notificationContent.text = reminder.content
      //  binding.date.text = DateAndTimeUtil.toStringReadableDate(calendar)
        binding.notificationLayout.notificationIcon.setImageResource(
            R.drawable.ic_notifications_black_empty
        )
        binding.notificationLayout.notificationCircle.setColorFilter(Color.parseColor("#CCFFDD"))
      //  val readableTime: String = DateAndTimeUtil.toStringReadableTime(calendar, this)
    //    binding.time.text = readableTime
    //    binding.notificationLayout.notificationTime.text = readableTime
      /*  if (reminder.repeatType == Reminder.SPECIFIC_DAYS) {
            binding.repeat.text = reminder.daysOfWeek
        } else {
            if (reminder.interval > 1) {
                binding.repeat.text = TextFormatUtil.formatAdvancedRepeatText(
                    this,
                    reminder.repeatType,
                    reminder.interval
                )
            } else {
                binding.repeat.text =
                    resources.getStringArray(R.array.repeat_array)[reminder.repeatType]
            }
        }
        if (java.lang.Boolean.parseBoolean(reminder.foreverState)) {
            binding.shown.setText(R.string.forever)
        } else {
            binding.shown.text = getString(R.string.times_shown, reminder.numberShown, 1)
        }*/

        // Hide "Mark as done" action if reminder is inactive
     //   hideMarkAsDone = reminder.active && !java.lang.Boolean.parseBoolean(reminder.foreverState)
        invalidateOptionsMenu()
    }

    private fun setupTransitions() {

        // Enter transitions
        val setEnter = TransitionSet()
        val slideDown: Transition = Explode()
        slideDown.addTarget(binding.header)
        slideDown.excludeTarget(binding.scroll, true)
        slideDown.duration = 500
        setEnter.addTransition(slideDown)
        val fadeOut: Transition = Slide(Gravity.BOTTOM)
        fadeOut.addTarget(binding.scroll)
        fadeOut.duration = 500
        setEnter.addTransition(fadeOut)

        // Exit transitions
        val setExit = TransitionSet()
        val slideDown2: Transition = Explode()
        slideDown2.addTarget(binding.header)
        slideDown2.duration = 570
        setExit.addTransition(slideDown2)
        val fadeOut2: Transition = Slide(Gravity.BOTTOM)
        fadeOut2.addTarget(binding.scroll)
        fadeOut2.duration = 280
        setExit.addTransition(fadeOut2)
        window.enterTransition = setEnter
        window.returnTransition = setExit

    }

    private fun confirmDelete() {
        AlertDialog.Builder(this, R.style.Dialog)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(
                R.string.yes
            ) { _, _ -> actionDelete() }
            .setNegativeButton(R.string.no, null).show()
    }

    private fun actionShowNow() {
        NotificationUtil.createNotification(this, reminder)
    }

    private fun actionDelete() {
       /* viewModel.deleteReminder(reminder)
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        AlarmUtil.cancelAlarm(this, alarmIntent, reminder.id)
        val snoozeIntent = Intent(this, SnoozeReceiver::class.java)
        AlarmUtil.cancelAlarm(this, snoozeIntent, reminder.id)
        finish()*/
    }

    private fun actionEdit() {
        val intent = Intent(this, TransactionView::class.java)
        //intent.putExtra("NOTIFICATION_ID", reminder.id)
        startActivity(intent)
        finish()
    }

    private fun actionMarkAsDone() {
        reminderChanged = true
        // Check whether next alarm needs to be set
        /*if (reminder.active || parseBoolean(reminder.foreverState)) {
            AlarmUtil.setNextAlarm(this, reminder, session.getReminderDao())
        } else {
            val alarmIntent = Intent(getApplicationContext(), AlarmReceiver::class.java)
            AlarmUtil.cancelAlarm(this, alarmIntent, reminder.id.intValue())
            reminder.setDateAndTime(DateAndTimeUtil.toStringDateAndTime(Calendar.getInstance()))
        }
        reminder.setNumberShown(reminder.numberShown + 1)
        session.getReminderDao().update(reminder)
        assignReminderValues()
        Snackbar.make(binding.viewCoordinator, R.string.toast_mark_as_done, Snackbar.LENGTH_SHORT).show()*/
    }

    private fun actionShareText() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, reminder.title + "\n" + reminder.content)
        startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
    }

    private fun returnHome() {
        val intent = Intent(this, PennyMain::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    fun updateReminder() {
       // reminder = session.getReminderDao().loadByRowId(reminder.id)
        assignReminderValues()
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(messageReceiver, IntentFilter("BROADCAST_REFRESH"))
        updateReminder()
        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
        super.onPause()
    }

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            reminderChanged = true
            updateReminder()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_viewer, menu)
        if (hideMarkAsDone) {
            menu.findItem(R.id.action_mark_as_done).isVisible = false
        }
        return true
    }

    override fun onBackPressed() {
        if (reminderChanged) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            R.id.action_delete -> {
                confirmDelete()
                return true
            }

            R.id.action_edit -> {
                actionEdit()
                return true
            }

            R.id.action_share -> {
                actionShareText()
                return true
            }

            R.id.action_mark_as_done -> {
                actionMarkAsDone()
                return true
            }

            R.id.action_show_now -> {
                actionShowNow()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}