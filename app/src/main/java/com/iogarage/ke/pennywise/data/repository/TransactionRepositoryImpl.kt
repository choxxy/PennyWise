package com.iogarage.ke.pennywise.data.repository

import com.iogarage.ke.pennywise.data.dao.DebtDao
import com.iogarage.ke.pennywise.domain.TransactionRepository
import com.iogarage.ke.pennywise.domain.entity.Transaction
import com.iogarage.ke.pennywise.domain.entity.TransactionWithPayments
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class TransactionRepositoryImpl @Inject constructor(
    private val debtDao: DebtDao,
) : TransactionRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
       return debtDao.getDebts()
    }

    override suspend fun getTransaction(id: Long): Transaction {
        return debtDao.getDebt(id)
    }

    override  fun getDebtWithPayments(id: Long): Flow<TransactionWithPayments> {
        return debtDao.getDebtWithPayments(id)
    }

    override suspend fun getAllDebtWithPayments(): List<TransactionWithPayments> {
        return debtDao.getAllDebtWithPayments()
    }

    override suspend fun insertTransactionWithPayments(transactions : TransactionWithPayments) {
       return  debtDao.insertTransactionWithPayments(transactions)
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return  debtDao.insert(transaction)
    }

    override suspend fun deleteDebt(transaction: Transaction) =
        debtDao.deleteDebt(transaction)

    override suspend fun updateDebt(transaction: Transaction) =
        debtDao.updateDebt(transaction)

}

