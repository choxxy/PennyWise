package com.iogarage.ke.pennywise.domain

import com.iogarage.ke.pennywise.domain.entity.User
import io.appwrite.models.Session


interface LoginRepository {
    suspend fun authenticate(email: String, password: String): User
    suspend fun sendRecoveryEmail(email: String)
    suspend fun createAnonymousAccount(): Session
    suspend fun linkAccount(
        email: String,
        password: String,
        userName: String
    ): User

    suspend fun deleteAccount()
    suspend fun signUp(
        userName: String,
        email: String,
        password: String
    ): User

    suspend fun signOut(sessionId: String)
}

