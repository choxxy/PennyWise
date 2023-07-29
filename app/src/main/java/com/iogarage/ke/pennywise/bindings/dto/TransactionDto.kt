package com.iogarage.ke.pennywise.bindings.dto

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.iogarage.ke.pennywise.BR
import com.iogarage.ke.pennywise.domain.entity.LoanStatus
import com.iogarage.ke.pennywise.domain.entity.ReminderStatus
import com.iogarage.ke.pennywise.domain.entity.Transaction
import com.iogarage.ke.pennywise.domain.entity.TransactionType
import java.time.LocalDate

class TransactionDto : BaseObservable() {

    var debtId: Long = 0
    var alarmId: Int = 0
    var reminderStatus: ReminderStatus = ReminderStatus.OFF
    var reminderTitle: String = ""
    var reminderMessage: String = ""
    var balance: Double = 0.0
    var type: TransactionType = TransactionType.LENDING

    @get:Bindable
    var transactionDate: Long = LocalDate.now().toEpochDay()
        set(value) {
            field = value
            notifyPropertyChanged(BR.transactionDate)
        }

    @get:Bindable // 1
    var personName: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.personName) // 2
        }

    @get:Bindable
    var phoneNumber: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.phoneNumber)
        }

    @get:Bindable // 1
    var amount: Double = 0.0
        set(value) {
            field = value
            notifyPropertyChanged(BR.amount)
            balance = value
        }

    @get:Bindable
    var note: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.note)
        }

    @get:Bindable // 1
    var payDate: Long = LocalDate.now().plusMonths(1).toEpochDay()
        set(value) {
            field = value
            notifyPropertyChanged(BR.payDate) // 2
        }

    @get:Bindable
    var paid: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.paid)
        }


    @get:Bindable
    var currency: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.currency)
        }

    @get:Bindable // 1
    var status: LoanStatus = LoanStatus.ACTIVE
        set(value) {
            field = value
            notifyPropertyChanged(BR.status) // 2
        }

    @get:Bindable
    var reminderDate: Long = 0L
        set(value) {
            field = value
            notifyPropertyChanged(BR.reminderDate)
            reminderStatus = if (value != 0L)
                ReminderStatus.ON
            else
                ReminderStatus.OFF

        }

}

fun TransactionDto.toEntity(): Transaction =
    Transaction(
        transactionId = this.debtId,
        transactionDate = this.transactionDate,
        personName = this.personName,
        phoneNumber = this.phoneNumber,
        amount = this.amount,
        note = this.note,
        payDate = this.payDate,
        paid = this.paid,
        balance = this.balance,
        type = this.type,
        currency = this.currency,
        status = this.status,
        reminderTitle = this.reminderTitle,
        reminderContent = this.reminderMessage,
        reminderDate = this.reminderDate,
        reminderStatus = this.reminderStatus,
        alarmId = this.alarmId
    )

fun TransactionDto.fromEntity(entity: Transaction) {
    this.debtId = entity.transactionId
    this.transactionDate = entity.transactionDate
    this.personName = entity.personName
    this.phoneNumber = entity.phoneNumber
    this.amount = entity.amount
    this.note = entity.note
    this.payDate = entity.payDate
    this.paid = entity.paid
    this.balance = entity.balance
    this.type = entity.type
    this.currency = entity.currency
    this.status = entity.status
    this.reminderTitle = entity.reminderTitle
    this.reminderMessage = entity.reminderContent
    this.reminderDate = entity.reminderDate
    this.alarmId = entity.alarmId
    this.reminderStatus = entity.reminderStatus
}





