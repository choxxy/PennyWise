package com.iogarage.ke.pennywise.di

import android.content.Context
import androidx.room.Room
import com.iogarage.ke.pennywise.data.AppDatabase
import com.iogarage.ke.pennywise.data.dao.DebtDao
import com.iogarage.ke.pennywise.data.dao.PaymentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "penny-wise")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDebtDao(database: AppDatabase): DebtDao {
        return database.debtDao()
    }


    @Provides
    @Singleton
    fun providePaymentDao(database: AppDatabase): PaymentDao {
        return database.paymentDao()
    }
}