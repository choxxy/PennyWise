package com.iogarage.ke.pennywise.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.iogarage.ke.pennywise.domain.entity.Payment
import com.iogarage.ke.pennywise.domain.entity.TransactionWithPayments
import kotlinx.coroutines.flow.Flow
import com.iogarage.ke.pennywise.domain.entity.Transaction as Trx

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions")
    fun getDebts(): Flow<List<Trx>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTransaction(transaction: List<Trx>)

    @Delete
    suspend fun deleteDebt(transaction: Trx)

    @Update
    suspend fun updateDebt(transaction: Trx)

    @Query("SELECT * FROM transactions WHERE transactionId=:transactionId")
    suspend fun getTransaction(transactionId: Long): Trx

    /**
     * This query will tell Room to query both the [Transaction] and [Payment] tables and handle
     * the object mapping.
     */
    @Transaction
    @Query("SELECT * FROM transactions WHERE transactionId=:transactionId")
    fun getDebtWithPayments(transactionId: Long): Flow<TransactionWithPayments>

    @Transaction
    @Query("SELECT * FROM transactions")
    fun getAllDebtWithPayments(): List<TransactionWithPayments>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Trx): Long

    @Transaction
    suspend fun insertTransactionWithPayments(transactions: TransactionWithPayments) {
        val transactionId = insert(transactions.transaction)
        // Update payment transactionId and paymentId then insert
        for (payment in transactions.payments) {
            payment.paymentId = 0L
            payment.transactionId = transactionId
            insert(payment)
        }
    }
}

