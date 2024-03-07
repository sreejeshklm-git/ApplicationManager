package com.example.appblockr.services

import android.annotation.SuppressLint
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.appblockr.model.AppUsesData
import com.example.appblockr.model.StatsModel
import com.example.appblockr.realm.DailyStats
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import io.realm.Realm
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatsWorkerManager(
    val context: Context,
    private val workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private lateinit var usersEmail: String
    private var isDateAvailable: Boolean? = false
    lateinit var db: FirebaseFirestore
//    private var statsDataModel: UsesStatsDataModel? = null
    private var appLaunchCountPerDay: HashMap<String, Int> = HashMap()

    private lateinit var realm : Realm;


    override suspend fun doWork(): Result {
        val inputData = inputData
        usersEmail = inputData.getString("KEY_USER_EMAIL").toString()
        db = FirebaseFirestore.getInstance()

        realm = Realm.getDefaultInstance()
        uploadStatsToFireStoreDb()

        return Result.success()
    }


    @SuppressLint("SimpleDateFormat")
    private fun uploadStatsToFireStoreDb() {

        val sdf = SimpleDateFormat("dd:MM:yyyy")
        val currentDate = sdf.format(Date())
        println(" ##C-DATE ::  $currentDate")

        GlobalScope.launch {
            Log.d("##StatsFromFirebase", "Coroutine started")

            var dailyStats: ArrayList<AppUsesData> = ArrayList()
            val result = async {

                Log.d("##StatsFromFirebase", "Async started")
                dailyStats = getAppUsageData()
                Log.d("##StatsFromFirebase", "dailyStats::" + dailyStats.size)
            }
            result.await()
            Log.d("##StatsFromFirebase", "result await ended")

            //pushing daily stats in to Firebase Database
            db.collection(usersEmail).document(currentDate).set(StatsModel(dailyStats))

            //RealM Database
            val stats = DailyStats("1", currentDate, Gson().toJson(dailyStats), "", false)
            //realm.insert(stats)

        }

    }


    private fun getAppUsageData(): ArrayList<AppUsesData> {
        var usageStats: List<UsageStats>? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            usageStats = getAppUsageStats(context = context) as List<UsageStats>?
        }
        val appUsageList: MutableList<AppUsesData> = ArrayList<AppUsesData>()
        assert(usageStats != null)
        for (usageStat in usageStats!!) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (isOSApp(usageStat.packageName, context)) {
                    if (usageStat.totalTimeInForeground != 0L) {
                        val packageName = usageStat.packageName
                        val appName: String = getAppNameFromPackage(packageName)

                        val startTime = usageStat.lastTimeUsed
                        val endTime = usageStat.lastTimeStamp
                        val usageTime: String = getTime(usageStat.totalTimeInForeground)
                        val launchCount: Int = incrementAppLaunchCount(packageName)

                        val appUsage = AppUsesData(
                            appName,
                            packageName,
                            startTime,
                            endTime,
                            usageTime,
                            launchCount
                        )
                        appUsageList.add(appUsage)
                        println("AppUsageList$appUsageList")

                    }
                }
            }
        }
        return appUsageList as ArrayList<AppUsesData>
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun getAppUsageStats(context: Context): List<UsageStats?>? {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * 60 * 60 * 1000 // 24 hours ago
        var usageStatsManager: UsageStatsManager? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        }
        assert(usageStatsManager != null)
        return usageStatsManager!!.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
    }

    private fun isOSApp(packageName: String, context: Context): Boolean {
        val packageManager = context.packageManager
        return try {
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            ).applicationInfo
            packageInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getAppNameFromPackage(packageName: String): String {
        val packageManager: PackageManager = context.packageManager
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    private fun getTime(totalTimeInForeground: Long): String {
        val seconds = totalTimeInForeground / 1000 % 60
        val hh = seconds / 3600
        val mm = seconds % 3600 / 60
        val ss = seconds % 60
        return "$hh:$mm:$ss"
    }

    private fun incrementAppLaunchCount(packageName: String): Int {
        var launchCount = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            launchCount = appLaunchCountPerDay.getOrDefault(packageName, 0)
        }
        appLaunchCountPerDay.put(packageName, launchCount + 1)
        return launchCount + 1
    }

    private fun getUserDocList(){
        db.collection(usersEmail).get().addOnSuccessListener { doc ->
            for (document in doc){
                Log.d("##getUserDocList",doc.toString())
            }
        }
    }


}