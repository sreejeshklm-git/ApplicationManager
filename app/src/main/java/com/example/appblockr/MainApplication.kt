package com.example.appblockr

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        val config : RealmConfiguration = RealmConfiguration.Builder()
            .name("stats.db")
            .allowWritesOnUiThread(true)
            .deleteRealmIfMigrationNeeded()
            .build()

        Realm.setDefaultConfiguration(config)
    }
}