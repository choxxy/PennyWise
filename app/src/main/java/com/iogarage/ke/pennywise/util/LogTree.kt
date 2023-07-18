package com.iogarage.ke.pennywise.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogTree @Inject constructor() : Timber.Tree() {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        //super.log(priority, tag, message, t)
    }

    override fun e(message: String?, vararg args: Any?) {
        message?.let {
            crashlytics.recordException(Throwable(it))
        }
    }

    override fun e(t: Throwable?) {
        t?.let {
            crashlytics.recordException(t)
        }
    }
}