package com.iogarage.ke.pennywise.domain

import com.iogarage.ke.pennywise.domain.entity.Transaction
import com.iogarage.ke.pennywise.domain.entity.TransactionWithPayments
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(): Flow<List<Transaction>>
    suspend fun getTransaction(id: Long): Transaction
    suspend fun deleteDebt(transaction: Transaction)
    suspend fun updateDebt(transaction: Transaction)
    fun getDebtWithPayments(id: Long): Flow<TransactionWithPayments>

    suspend fun getAllDebtWithPayments(): List<TransactionWithPayments>
    suspend fun insertTransactionWithPayments(transactions: TransactionWithPayments)

    suspend fun insertTransaction(transactions: Transaction):Long
}