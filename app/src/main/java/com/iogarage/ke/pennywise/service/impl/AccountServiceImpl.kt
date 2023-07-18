package com.iogarage.ke.pennywise.service.impl

import com.iogarage.ke.pennywise.domain.entity.User
import com.iogarage.ke.pennywise.service.AccountService
import com.iogarage.ke.pennywise.service.trace
import io.appwrite.models.Session
import io.appwrite.services.Account
import javax.inject.Inject

class AccountServiceImpl @Inject constructor(
    private val account: Account
) : AccountService {
    override suspend fun authenticate(email: String, password: String): Session {
        return account.createEmailSession(
            email = email,
            password = password
        )
    }

    override suspend fun signUp(
        userId: String,
        userName: String,
        email: String,
        password: String
    ): User {
        val response = account.create(
            userId = userId,
            email = email,
            password = password,
            name = userName
        )
        return User(
            id = response.id,
            email = response.email,
            userName = response.name
        )
    }

    override suspend fun signOut(sessionId: String) {
        account.deleteSession(sessionId)
    }

    override suspend fun sendRecoveryEmail(email: String) {
    }

    override suspend fun createAnonymousAccount(): Session {
        return account.createAnonymousSession()
    }

    override suspend fun linkAccount(email: String, password: String): User =
        trace(LINK_ACCOUNT_TRACE) {
            val response = account.updateEmail(email, password)
            User(
                id = response.id,
                email = response.email,
                userName = response.name
            )
        }

    override suspend fun deleteAccount() {
    }

    companion object {
        private const val LINK_ACCOUNT_TRACE = "linkAccount"
    }
}