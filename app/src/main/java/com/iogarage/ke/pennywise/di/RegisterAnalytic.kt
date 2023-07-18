package com.iogarage.ke.pennywise.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.iogarage.ke.pennywise.util.AnalyticProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegisterAnalytic @Inject constructor(): AnalyticProvider {
    override fun crashlytics(): FirebaseCrashlytics {
        return Firebase.crashlytics
    }

    override fun analytics(): FirebaseAnalytics {
        return Firebase.analytics
    }
}