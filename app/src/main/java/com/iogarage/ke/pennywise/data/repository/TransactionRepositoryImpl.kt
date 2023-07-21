package com.iogarage.ke.pennywise.data.repository

import com.iogarage.ke.pennywise.data.dao.TransactionDao
import com.iogarage.ke.pennywise.domain.TransactionRepository
import com.iogarage.ke.pennywise.domain.entity.Transaction
import com.iogarage.ke.pennywise.domain.entity.TransactionWithPayments
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
       return transactionDao.getDebts()
    }

    override suspend fun getTransaction(id: Long): Transaction {
        return transactionDao.getTransaction(id)
    }

    override  fun getDebtWithPayments(id: Long): Flow<TransactionWithPayments> {
        return transactionDao.getDebtWithPayments(id)
    }

    override suspend fun getAllDebtWithPayments(): List<TransactionWithPayments> {
        return transactionDao.getAllDebtWithPayments()
    }

    override suspend fun insertTransactionWithPayments(transactions : TransactionWithPayments) {
       return  transactionDao.insertTransactionWithPayments(transactions)
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return  transactionDao.insert(transaction)
    }

    override suspend fun deleteDebt(transaction: Transaction) =
        transactionDao.deleteDebt(transaction)

    override suspend fun updateDebt(transaction: Transaction) =
        transactionDao.updateDebt(transaction)

}

