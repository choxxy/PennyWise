package com.iogarage.ke.pennywise.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Realtime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppWriteModule {
    @Provides
    @Singleton
    fun appWriteClient(@ApplicationContext context: Context): Client = Client(context)
        .setEndpoint("https://appwrite.redheron.co/v1")
        .setProject("64a2d949ea9693ed3451")
        .setSelfSigned(false) // For self signed certificates, only use for development

    @Provides
    @Singleton
    fun provideAppWriteAccount(client: io.appwrite.Client) = Account(client)

    @Provides
    @Singleton
    fun provideAppWriteDatabases(client: io.appwrite.Client) = Databases(client)

    @Provides
    @Singleton
    fun provideAppWriteRealtime(client: io.appwrite.Client) = Realtime(client)
}