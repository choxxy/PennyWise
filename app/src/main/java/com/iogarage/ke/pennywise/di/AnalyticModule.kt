package com.iogarage.ke.pennywise.di

import com.iogarage.ke.pennywise.util.IPiAnalyticProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticModule {

    @Singleton
    @Provides
    fun getAnalyticsProvider(piGiftRegisterAnalytic: PiGiftRegisterAnalytic): IPiAnalyticProvider {
        return piGiftRegisterAnalytic
    }
}