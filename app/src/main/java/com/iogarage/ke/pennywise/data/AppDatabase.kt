package com.iogarage.ke.pennywise.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iogarage.ke.pennywise.data.dao.TransactionDao
import com.iogarage.ke.pennywise.data.dao.PaymentDao
import com.iogarage.ke.pennywise.domain.entity.Payment
import com.iogarage.ke.pennywise.domain.entity.Transaction

@Database(
    entities = [Transaction::class, Payment::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun debtDao(): TransactionDao
    abstract fun paymentDao(): PaymentDao
}