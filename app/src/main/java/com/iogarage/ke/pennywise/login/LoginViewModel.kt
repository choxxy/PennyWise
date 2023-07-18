package com.iogarage.ke.pennywise.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.domain.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginFormState = MutableStateFlow<LoginFormState>(LoginFormState.Pending)
    val loginFormState: StateFlow<LoginFormState> = _loginFormState

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Loading)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {

        viewModelScope.launch {
            val user = loginRepository.authenticate(email, password)
            _loginState.value = LoginState.Success(user)
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginFormState.value = LoginFormState.UserNameError(R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginFormState.value = LoginFormState.PasswordError(R.string.invalid_password)
        } else {
            _loginFormState.value = LoginFormState.Valid
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}