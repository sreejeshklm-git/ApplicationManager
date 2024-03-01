package com.example.appblockr;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appblockr.adapter.AllAppAdapter;
import com.example.appblockr.model.AppModel;
import com.example.appblockr.shared.SharedPrefUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;



public class ShowAllApps extends AppCompatActivity {
    RecyclerView recyclerView;
    List<AppModel> apps = new ArrayList<>();
    AllAppAdapter adapter;
    ProgressDialog progressDialog;
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_apps);
        setTheme(R.style.Theme_Appsift);
        addIconToBar();
        setTitle(" Installed Apps");
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_all_apps);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_locked_apps:
                        startActivity(new Intent(getApplicationContext(),
                                MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_all_apps:
                      /*  startActivity(new Intent(getApplicationContext(),
                                ShowAllApps.class));
                        overridePendingTransition(0,0);*/
                        return true;
                    case R.id.nav_settings:
                      /*  startActivity(new Intent(getApplicationContext(),
                                About.class));
                        overridePendingTransition(0, 0);*/
                        return true;
                }
                return false;
            }
        });

        recyclerView = findViewById(R.id.recycleview);
        adapter = new AllAppAdapter(apps, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        recyclerView.setAdapter(adapter);
        progressDialog = new ProgressDialog(this);
        progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                getInstalledApps();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressDialog.setTitle("Fetching Apps");
        progressDialog.setMessage("Loading");
        progressDialog.show();
    }

    private void addIconToBar() {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        setContentView(R.layout.activity_show_all_apps);
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
                        apps.add(new AppModel(name, icon, 1, packageName));
                    } else {
                        apps.add(new AppModel(name, icon, 0, packageName));
                    }
                } else {
                    apps.add(new AppModel(name, icon, 0, packageName));
                }
            } else {
                //do not add settings to app list
            }

        }
        adapter.notifyDataSetChanged();
        progressDialog.dismiss();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search...");
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String userInput = newText.toLowerCase();
                ArrayList<AppModel> newList = new ArrayList<>();
                for (AppModel app : apps) {
                    if (app.getAppName().toLowerCase().contains(userInput)) {
                        newList.add(app);
                    }
                }
                adapter.updateList(newList);
                return false;
            }
        });
        /*MenuItem savelist = menu.findItem(R.id.action_save);
        AppCompatButton bView = (AppCompatButton) savelist.getActionView();
        bView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ShowAllApps.this, "Save", Toast.LENGTH_SHORT).show();
            }
        });*/
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            default:
                return super.onOptionsItemSelected(item);
        }
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

}