

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.appblockr.model.AppData


import java.text.SimpleDateFormat
import java.util.Calendar


class UsesStats {

    companion object {
        private val dateFormat = SimpleDateFormat("M-d-yyyy HH:mm:ss")
        val TAG = UsesStats::class.java.simpleName
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
        fun printCurrentUsageStatus(context: Context): MutableList<AppData> {
            val list = mutableListOf<AppData>()
            val usesStatsList = printUsageStats(getUsageStatsList(context))
            if (usesStatsList.isNotEmpty()) {
                for ( u in usesStatsList) {
                    if (isOSApp(u.packageName, context)) {
                        if (u.totalTimeInForeground.toInt() != 0) {
                            val appData = AppData()
                            appData.appName = getAppNameFromPackageName(u.packageName, context)
                            appData.clicksCount = ""
                            appData.duration = getTime(u.totalTimeVisible)
                            appData.email = ""
                            appData.bundle_id = getAppNameFromPackageName(u.packageName, context)
                            appData.isAppLocked = false
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
    }

}