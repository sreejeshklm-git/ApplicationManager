package com.example.appblockr.firestore

data class AppData(
    var appName: String?,
    var bundle_id: String?,
    var email: String?,
    var duration: String?,
    var clicksCount: String?,
    var isAppLocked: Boolean
) {
    constructor() : this("", "", "", "", "", false)
}
