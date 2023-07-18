package com.iogarage.ke.pennywise.domain

import com.iogarage.ke.pennywise.domain.entity.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun getPayments(debtId: Long): Flow<List<Payment>>
    fun getPayment(paymentId: Long):Flow<Payment>
    suspend fun deletePayment(payment: Payment)
    suspend fun updatePayment(payment: Payment)
    suspend fun addPayment(payment: Payment): Long
}