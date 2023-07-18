package com.iogarage.ke.pennywise.service.gdrive

import com.iogarage.ke.pennywise.domain.entity.TransactionWithPayments

data class Tables(
    val lstOfTransactions: List<TransactionWithPayments>? = null
)