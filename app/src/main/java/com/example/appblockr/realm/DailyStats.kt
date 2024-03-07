package com.example.appblockr.realm

import com.example.appblockr.model.AppUsesData
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass
open class DailyStats(
    id: String,
    date: String,
    stats: String,
    deviceId: String,
    isOnline: Boolean
) : RealmObject() {

    constructor(): this("","","" ,"",false)

    @Required
    @PrimaryKey
    var id: String? = null

    @Required
    var date: String? = null

    var deviceId : String? = null

    @Required
    var stats: String? = null

    var isOnline : Boolean = false

}