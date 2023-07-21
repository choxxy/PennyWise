package com.iogarage.ke.pennywise.views.backup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.iogarage.ke.pennywise.domain.TransactionRepository
import com.iogarage.ke.pennywise.service.gdrive.GoogleDriveSyncWorker
import com.iogarage.ke.pennywise.util.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class AuthState(
    val isUserSignIn: Boolean = false,
    val currentUser: GoogleSignInAccount? = null,
    val syncDate: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val transactionRepository: TransactionRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    private val gso = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    init {
        initializeSignIn()
    }

    private fun initializeSignIn() {
        viewModelScope.launch {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            appPreferences.putBoolean(AppPreferences.IS_USER_LOGGED_IN, account != null)
            _uiState.update { it.copy(currentUser = account, isUserSignIn = account != null) }
            lastSyncDate()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    fun updateAccountInfo(account: GoogleSignInAccount?) {
        _uiState.update { it.copy(currentUser = account, isUserSignIn = account != null) }
    }

    fun performLogout() {
        appPreferences.remove(AppPreferences.LAST_SYNC_TIME)
        googleSignInClient.signOut()
        initializeSignIn()
    }

    private fun lastSyncDate() {
        val lstSyncTime = appPreferences.getString(AppPreferences.LAST_SYNC_TIME)
        _uiState.update { it.copy(syncDate = lstSyncTime) }
    }

    fun syncNow() {

        viewModelScope.launch {

            if (isUserLoggedIn()) {
                val workManager = WorkManager.getInstance(context)
                // User is logged in, schedule the background operation
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build()
                val work = OneTimeWorkRequest.Builder(GoogleDriveSyncWorker::class.java)
                        .setConstraints(constraints).build()
                workManager.getWorkInfoByIdLiveData(work.id).observeForever { workInfo ->
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            _uiState.update { it.copy(isLoading = false) }
                            lastSyncDate()
                        }

                        WorkInfo.State.FAILED -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    syncDate = "Last sync was failed"
                                )
                            }
                        }

                        else -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    syncDate = "Sync is in progress"
                                )
                            }
                        }
                    }
                }
                workManager.enqueue(work)
            }

        }
    }


}