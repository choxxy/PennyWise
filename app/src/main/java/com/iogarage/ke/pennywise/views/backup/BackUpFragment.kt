package com.iogarage.ke.pennywise.views.backup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.databinding.FragmentBackUpBinding
import com.iogarage.ke.pennywise.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BackUpFragment : Fragment(R.layout.fragment_back_up) {

    private val viewModel: BackupViewModel by viewModels()
    private val binding by viewBinding(FragmentBackUpBinding::bind)

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Handle the result from the activity
                val data: Intent? = result.data
                // Process the data here
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun updateUi(state: AuthState) {
        if (state.isLoading) {
            // show progress bar
        } else {
            // hide progress bar
        }

        if (state.isUserSignIn) {
            binding.login.text = "Log Out"
            binding.sync.visibility = View.VISIBLE
        } else {
            binding.login.text = "Login In"
            binding.sync.visibility = View.GONE
        }

        state.currentUser?.let {
            binding.userEmail.text = it.email
            binding.username.text = it.displayName
        }

        state.syncDate?.let {
            binding.lastSyncDate.text = it
        }

        binding.login.setOnClickListener {
            if (state.isUserSignIn)
                performLogOut()
            else
                performLogin()
        }

        binding.sync.setOnClickListener {
            viewModel.syncNow()
        }

    }

    private fun performLogOut() {
        viewModel.performLogout()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_back_up, container, false)
    }

    private fun performLogin() {
        val signInIntent = viewModel.googleSignInClient.signInIntent
        startForResult.launch(signInIntent)
    }

}