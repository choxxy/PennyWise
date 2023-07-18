package com.iogarage.ke.pennywise.service.model

import com.iogarage.ke.pennywise.domain.entity.Payment
import com.iogarage.ke.pennywise.domain.entity.Reminder

data class Transaction(
    var id: String,
    var userId: String,
    var transactionDate: String = "",
    var personName: String = "",
    var phoneNumber: String? = null,
    var amount: Double? = null,
    var note: String? = null,
    var payDate: String? = null,
    var paid: Boolean? = null,
    var balance: Double? = null,
    var type: Int = 0,
    var currency: String? = null,
    var status: Int? = null,
    val reminder: Reminder?,
    val payment: List<Payment>?
)




