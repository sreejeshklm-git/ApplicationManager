package com.example.appblockr.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appblockr.R
import com.example.appblockr.databinding.ActivityAdminHomeBinding
import com.example.appblockr.databinding.ItemAppsLayoutBinding
import com.example.appblockr.databinding.ItemUserBinding
import com.example.appblockr.firestore.AppData
import com.example.appblockr.firestore.FireStoreManager

import com.example.appblockr.model.AppModel
import com.example.appblockr.shared.SharedPrefUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.born.applicationmanager.firestore.User

const val TAG = "AdminHomeActivity"
class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding

    private val userList: ArrayList<User> = ArrayList()
    private val fireDBAppList: ArrayList<AppData> = ArrayList()
    var installedApps: ArrayList<AppModel> = ArrayList()
    val tempList: ArrayList<AppData> = ArrayList()

    private lateinit var fireStoreManager: FireStoreManager
    private lateinit var db: FirebaseFirestore
    private lateinit var userAdapter: GenericRecyclerAdapter<User, ItemUserBinding>
    private lateinit var appsAdapter: GenericRecyclerAdapter<AppData, ItemAppsLayoutBinding>
    var userType : String = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin_home)

        fireStoreManager = FireStoreManager()
        db = fireStoreManager.fireStoreInstance
        getInstalledApps()
        if (userType == "2") {
            setupUserData()
            getUsersListFromFireDB()
        }else if (userType == "1"){
            setUpAppData()
            getAppListFromFireDB()
        }

//        GenericRecyclerAdapter
    }

    private fun getAppListFromFireDB() {
        db.collection("apps_list")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val appData = document.toObject(AppData::class.java)
                        fireDBAppList.add(appData)
                        Log.d(TAG,"App:: "+ appData.appName!!)
                        // hashMap = (Map<String, Object>) document.getData();
                        Log.d(TAG, document.id + " => " + document.data)
                    }
                    Log.d(TAG, "App Size:: " + fireDBAppList.size)
                    appsAdapter.notifyDataSetChanged()
                } else {
                    Log.w(TAG, "Error getting documents.", task.exception)
                }
            })

    }

    private fun setUpAppData() {
        binding.rvUsersList.visibility = View.GONE
        binding.rvAppsList.visibility = View.VISIBLE
        binding.rvAppsList.layoutManager = LinearLayoutManager(this)

        appsAdapter = object : GenericRecyclerAdapter<AppData, ItemAppsLayoutBinding>(tempList) {
            override fun getLayoutId(): Int {
                return R.layout.item_apps_layout
            }

            override fun onBinder(model: AppData, viewBinding: ItemAppsLayoutBinding, position: Int) {
                viewBinding.txtAppName.text = model.appName
                viewBinding.txtBundle.text = model.bundle_id
                viewBinding.switchAppLocked.isChecked = model.isAppLocked

                /*viewBinding.switchAppLocked.setOnCheckedChangeListener{_,isChecked ->
                    apps.get(position).status.let { if (isChecked) { 1 } else { 0 } }

                   // appsAdapter.notifyItemChanged(position)
                    Log.d("Position UPDATE:: ",""+apps.get(position).status)
                    updateAppsFireDB()

                }*/
            }

        }

        binding.rvAppsList.adapter = appsAdapter
    }

    private fun setupUserData() {
        binding.rvUsersList.visibility = View.VISIBLE
        binding.rvAppsList.visibility = View.GONE
        binding.rvUsersList.layoutManager = LinearLayoutManager(this)

       userAdapter = object : GenericRecyclerAdapter<User, ItemUserBinding>(userList) {
                override fun getLayoutId(): Int {
                    return R.layout.item_user
                }

                override fun onBinder(model: User, viewBinding: ItemUserBinding, position: Int) {
                    viewBinding.txtEmail.text = model.email
                    viewBinding.txtUser.text = model.user_name
                }

            }

        binding.rvUsersList.adapter = userAdapter

    }

    private fun getUsersListFromFireDB() {

        db.collection("add_users")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val users = document.toObject(
                            User::class.java
                        )
                        userList.add(users)
                        Log.d(TAG, users.email!!)
                        // hashMap = (Map<String, Object>) document.getData();
                        Log.d(TAG, document.id + " => " + document.data)
                    }
                    Log.d(TAG, "Size:: " + userList.size)
                    userAdapter.notifyDataSetChanged()
                } else {
                    Log.w(TAG, "Error getting documents.", task.exception)
                }
            })

    }

    private fun getInstalledApps() {
        val prefLockedAppList = SharedPrefUtil.getInstance(this).lockedAppsList
        /*List<ApplicationInfo> packageInfos = getPackageManager().getInstalledApplications(0);*/
        val pk =
            packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfoList = pk.queryIntentActivities(intent, 0)
        for (resolveInfo in resolveInfoList) {
            val activityInfo = resolveInfo.activityInfo
            val name = activityInfo.loadLabel(packageManager).toString()
            val icon = activityInfo.loadIcon(packageManager)
            val packageName = activityInfo.packageName
            if (!packageName.matches("com.robocora.appsift|com.android.settings")) {
                if (!prefLockedAppList.isEmpty()) {
                    //check if apps is locked
                    if (prefLockedAppList.contains(packageName)) {
                        installedApps.add(AppModel(name, icon, 1, packageName))
                    } else {
                        installedApps.add(AppModel(name, icon, 0, packageName))
                    }
                } else {
                    installedApps.add(AppModel(name, icon, 0, packageName))
                }
            } else {
                //do not add settings to app list
            }
        }
        updateAppsToFireDB()
    }

    private fun updateAppsToFireDB() {

        val dataMap: HashMap<String, List<AppData>> = HashMap()
        installedApps.forEach { appModel: AppModel ->
            val data :AppData = AppData(
                appModel.appName,appModel.packageName,"test_email","00:23","21",appModel.status==1
            )
            tempList.add(data)
        }
        dataMap.put("apps",tempList)
        db.collection("app_list").document("srinivas.p@gmail.com").set(dataMap);

    }


}

infix fun String.matches(regex: String): Boolean {
return this.contains(regex)
}

