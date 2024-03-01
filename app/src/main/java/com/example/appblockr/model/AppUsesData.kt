package com.example.appblockr.model

data class AppUsesData(
    var appName: String? = null,
    var bundle_id: String? = null,
    var startTime: Long? = null,
    var endTime: Long? = null,
    var usageTime: String? = null,
    var launchCount: Int? = null
)
