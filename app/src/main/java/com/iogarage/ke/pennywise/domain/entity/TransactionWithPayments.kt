package com.iogarage.ke.pennywise.domain.entity

import androidx.room.Embedded
import androidx.room.Relation


data class TransactionWithPayments(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "transactionId",
        entityColumn = "transactionId"
    )
    val payments: List<Payment>,
)
