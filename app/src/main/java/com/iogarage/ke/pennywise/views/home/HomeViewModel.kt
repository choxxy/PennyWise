package com.iogarage.ke.pennywise.views.home

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.domain.TransactionRepository
import com.iogarage.ke.pennywise.domain.entity.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.coldtea.smplr.smplralarm.alarmNotification
import de.coldtea.smplr.smplralarm.channel
import de.coldtea.smplr.smplralarm.smplrAlarmSet
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private var _transactions = MutableLiveData<List<Transaction>>()
    val transaction: LiveData<List<Transaction>> = _transactions

    init {
        getTransactions()
    }

    private fun getTransactions() {
        viewModelScope.launch {
            transactionRepository.getTransactions().collect { list ->
                _transactions.value = list
            }
        }
    }

    fun setAlarm(hour: Int, minute: Int) {
        smplrAlarmSet(context) {
            hour { hour }
            min { minute }
            weekdays {
                monday()
                friday()
                sunday()
            }
            notification {
                alarmNotification {
                    smallIcon { R.drawable.ic_baseline_alarm_on_24 }
                    title { "Simple alarm is ringing" }
                    message { "Simple alarm is ringing" }
                    bigText { "Simple alarm is ringing" }
                    autoCancel { true }
                }
            }
            notificationChannel {
                channel {
                    importance { NotificationManager.IMPORTANCE_HIGH }
                    showBadge { false }
                    name { "de.coldtea.smplr.alarm.channel" }
                    description { "This notification channel is created by SmplrAlarm" }
                }
            }
        }

    }

    fun updateAlarm() {

    }


    fun cancelAlarm() {

    }

}