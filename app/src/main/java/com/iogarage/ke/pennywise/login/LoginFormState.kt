package com.iogarage.ke.pennywise.login

import androidx.annotation.StringRes

sealed class LoginFormState {
    object Pending : LoginFormState()
    object Valid : LoginFormState()
    data class UserNameError(@StringRes val error: Int) : LoginFormState()
    data class PasswordError(@StringRes val error: Int) : LoginFormState()
}