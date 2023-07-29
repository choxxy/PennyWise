package com.iogarage.ke.pennywise.views.backup

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
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.databinding.FragmentBackUpBinding
import com.iogarage.ke.pennywise.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class BackUpFragment : Fragment(R.layout.fragment_back_up) {

    private val viewModel: BackupViewModel by viewModels()
    private val binding by viewBinding(FragmentBackUpBinding::bind)

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .addOnCompleteListener {
                    Timber.d("isSuccessful ${it.isSuccessful}")
                    if (it.isSuccessful) {
                        viewModel.updateAccountInfo(it.result)
                    } else {
                        // authentication failed
                        Timber.e("exception ${it.exception}")
                        viewModel.setError(it.exception?.message)
                    }
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
            binding.progressBar.visibility = View.VISIBLE
        } else {
            // hide progress bar
            binding.progressBar.visibility = View.GONE
        }

        if (state.isUserSignIn) {
            binding.login.text = "Log Out"
            binding.sync.visibility = View.VISIBLE
        } else {
            binding.login.text = "Login In"
            binding.sync.visibility = View.GONE
            binding.image.load(R.drawable.ic_google_drive) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
            binding.lastSyncDate.text = ""
            binding.message.text = getString(R.string.desc_google_drive_login)
            binding.userDetails.text = ""
        }

        state.currentUser?.let {
            binding.userDetails.text = it.email
            binding.message.text = getString(R.string.activity_backup_drive_desc)
            binding.image.load(it.photoUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_google_drive)
                transformations(CircleCropTransformation())
            }
        }

        state.syncDate?.let {
            binding.lastSyncDate.text = it
        }

        state.error?.let {
            binding.errorMessage.text = it
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