package com.iogarage.ke.pennywise

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.iogarage.ke.pennywise.service.Session
import com.iogarage.ke.pennywise.util.AppPreferences
import com.iogarage.ke.pennywise.util.LogTree
import com.iogarage.ke.pennywise.util.PiTracker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * Created by Joshua on 2/8/2015.
 */
@HiltAndroidApp
class PennyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var preference: AppPreferences

    @Inject
    lateinit var logTree: LogTree

    @Inject
    lateinit var session: Session

    @Inject
    lateinit var piTracker: PiTracker

    override fun onCreate() {
        super.onCreate()

        Timber.plant(logTree)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        session.appName = applicationContext.getString(R.string.app_name)
        session.packageName = BuildConfig.APPLICATION_ID
        session.appVersion = BuildConfig.VERSION_NAME
        session.buildNumber = BuildConfig.VERSION_CODE.toString()
        var deviceId = preference.getString(AppPreferences.UUID)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            preference.putString(AppPreferences.UUID, deviceId)
        }
        session.uuid = deviceId
        piTracker.trackUserProperty(mapOf(AppPreferences.UUID to deviceId))
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder().setWorkerFactory(workerFactory).build()
}