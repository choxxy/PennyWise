package com.iogarage.ke.pennywise.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    var paymentId: Long = 0,
    var transactionId: Long,
    var paymentDate: Long,
    var description: String,
    var note: String,
    var amountPaid: Double
)