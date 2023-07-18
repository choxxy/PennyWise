package com.iogarage.ke.pennywise.service


import com.iogarage.ke.pennywise.domain.entity.User
import io.appwrite.models.Session

interface AccountService {
    suspend fun authenticate(email: String, password: String): Session
    suspend fun sendRecoveryEmail(email: String)
    suspend fun createAnonymousAccount(): Session
    suspend fun linkAccount(email: String, password: String): User
    suspend fun deleteAccount()
    suspend fun signUp(userId: String, userName: String, email: String, password: String): User
    suspend fun signOut(sessionId: String)
}