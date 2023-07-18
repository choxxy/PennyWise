package com.iogarage.ke.pennywise.data.repository

import com.iogarage.ke.pennywise.domain.LoginRepository
import com.iogarage.ke.pennywise.domain.UserRepository
import com.iogarage.ke.pennywise.domain.entity.User
import com.iogarage.ke.pennywise.service.AccountService
import com.iogarage.ke.pennywise.util.AppPreferences
import io.appwrite.ID
import io.appwrite.models.Session
import javax.inject.Inject

class LoginRepositoryImpl
@Inject constructor(
    private val userRepository: UserRepository,
    private val accountService: AccountService,
    private val appPreferences: AppPreferences,
) : LoginRepository {
    override suspend fun authenticate(email: String, password: String): User {
        val session = accountService.authenticate(email, password)
        appPreferences.putString(AppPreferences.SESSION_ID, session.id)
        return userRepository.getUser(email)
    }

    override suspend fun signUp(
        userName: String,
        email: String,
        password: String
    ): User {

        val user = accountService.signUp(ID.unique(), userName, email, password)
        userRepository.addUser(user)
        return user
    }

    override suspend fun signOut(sessionId: String) {
        accountService.signOut(sessionId)
    }

    override suspend fun sendRecoveryEmail(email: String) {
    }

    override suspend fun createAnonymousAccount(): Session {
        val session = accountService.createAnonymousAccount()
        appPreferences.putString(AppPreferences.SESSION_ID, session.id)
        return session
    }

    override suspend fun linkAccount(email: String, password: String, userName: String): User {
        val user = accountService.linkAccount(email, password)
        userRepository.addUser(user)
        return user
    }

    override suspend fun deleteAccount() {
    }

}