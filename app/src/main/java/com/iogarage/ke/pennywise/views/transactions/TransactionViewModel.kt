package com.iogarage.ke.pennywise.views.transactions

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.bindings.dto.TransactionDto
import com.iogarage.ke.pennywise.bindings.dto.fromEntity
import com.iogarage.ke.pennywise.bindings.dto.toEntity
import com.iogarage.ke.pennywise.domain.TransactionRepository
import com.iogarage.ke.pennywise.domain.entity.Transaction
import com.iogarage.ke.pennywise.domain.entity.TransactionType
import com.iogarage.ke.pennywise.receivers.AlarmBroadcastReceiver
import com.iogarage.ke.pennywise.receivers.SnoozeActionReceiver
import com.iogarage.ke.pennywise.util.AppPreferences
import com.iogarage.ke.pennywise.views.PennyMain
import com.iogarage.ke.pennywise.views.lockscreenalarm.ActivityLockScreenAlarm
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.coldtea.smplr.smplralarm.alarmNotification
import de.coldtea.smplr.smplralarm.channel
import de.coldtea.smplr.smplralarm.extensions.asLocalDate
import de.coldtea.smplr.smplralarm.receivers.ActionReceiver
import de.coldtea.smplr.smplralarm.receivers.AlarmAction
import de.coldtea.smplr.smplralarm.receivers.AlarmAction.ACTION_DISMISS
import de.coldtea.smplr.smplralarm.receivers.AlarmAction.HOUR
import de.coldtea.smplr.smplralarm.receivers.AlarmAction.MINUTE
import de.coldtea.smplr.smplralarm.smplrAlarmCancel
import de.coldtea.smplr.smplralarm.smplrAlarmSet
import de.coldtea.smplr.smplralarm.smplrAlarmUpdate
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.EnumMap
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val state: SavedStateHandle
) : ViewModel() {

    private var _transaction = MutableLiveData<Transaction>()
    val transaction: LiveData<Transaction> = _transaction

    var transactionDto: TransactionDto = TransactionDto()

    private val _nameError = MutableLiveData("")
    val nameError: LiveData<String> = _nameError
    private val _amountError = MutableLiveData("")
    val amountError: LiveData<String> = _amountError
    private val _transactionTypeError = MutableLiveData("")
    val transactionTypeError: LiveData<String> = _transactionTypeError

    val transactionType = MutableLiveData<EnumMap<TransactionType, Boolean>>(
        EnumMap(TransactionType::class.java)
    ).apply {
        TransactionType.values().forEach { value?.put(it, false) }
    }

    init {
        val transactionId = state.get<Long>("transactionId")
        transactionId?.let {
            if (it != 0L)
                getTransaction(it)
        }
    }

    private fun getTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransaction(id)
            transactionDto.fromEntity(transaction)
        }
    }

    fun update(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateDebt(transaction)
        }
    }

    fun setReminderDate(timeInMillis: Long) {
        val localTime =
            Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        transactionDto.reminderDate = localTime.toEpochDay()
    }

    private fun validateInput(): Boolean {
        var isValid = true
        _nameError.value = ""
        _transactionTypeError.value = ""
        _amountError.value = ""

        val selectedTransactionType =
            transactionType.value?.filter { e -> e.value }?.keys
        if (selectedTransactionType?.isEmpty() == true) {
            _transactionTypeError.value = "Transaction type required"
            isValid = false
        } else {
            val type = selectedTransactionType?.first()
            transactionDto.type = type!!
        }

        if (transactionDto.personName.isEmpty()) {
            _nameError.value = "Name is required"
            isValid = false
        }

        if (transactionDto.amount <= 0.0) {
            _amountError.value = "Must be more than 0"
            isValid = false
        }
        return isValid

    }

    fun save() {
        viewModelScope.launch {
            if (validateInput()) {
                transactionRepository.insertTransaction(transactionDto.toEntity())
                if (transactionDto.reminderDate != 0L) {
                    val defaultAlarmHour = appPreferences.getDefaultAlarmHour()
                    setAlarm(defaultAlarmHour, 0, transactionDto.reminderDate.asLocalDate())
                }
            }
        }

    }

    fun setEndDate(timeInMillis: Long) {
        val localDate =
            Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        transactionDto.payDate = localDate.toEpochDay()
    }

    fun setStartDate(timeInMillis: Long) {
        val localDate =
            Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.systemDefault()).toLocalDate();
        transactionDto.transactionDate = localDate.toEpochDay()
    }

    private fun setAlarm(hour: Int, minute: Int, date: LocalDate): Int {

        val onClickShortcutIntent = Intent(
            context,
            PennyMain::class.java
        )

        val fullScreenIntent = Intent(
            context,
            ActivityLockScreenAlarm::class.java
        )

        val alarmReceivedIntent = Intent(
            context,
            AlarmBroadcastReceiver::class.java
        )

        val snoozeIntent = Intent(context, SnoozeActionReceiver::class.java).apply {
            action = AlarmAction.ACTION_SNOOZE
            putExtra(HOUR, hour)
            putExtra(MINUTE, minute)
        }

        val dismissIntent = Intent(context, ActionReceiver::class.java).apply {
            action = ACTION_DISMISS
        }

        val notificationDismissIntent = Intent(context, ActionReceiver::class.java).apply {
            action = AlarmAction.ACTION_NOTIFICATION_DISMISS
        }

        fullScreenIntent.putExtra("SmplrText", "You did it, you crazy bastard you did it!")

        val alarmId = smplrAlarmSet(context) {
            hour { hour }
            min { minute }
            date { date }
            weekdays {}
            contentIntent { onClickShortcutIntent }
            receiverIntent { fullScreenIntent }
            alarmReceivedIntent { alarmReceivedIntent }
            notification {
                alarmNotification {
                    smallIcon { R.drawable.alarm }
                    title { "Simple alarm is ringing" }
                    message { "Simple alarm is ringing" }
                    bigText { "Simple alarm is ringing" }
                    autoCancel { true }
                    firstButtonText { "Snooze" }
                    secondButtonText { "Dismiss" }
                    firstButtonIntent { snoozeIntent }
                    secondButtonIntent { dismissIntent }
                    notificationDismissedIntent { notificationDismissIntent }
                }
            }
            notificationChannel {
                channel {
                    importance { NotificationManager.IMPORTANCE_HIGH }
                    showBadge { false }
                    name { "com.iogarage.ke.pennywise.channel" }
                    description { "This notification channel is created by SmplrAlarm" }
                }
            }
        }
        return alarmId
    }

    fun updateAlarm(alarmId: Int, hour: Int, minute: Int, date: LocalDate, isActive: Boolean) {

        smplrAlarmUpdate(context) {
            requestCode { alarmId }
            hour { hour }
            min { minute }
            date { date }
            weekdays {}
            isActive { isActive }
        }

        smplrAlarmUpdate(context) {
        }
    }


    fun cancelAlarm(alarmId: Int) {
        smplrAlarmCancel(context) {
            requestCode { alarmId }
        }
    }


}