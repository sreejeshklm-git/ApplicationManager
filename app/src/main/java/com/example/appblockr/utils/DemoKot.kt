package com.example.appblockr.utils

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.appblockr.model.AppData

import java.text.SimpleDateFormat
import java.util.Calendar


class DemoKot {
    companion object {
        private val dateFormat = SimpleDateFormat("M-d-yyyy HH:mm:ss")
        val TAG = DemoKot::class.java.simpleName
        private val appLaunchCountPerDay = HashMap<String, Int>()
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun getStats(context: Context) {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val interval = UsageStatsManager.INTERVAL_YEARLY
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.YEAR, -1)
            val startTime = calendar.timeInMillis
            Log.d(TAG, "Range start:" + dateFormat.format(startTime))
            Log.d(TAG, "Range end:" + dateFormat.format(endTime))
            val uEvents = usm.queryEvents(startTime, endTime)
            while (uEvents.hasNextEvent()) {
                val e = UsageEvents.Event()
                uEvents.getNextEvent(e)
                if (e != null) {
                    Log.d(TAG, "Event: " + e.packageName + "\t" + e.timeStamp)
                }
            }
        }
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun getUsageStatsList(context: Context): List<UsageStats> {
            val usm = getUsageStatsManager(context)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -1)
            val startTime = calendar.timeInMillis
            Log.d(TAG, "Range start:" + dateFormat.format(startTime))
            Log.d(TAG, "Range end:" + dateFormat.format(System.currentTimeMillis()))
            return usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, System.currentTimeMillis())
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun printUsageStats(usageStatsList: List<UsageStats>): List<UsageStats> {
            for (u in usageStatsList) {
                Log.d(
                    TAG, "Pkg: " + u.packageName + "\t" + "ForegroundTime: "
                            + u.totalTimeInForeground
                )
            }
            return usageStatsList
        }

        private fun getAppIcon(packageName: String, context: Context): Drawable? {
            var icon: Drawable? = null
            try {
                icon = context.packageManager
                    .getApplicationIcon(packageName)
                return icon
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return icon
        }


        @RequiresApi(Build.VERSION_CODES.Q)
        fun printCurrentUsageStatus(
            context: Context,
            appDataList: java.util.ArrayList<AppData>,
            usersEmail: String
        ): java.util.ArrayList<AppData> {
            var list = ArrayList<AppData>()
              if(checkUsageStatsPermission(context)){
                  list =getData(context, appDataList, usersEmail)
              }else{
                  requestUsageStatsPermission(context)
                list = getData(context, appDataList, usersEmail)
              }

            return list
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        fun getData(context: Context, appDataList: ArrayList<AppData>, usersEmail: String): ArrayList<AppData> {
            val list = ArrayList<AppData>()
            val usesStatsList = printUsageStats(getUsageStatsList(context))
            if (usesStatsList.isNotEmpty()) {
                for ( u in usesStatsList) {
                    if (isOSApp(u.packageName, context)) {
                        if (u.totalTimeInForeground.toInt() != 0) {
                            val appData = AppData()
                            appData.appName = getAppNameFromPackageName(u.packageName, context)
                            appData.clicksCount = incrementAppLaunchCount(u.packageName).toString()
                            appData.duration = getTime(u.totalTimeVisible)
                            appData.email = usersEmail
                            appData.bundle_id = u.packageName
                            appData.isAppLocked = false
//                            if (appDataList.isNotEmpty()) {
//                               for (i in appDataList) {
//                                   if (appData.appName.equals(i.appName)) {
//                                       appData.isAppLocked = i.isAppLocked
//                                   } else {
//                                       appData.isAppLocked = false
//                                   }
//                               }
//                            } else {
//                                appData.isAppLocked = false
//                            }
                            list.add(appData)
                        }
                    }
                }
            }
            return list
        }


        @RequiresApi(Build.VERSION_CODES.O)
        private fun getTime(totalTimeInForeground: Long): String {
            val seconds = totalTimeInForeground / 1000 % 60
            val hh = seconds / 3600
            val mm = seconds % 3600 / 60
            val ss = seconds % 60
            return "$hh:$mm:$ss"
        }

        private fun isOSApp(packageName: String, context: Context): Boolean {
            val packageManager = context.packageManager
            return try {
                val packageInfo =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
        private fun getAppNameFromPackageName(packageName: String, context: Context): String {
            val packageManager = context.packageManager
            return try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(applicationInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                packageName
            }
        }
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun getUsageStatsManager(context: Context): UsageStatsManager {
            return context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        }
        private fun checkUsageStatsPermission(context: Context): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        }

        private fun requestUsageStatsPermission(context: Context) {
            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            // Inform the user to grant permission and handle it appropriately
        }

        private fun incrementAppLaunchCount(packageName: String): Int {
            var launchCount = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                launchCount = appLaunchCountPerDay.getOrDefault(packageName, 0)
            }
            appLaunchCountPerDay[packageName] = launchCount + 1
            return launchCount + 1
        }

    }
}