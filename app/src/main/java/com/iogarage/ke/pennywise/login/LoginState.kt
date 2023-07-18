package com.iogarage.ke.pennywise.login

import com.iogarage.ke.pennywise.domain.entity.User

sealed class LoginState {
    data class Success(val user: User) : LoginState()
    data class Error(val message: String?) : LoginState()
    object Loading : LoginState()
}