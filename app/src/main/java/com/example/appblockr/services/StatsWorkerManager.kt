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
import com.example.appblockr.model.UsesStatsDataModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StatsWorkerManager(
    val context: Context,
    private val workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private lateinit var usersEmail: String
    private var isDateAvailable: Boolean? = false
    lateinit var db: FirebaseFirestore
    private var statsDataModel: UsesStatsDataModel? = null
    private var appLaunchCountPerDay: HashMap<String, Int> = HashMap()


    override suspend fun doWork(): Result {
        val inputData = inputData
        usersEmail = inputData.getString("KEY_USER_EMAIL").toString()
        db = FirebaseFirestore.getInstance()

        uploadStatsToFireStoreDb()

        return Result.success()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SimpleDateFormat")
    private fun uploadStatsToFireStoreDb() {

        val sdf = SimpleDateFormat("dd:MM:yyyy")
        val currentDate = sdf.format(Date())
        println(" ##C-DATE ::  $currentDate")

        GlobalScope.launch {
            Log.d("##StatsFromFirebase", "Coroutine started")
            var firebaseStats: ArrayList<StatsModel>? = ArrayList<StatsModel>()
            val result1 = async {
                firebaseStats = getStatsFromFirebaseDb()
            }
            result1.await()
            Log.d("##StatsFromFirebase", "firebaseStats:: " + firebaseStats.isNullOrEmpty())


            firebaseStats.let {
                it?.forEach { statsObj ->
                    Log.d("##StatsFromFirebase", "forEach:: " + statsObj.date)

                    if (statsObj.date.contains(currentDate)) {
                        isDateAvailable = true
                    }
                }
            }
            if (isDateAvailable == true) {
                //do not insert daily stats in to firebase Db
                Log.d("##StatsFromFirebase", "" + isDateAvailable)
            } else {
                var dailyStats: ArrayList<AppUsesData> = ArrayList()
                val result = async {

                    Log.d("##StatsFromFirebase", "Async started")
                    dailyStats = getAppUsageData()
                    Log.d("##StatsFromFirebase", "dailyStats::" + dailyStats.size)
                }
                result.await()
                Log.d("##StatsFromFirebase", "result await ended")


                if (statsDataModel == null) {
                    Log.d("##StatsFromFirebase", "statsDataModel is null")
                    val latestStats = ArrayList<StatsModel>()
                    latestStats.add(StatsModel(currentDate, dailyStats))
                    statsDataModel = UsesStatsDataModel(usersEmail,latestStats)

                    statsDataModel?.let {
                        db.collection("app_stats").document(usersEmail).set(it)
                    }

                } else {
                    Log.d("##StatsFromFirebase", "statsDataModel is not null")
                    Log.d("##StatsFromFirebase", "Email:: " + statsDataModel?.email)
                    Log.d(
                        "##StatsFromFirebase",
                        "Root Size:: " + statsDataModel?.dataArrayList?.size
                    )

                    val model = StatsModel(currentDate, dailyStats)
                    val list: ArrayList<StatsModel> = ArrayList()
                    if (statsDataModel?.dataArrayList.isNullOrEmpty()) {
                        statsDataModel?.dataArrayList = ArrayList<StatsModel>()
                        Log.d("##StatsFromFirebase", "List is null form fireDb")
                    } else {
                        statsDataModel?.dataArrayList?.let { list.addAll(it) }
                    }
                    list.add(model)
                    val usageStats = UsesStatsDataModel(usersEmail, list)
                    usageStats.email?.let { Log.d("##StatsFromFirebase", it) }

                    usageStats.let {
                        db.collection("app_stats").document(usersEmail).set(it)
                    }
                }

            }


        }

    }

    private suspend fun getStatsFromFirebaseDb(): ArrayList<StatsModel>? {

        val dayWiseStatsList: ArrayList<StatsModel> = ArrayList()
        val docRef: DocumentReference = db.collection("app_stats").document(usersEmail);
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                try {
                    if (document.exists()) {
                        statsDataModel =
                            document.toObject<UsesStatsDataModel>(UsesStatsDataModel::class.java)
                        statsDataModel?.let {
                            it.dataArrayList?.let { it1 ->
                                dayWiseStatsList.addAll(it1)
                                Log.d("##StatsFromFirebase", "DocRef:: " + dayWiseStatsList.size)
                            }
                        }

                    } else {
                        //statsDataModel = UsesStatsDataModel()
                        Log.d("##StatsFromFirebase", "Doc not exist")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Log.i("TAG", "get failed with ", task.exception)
            }
        }
        Log.d("##StatsFromFirebase",""+dayWiseStatsList.size)
        return dayWiseStatsList
    }

    fun getAppUsageData(): ArrayList<AppUsesData> {
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


}