package com.iogarage.ke.pennywise.service

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Session @Inject constructor() {
    var appName: String? = null
    var packageName: String? = null
    var appVersion: String? = null
    var buildNumber: String? = null
    var uuid: String? = null
}
