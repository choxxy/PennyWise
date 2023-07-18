package com.iogarage.ke.pennywise.domain.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation


data class DebtWithPayments(
    @Embedded val debt: Debt,
    @Relation(
        parentColumn = "debtId",
        entityColumn = "debtId"
    )
    val payments: List<Payment>,
)
