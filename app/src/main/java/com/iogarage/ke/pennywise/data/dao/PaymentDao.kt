package com.iogarage.ke.pennywise.data.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iogarage.ke.pennywise.domain.entity.Payment
import kotlinx.coroutines.flow.Flow
import java.util.*


@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments WHERE transactionId=:debtId")
    fun getPayments(debtId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE paymentId=:paymentId")
    fun getPayment(paymentId: Long): Flow<Payment>

    @Insert
    suspend fun addPayment(payment: Payment): Long

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Update
    suspend fun updatePayment(payment: Payment)
}