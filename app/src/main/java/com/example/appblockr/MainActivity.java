package com.example.appblockr;

import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appblockr.adapter.AppListAdapter;
import com.example.appblockr.adapter.LockedAppAdapter;
import com.example.appblockr.model.AppData;
import com.example.appblockr.model.AppModel;
import com.example.appblockr.model.AppUsesData;
import com.example.appblockr.model.ApplicationListModel;
import com.example.appblockr.model.StatsModel;
import com.example.appblockr.model.UsesStatsDataModel;
import com.example.appblockr.services.BackgroundManager;
import com.example.appblockr.services.ForegroundService;
import com.example.appblockr.services.MyAccessibilityService;
import com.example.appblockr.shared.SharedPrefUtil;
import com.example.appblockr.ui.stats.UsesStatsActivity;
import com.example.appblockr.utils.DemoKot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AppListAdapter.ToggleCheckedListener {

    private final String TAG = "MainActivity";
    static List<AppModel> lockedAppsList = new ArrayList<>();
    static Context context;
    private static final int ACCESSIBILITY_SERVICE_REQUEST = 101;
    ImageView allAppsBtn;
    List<AppModel> allInstalledApps = new ArrayList<>();
    LockedAppAdapter lockedAppsAdapter = new LockedAppAdapter(lockedAppsList, context);
    RecyclerView recyclerView;
//    LockedAppAdapter adapter;
    Button setScheduleBtn;
    ProgressDialog progressDialog;
    LinearLayout emptyLockListInfo, blockingInfoLayout;
    RelativeLayout enableUsageAccess, enableOverlayAccess,accessServiceLayout;
    TextView btnEnableUsageAccess,btnEnableAS, btnEnableOverlay,blockingScheduleDescription,scheduleMode ;
    ImageView checkBoxOverlay, checkBoxUsage,checkedASIcon;


    private String usersEmail;
    private FirebaseFirestore db;
    private AppListAdapter adapter;
    ArrayList<AppData> appsListFromFireDb;
    ArrayList<AppUsesData> appUsesDataArrayList;
    ArrayList<StatsModel> statsModelArrayList;
    ArrayList<AppModel> installedAppsList;
    ArrayList<AppData> commonList;
    ArrayList<String> lockedApps;
    SharedPrefUtil prefUtil;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(" Locked Apps");
        setTheme(R.style.Theme_Appsift);
        setContentView(R.layout.activity_main);
        usersEmail = getIntent().getStringExtra("email");
        appsListFromFireDb = new ArrayList<AppData>();
        appUsesDataArrayList = new ArrayList<AppUsesData>();
        statsModelArrayList = new ArrayList<StatsModel>();
        installedAppsList = new ArrayList<AppModel>();
        commonList = new ArrayList<AppData>();
        lockedApps = new ArrayList<String>();

        db = FirebaseFirestore.getInstance();

        prefUtil = new SharedPrefUtil(getApplicationContext());

      // BackgroundManager.getInstance().init(this).startService();
        //BackgroundManager.getInstance().init(this).startAlarmManager();
        ContextCompat.startForegroundService(this, new Intent(this, ForegroundService.class));

        //   BackgroundManager.getInstance().init(this).startService();
       // BackgroundManager.getInstance().init(this).startAlarmManager();
        addIconToBar();
        progressDialog = new ProgressDialog(this);
        emptyLockListInfo = findViewById(R.id.emptyLockListInfo);
        allAppsBtn = findViewById(R.id.all_apps_button_img);
        enableOverlayAccess = findViewById(R.id.permissionsBoxDisplay);
        enableUsageAccess = findViewById(R.id.permissionsBoxUsage);

        btnEnableOverlay = findViewById(R.id.enableStatusDisplay);
        btnEnableUsageAccess = findViewById(R.id.enableStatusUsage);

        checkBoxOverlay = findViewById(R.id.checkedIconDisplay);
        checkBoxUsage = findViewById(R.id.checkedIconUsage);

        blockingInfoLayout = findViewById(R.id.blockingInfoLayout);
        blockingScheduleDescription = findViewById(R.id.blockingScheduleDescription);
        scheduleMode = findViewById(R.id.scheduleMode);
        setScheduleBtn = findViewById(R.id.setScheduleBtn);

        recyclerView = findViewById(R.id.lockedAppsListt);
//        adapter = new LockedAppAdapter(lockedAppsList, this);
        adapter = new AppListAdapter(appsListFromFireDb, getApplicationContext(), MainActivity.this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(adapter);
        //updateDocToDB();
//        showBlockingInfo();


        setScheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, Schedule.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
        allAppsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, ShowAllApps.class);
                startActivity(myIntent);
            }
        });

        final Context context = this;
        getLockedApps(context);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_locked_apps);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_locked_apps:
                        return true;
                    case R.id.nav_all_apps:
                        startActivity(new Intent(getApplicationContext(),
                                ShowAllApps.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_settings:
                        startActivity(new Intent(getApplicationContext(),
                                About.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });


        progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                getLockedApps(context);
            }
        });
        //toggle permissions box
        togglePermissionBox();
       // checkAppsFirstTimeLaunch();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showBlockingInfo(){
        SharedPrefUtil prefUtil = SharedPrefUtil.getInstance(this);
        boolean checkSchedule = prefUtil.getBoolean("confirmSchedule");
        String startTimeHour = prefUtil.getStartTimeHour();
        String startTimeMin = prefUtil.getStartTimeMinute();
        String endTimeHour = prefUtil.getEndTimeHour();
        String endTimeMin = prefUtil.getEndTimeMinute();
        List<String> appsList = prefUtil.getLockedAppsList();
        List<String> days = prefUtil.getDaysList();
        List<String> shortDaysName = new ArrayList<>();
        days.forEach(day -> shortDaysName.add(day.substring(0,3)));
        if(appsList.size() > 0){
            if(checkSchedule){
                scheduleMode.setText("Every " +String.join(", ", shortDaysName) +" from "+ startTimeHour+":"+startTimeMin+" to "+endTimeHour+":"+endTimeMin);
            } else {
                scheduleMode.setText("Always Blocking");
            }
        } else {
            blockingInfoLayout.setVisibility(View.GONE);
        }
    }

    private void checkAppsFirstTimeLaunch() {
        /*Intent myIntent = new Intent(MainActivity.this, IntroScreen.class);
        MainActivity.this.startActivity(myIntent);*/
        boolean secondTimePref = SharedPrefUtil.getInstance(this).getBoolean("secondRun");
        if (!secondTimePref) {
            Intent myIntent = new Intent(MainActivity.this, IntroScreen.class);
            MainActivity.this.startActivity(myIntent);
            SharedPrefUtil.getInstance(this).putBoolean("secondRun", true);
        }
    }

    private void togglePermissionBox() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this) || !isAccessGranted()) {
                emptyLockListInfo.setVisibility(View.GONE);
                btnEnableOverlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        overlayPermission();
                    }
                });
                btnEnableUsageAccess.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        accessPermission();
                    }
                });

                if (Settings.canDrawOverlays(this)) {
                    btnEnableOverlay.setVisibility(View.INVISIBLE);
                    checkBoxOverlay.setColorFilter(Color.GREEN);
                }
                if (isAccessGranted()) {
                    btnEnableUsageAccess.setVisibility(View.INVISIBLE);
                    checkBoxUsage.setColorFilter(Color.GREEN);
                }

            } else {
                    enableUsageAccess.setVisibility(View.GONE);
                    enableOverlayAccess.setVisibility(View.GONE);
                    toggleEmptyLockListInfo(this);
                    updateDocToDB();
            }

        }
    }

    private void addIconToBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_zz);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BackgroundManager.getInstance().init(this).startService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        BackgroundManager.getInstance().init(this).startService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BackgroundManager.getInstance().init(this).startService();
    }

    public void getLockedApps(Context ctx) {
        toggleEmptyLockListInfo(ctx);
        List<String> prefAppList = SharedPrefUtil.getInstance(ctx).getLockedAppsList();
        List<ApplicationInfo> packageInfos = ctx.getPackageManager().getInstalledApplications(0);
        lockedAppsList.clear();
        for (int i = 0; i < packageInfos.size(); i++) {
            if (packageInfos.get(i).icon > 0) {
                String name = packageInfos.get(i).loadLabel(ctx.getPackageManager()).toString();
                Drawable icon = packageInfos.get(i).loadIcon(ctx.getPackageManager());
                String packageName = packageInfos.get(i).packageName;
                if (prefAppList.contains(packageName)) {
                    lockedAppsList.add(new AppModel(name, icon, 1, packageName));
                } else {
                    continue;
                }
            }
        }
        lockedAppsAdapter.notifyDataSetChanged();
        progressDialog.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        togglePermissionBox();
    }

    public void toggleEmptyLockListInfo(Context ctx) {
        List<String> prefAppList = SharedPrefUtil.getInstance(ctx).getLockedAppsList();
        if (prefAppList.size() > 0) {
            emptyLockListInfo.setVisibility(View.GONE);
        } else {
            emptyLockListInfo.setVisibility(View.VISIBLE);
        }
    }

    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            }
            int mode = 0;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void accessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isAccessGranted()) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivityForResult(intent, 102);
            }
        }
    }
    public void ASPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isAccessibilityServiceEnabled()) {
                // If not enabled, open accessibility settings to enable it
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(intent, ACCESSIBILITY_SERVICE_REQUEST);
            }
        }
    }
    private boolean isAccessibilityServiceEnabled() {
        // Check if your accessibility service is enabled
        String service = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();
        int accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, 0);
        if (accessibilityEnabled == 1) {
            String enabledServices = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (enabledServices != null) {
                return enabledServices.toLowerCase().contains(service.toLowerCase());
            }
        }
        return false;
    }
    public void overlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(myIntent, 101);
            }
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.schedule_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.scheduleMenuBtn) {
            Intent myIntent = new Intent(MainActivity.this, Schedule.class);
            MainActivity.this.startActivity(myIntent);
        }else if(id == R.id.logout_item){
                prefUtil.setUserName("");
                prefUtil.setPassword("");
                Intent intent=new Intent(getApplicationContext(), LoginPage.class);
                startActivity(intent);
                finishAffinity();
        }
        if (id == R.id.statsButton) {
            Intent myIntent = new Intent(MainActivity.this, UsesStatsActivity.class);
            myIntent.putExtra("email", usersEmail);
            MainActivity.this.startActivity(myIntent);
        }

        return super.onOptionsItemSelected(item);
    }
    private static void disableAccessibilityService(Context context, Class<?> serviceClass) {
       ComponentName componentName = new ComponentName(context, serviceClass);
        context.getPackageManager().setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private  void enableAccessibilityService(Context context, Class<?> serviceClass) {

        ComponentName componentName = new ComponentName(context, serviceClass);
        context.getPackageManager().setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        // Open Accessibility settings to prompt the user to re-enable the service
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       context.startActivity(intent);
        // MyAccessibilityService.killApp(context, "com.whatsapp");
        // serviceConnection.setMyServiceInfo(this,serviceConnection);
    }

    private void sendAppListToDB(ArrayList<AppData> appDataList) {
        ApplicationListModel applicationListModel = new ApplicationListModel(usersEmail, appDataList);
        db.collection("apps_list").document(usersEmail).set(applicationListModel);
    }
    public void getAppListFromDb() {
        DocumentReference docRef = db.collection("apps_list").document(usersEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    try {
                        if (document.exists()) {
                            ApplicationListModel applicationListModel = document.toObject(ApplicationListModel.class);
                            appsListFromFireDb.addAll(applicationListModel.getDataArrayList());

                            recyclerView.setAdapter(adapter);
                            validateApps();

                        } else {
                            Toast.makeText(MainActivity.this, "No Apps found", Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    Log.i("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    private void validateApps() {
        Log.d("$$validateApps:: ","validateApps");
        Log.d("$$validateApps:: ","appsListFromFireDb:: "+appsListFromFireDb.size());
        Log.d("$$validateApps:: ","installedAppsList:: "+installedAppsList.size());

        String appName="";
        if (installedAppsList != null && appsListFromFireDb != null) {
            for (int i = 0; i < installedAppsList.size(); i++) {
                appName = installedAppsList.get(i).getAppName();
                for (int j = 0; j < appsListFromFireDb.size(); j++) {
                    if (appsListFromFireDb.get(j).getAppName().equals(appName)) {
                        commonList.add(appsListFromFireDb.get(j));
                        Log.d("$$validateApps:: ",appsListFromFireDb.get(j).getBundle_id());
                    }
                }
            }
        }
//        List<String> prefLockedAppList = SharedPrefUtil.getInstance(this).getLockedAppsList();
        Log.d("$$validateApps",""+commonList.size());
        for (AppData app: commonList) {
            if (app.getIsAppLocked()) {
                lockedApps.add(app.getBundle_id());
                Log.d("$$validateApps:: ","LockedApp:: "+app.getBundle_id());
            }
        }
        SharedPrefUtil.getInstance(MainActivity.this).createLockedAppsList(lockedApps);
    }

    public void getInstalledApps() {
        List<String> prefLockedAppList = SharedPrefUtil.getInstance(this).getLockedAppsList();
        /*List<ApplicationInfo> packageInfos = getPackageManager().getInstalledApplications(0);*/
        PackageManager pk = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList = pk.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfoList) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            String name = activityInfo.loadLabel(getPackageManager()).toString();
            Drawable icon = activityInfo.loadIcon(getPackageManager());
            String packageName = activityInfo.packageName;
            if (!packageName.matches("com.robocora.appsift|com.android.settings")) {
                if (!prefLockedAppList.isEmpty()) {
                    //check if apps is locked
                    if (prefLockedAppList.contains(packageName)) {
                        installedAppsList.add(new AppModel(name, icon, 1, packageName));
                    } else {
                        installedAppsList.add(new AppModel(name, icon, 0, packageName));
                    }
                } else {
                    installedAppsList.add(new AppModel(name, icon, 0, packageName));
                }
            } else {
                //do not add settings to app list
            }

        }


    }

    private void updateDocToDB() {
        DocumentReference docRef = db.collection("apps_list").document(usersEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    try {
                        if (document.exists()) {
                            getInstalledApps();
                            getAppListFromDb();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                updateDB();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    Log.i("TAG", "get failed with ", task.getException());
                }
            }
        });
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private void updateDB() {
        appsListFromFireDb = DemoKot.Companion.printCurrentUsageStatus(getApplicationContext(), appsListFromFireDb, usersEmail);
        ApplicationListModel applicationListModel = new ApplicationListModel(usersEmail, appsListFromFireDb);
        db.collection("apps_list").document(usersEmail).set(applicationListModel);
        adapter = new AppListAdapter(appsListFromFireDb, getApplicationContext(), this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onChecked(boolean isChecked, int position,ArrayList<AppData> finalList) {
        Log.d("$$onChecked is:: ",isChecked+" pos:: "+position);
//        appsListFromFireDb.get(position).setIsAppLocked(isChecked);
//        sendAppListToDB(appsListFromFireDb);
        sendAppListToDB(finalList);
    }

    private void updateStashDB() {
        for (int i = 0; i<2; i++) {
            AppUsesData appUsesData = new AppUsesData();
            appUsesData.setAppName("qwer");
            appUsesData.setBundle_id("PackageName");
            appUsesData.setStartTime(11l);
            appUsesData.setEndTime(10l);
            appUsesData.setUsageTime(i+"n");
            appUsesData.setLaunchCount(1+2);
            appUsesDataArrayList.add(appUsesData);
        }

        for (int i = 0; i<2; i++) {
            statsModelArrayList.add(new StatsModel(i+"", appUsesDataArrayList));
        }
        UsesStatsDataModel statsModel = new UsesStatsDataModel(usersEmail, statsModelArrayList);
        db.collection("app_stats").document(usersEmail).set(statsModel);
    }

}