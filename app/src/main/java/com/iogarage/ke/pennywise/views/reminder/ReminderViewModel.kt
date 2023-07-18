package com.iogarage.ke.pennywise.views.reminder

import androidx.lifecycle.ViewModel
import com.iogarage.ke.pennywise.domain.TransactionRepository
import com.iogarage.ke.pennywise.domain.PaymentRepository
import com.iogarage.ke.pennywise.domain.entity.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    /*fun getReminder(reminderId: Int) = liveData {
        val result = reminderRepository.getReminder(reminderId)
        result.collect { emit(it) }
    }*/

    fun deleteReminder(reminder: Reminder) {

    }


}