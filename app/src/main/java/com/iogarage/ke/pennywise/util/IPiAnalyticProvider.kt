package com.iogarage.ke.pennywise.util

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

interface IPiAnalyticProvider {
    fun crashlytics(): FirebaseCrashlytics
    fun analytics(): FirebaseAnalytics

}