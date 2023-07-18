package com.iogarage.ke.pennywise.di

import com.iogarage.ke.pennywise.service.AccountService
import com.iogarage.ke.pennywise.service.impl.AccountServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    abstract fun provideAccountService(impl: AccountServiceImpl): AccountService

}
