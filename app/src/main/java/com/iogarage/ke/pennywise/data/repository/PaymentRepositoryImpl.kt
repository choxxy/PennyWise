package com.iogarage.ke.pennywise.data.repository

import com.iogarage.ke.pennywise.data.dao.PaymentDao
import com.iogarage.ke.pennywise.domain.PaymentRepository
import com.iogarage.ke.pennywise.domain.entity.Payment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class PaymentRepositoryImpl @Inject constructor(
    private val paymentDao: PaymentDao,
) : PaymentRepository {

    override fun getPayments(debtId: Long): Flow<List<Payment>> {
        return paymentDao.getPayments(debtId)
    }

    override fun getPayment(paymentId: Long): Flow<Payment> {
        return paymentDao.getPayment(paymentId)
    }

    override suspend fun deletePayment(payment: Payment) =
        paymentDao.deletePayment(payment)

    override suspend fun updatePayment(payment: Payment) =
        paymentDao.updatePayment(payment)

    override suspend fun addPayment(payment: Payment): Long =
        paymentDao.addPayment(payment)
}

