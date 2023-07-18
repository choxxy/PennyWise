package com.iogarage.ke.pennywise.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.iogarage.ke.pennywise.databinding.ActivityLogin2Binding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var binding: ActivityLogin2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.loginFormState.collect { uiState ->
                    when (uiState) {
                        LoginFormState.Valid -> {
                            enableLoginButton()
                        }

                        is LoginFormState.PasswordError -> {
                            showPasswordError(getString(uiState.error))
                        }

                        LoginFormState.Pending -> {}
                        is LoginFormState.UserNameError -> {
                            showUserNameError(getString(uiState.error))
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.loginState.collect { uiState ->
                    when (uiState) {
                        LoginState.Loading -> {
                            showProgressBar()
                        }

                        is LoginState.Success -> {
                            navigate()
                        }

                        is LoginState.Error -> {
                            hideProgressBar()
                            showError(uiState.message)
                        }
                    }
                }
            }
        }

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }
    }

    private fun hideProgressBar() {
        TODO("Not yet implemented")
    }

    private fun showProgressBar() {
        TODO("Not yet implemented")
    }

    private fun navigate() {
        TODO("Not yet implemented")
    }

    private fun showUserNameError(string: String) {

    }

    private fun showPasswordError(string: String) {

    }

    private fun enableLoginButton() {
        TODO("Not yet implemented")
    }


    private fun showError(message: String?) {

    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}