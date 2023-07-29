package com.iogarage.ke.pennywise.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    var transactionId: Long = 0,
    var transactionDate: Long = LocalDate.now().toEpochDay(),
    var personName: String = "",
    var phoneNumber: String = "",
    var amount: Double = 0.0,
    var note: String = "",
    var payDate: Long = LocalDate.now().plusMonths(1).toEpochDay(),
    var paid: Boolean = false,
    var balance: Double = 0.0,
    var type: TransactionType = TransactionType.LENDING,
    var currency: String = "",
    var status: LoanStatus = LoanStatus.ACTIVE,
    var reminderTitle: String = "",
    var reminderContent: String = "",
    var reminderDate: Long = 0,
    var alarmId: Int = 0,
    var reminderStatus: ReminderStatus = ReminderStatus.OFF,
)

enum class LoanStatus {
    ACTIVE,
    LATE,
    BAD,
}

enum class ReminderStatus {
    ON,
    OFF,
    MUTED,
}

enum class TransactionType {
    LENDING,
    BORROWING,
}



