package com.iogarage.ke.pennywise.service.gdrive

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.service.Resource
import com.iogarage.ke.pennywise.util.AppPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


@HiltWorker
class GoogleDriveSyncWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.US)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerProviderEntryPoint {
        fun driveManager(): DriveManager
        fun preference(): AppPreferences
    }

    override suspend fun doWork(): Result {
        val entryPoint =
            EntryPointAccessors.fromApplication(context, WorkerProviderEntryPoint::class.java)
        val driveManager = entryPoint.driveManager()
        val preference = entryPoint.preference()
        val info = createForegroundInfo(context.getString(R.string.sync_started))
        setForeground(info)
        var status = Resource.Status.NONE
        driveManager.doSync().collect {
            it.data?.let { progress ->
                status = it.status
            }
            if (it.status == Resource.Status.SUCCESS) {
                val date = dateFormat.format(Date())
                preference.putString(AppPreferences.LAST_SYNC_TIME, date)
            }
        }

        while (status == Resource.Status.NONE || status == Resource.Status.LOADING) {
            Timber.d("Wait at driver sync")
            delay(1000)
        }
        return if (status == Resource.Status.SUCCESS) {
            Result.success()
        } else {
            Result.failure()
        }
    }
}