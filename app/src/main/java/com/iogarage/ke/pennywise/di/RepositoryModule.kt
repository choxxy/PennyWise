package com.iogarage.ke.pennywise.di

import com.iogarage.ke.pennywise.data.repository.PaymentRepositoryImpl
import com.iogarage.ke.pennywise.data.repository.TransactionRepositoryImpl
import com.iogarage.ke.pennywise.domain.PaymentRepository
import com.iogarage.ke.pennywise.domain.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindDebtRepository(debtRepositoryImpl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    abstract fun bindPaymentRepository(paymentRepository: PaymentRepositoryImpl): PaymentRepository
}
