package com.example.appblockr.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appblockr.FilterListener;
import com.example.appblockr.R;
import com.example.appblockr.model.AppData;
import com.example.appblockr.model.AppInfo;
import com.example.appblockr.model.AppModel;
import com.example.appblockr.model.ApplicationListModel;
import com.example.appblockr.shared.SharedPrefUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class AppsDrawerAdapter extends RecyclerView.Adapter<AppsDrawerAdapter.ViewHolder>  implements FilterListener {

        private static Context context;
        private  List<AppInfo> appsList;
        private   List<ApplicationInfo> packageInfos;
        private  ArrayList<AppModel> apps = new ArrayList<>();
        private  FirebaseFirestore db;
        private   ArrayList<AppData>  appsListFromFireDb;
        private   ArrayList<AppModel> installedAppsList;
        private   ArrayList<AppModel> displayingAppsList;

        private   ArrayList<AppModel> filterdAppsList;
        private   ArrayList<AppData> commonList;
         private  ArrayList<String> lockedApps;
        private   ArrayList<AppModel> LockedAppsList;
        public AppsDrawerAdapter(Context c ) {

                //This is where we build our list of app details, using the app
                //object we created to store the label, package name and icon
                appsListFromFireDb = new ArrayList<AppData>();
                installedAppsList = new ArrayList<AppModel>();
                displayingAppsList = new ArrayList<AppModel>();
                filterdAppsList = new ArrayList<AppModel>();
                commonList = new ArrayList<AppData>();
                lockedApps = new ArrayList<String>();
                context = c;
                db = FirebaseFirestore.getInstance();
                setUpApps();


        }

        public  void setUpApps(){

               /* packageInfos = context.getPackageManager().getInstalledApplications(0);

                PackageManager pManager = context.getPackageManager();
                appsList = new ArrayList<AppInfo>();

                Intent i = new Intent(Intent.ACTION_MAIN, null);
                i.addCategory(Intent.CATEGORY_LAUNCHER);

                List<ResolveInfo> allApps = pManager.queryIntentActivities(i, 0);
                for (ResolveInfo ri : allApps) {
                        AppInfo app = new AppInfo();
                        app.label = ri.loadLabel(pManager);
                        app.packageName = ri.activityInfo.packageName;

                        Log.i(" Log package ",app.packageName.toString());
                        app.icon = ri.activityInfo.loadIcon(pManager);
                        appsList.add(app);

                }
*/

                getDeviceInstalledApps();
                setEventListenerFromDb();
                getAppListFromDb();

        }
        public  void getDeviceInstalledApps() {
                PackageManager pk = context.getPackageManager();
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> resolveInfoList = pk.queryIntentActivities(intent, 0);
                installedAppsList.clear();
                for (ResolveInfo resolveInfo : resolveInfoList) {
                        ActivityInfo activityInfo = resolveInfo.activityInfo;
                        String name = activityInfo.loadLabel(context.getPackageManager()).toString();
                        Drawable icon = activityInfo.loadIcon(context.getPackageManager());
                        String packageName = activityInfo.packageName;
                        installedAppsList.add(new AppModel(name, icon, 1, packageName));
                      /*  if (!packageName.matches("com.robocora.appsift|com.android.settings")) {
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
                        }*/

                }
        }
        public  void setEventListenerFromDb() {
               SharedPrefUtil prefUtil = new SharedPrefUtil(context);

                String email = prefUtil.getEmail("email");
                DocumentReference docRef = db.collection("apps_list").document(email);
                Log.e("docRef",""+docRef);
                docRef.addSnapshotListener((snapshot, e) -> {
                        getDeviceInstalledApps();
                        getAppListFromDb();
                        Log.e("event triggered","event triggered");
                        if (e != null) {
                                // Handle errors
                                return;
                        }
                        if (snapshot != null && snapshot.exists()) {

                                // Data exists, handle it here
                                //String data = snapshot.getString("isAppLocked");
                                // Log.e("event triggered",data);
                                // Update your UI or perform any other actions
                        } else {
                                // Document does not exist
                        }
                });

        }

        public  void getAppListFromDb() {
                SharedPrefUtil prefUtil = new SharedPrefUtil(context);

                String email = prefUtil.getEmail("email");
                appsListFromFireDb.clear();
                DocumentReference docRef = db.collection("apps_list").document(email);
               Log.e("docRef",""+docRef);

               docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        try {
                                                if (document.exists()) {
                                                        appsListFromFireDb.clear();
                                                        ApplicationListModel applicationListModel = document.toObject(ApplicationListModel.class);
                                                        appsListFromFireDb.addAll(applicationListModel.getDataArrayList());

                                                      //  recyclerView.setAdapter(adapter);
                                                        validateApps(installedAppsList,appsListFromFireDb);

                                                } else {
                                                        displayingAppsList=installedAppsList;
                                                        notifyDataSetChanged();
                                                       // validateApps(installedAppsList,appsListFromFireDb);
                                                        //Toast.makeText(context, "No Apps found", Toast.LENGTH_SHORT).show();

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

        private  void validateApps(ArrayList<AppModel> installedAppsList, ArrayList<AppData> appsListFromFireDb) {
                Log.d("$$validateApps:: ","validateApps");
                Log.d("$$validateApps:: ","appsListFromFireDb:: "+appsListFromFireDb.size());
                Log.d("$$validateApps:: ","installedAppsList:: "+installedAppsList.size());
                lockedApps.clear();
                String appName="";
                for (int j = 0; j < appsListFromFireDb.size(); j++) {
                       // if (appsListFromFireDb.get(j).getAppName().equals(appName)) {
                               // Log.e("installed app",appName+" from db "+appsListFromFireDb.get(j).getAppName()+" "+appsListFromFireDb.get(j).getIsAppLocked());
                                if (appsListFromFireDb.get(j).getIsAppLocked()) {
                                       // Log.e("installed locked application ",""appsListFromFireDb.get(j).getBundle_id()+")");
                                        //li.add(installedAppsList.get(i));
                                        lockedApps.add(appsListFromFireDb.get(j).getBundle_id());
                                        //Log.d("$$validateApps:: ","LockedApp:: "+app.getBundle_id());
                                }

                               // Log.d("$$validateApps:: ",appsListFromFireDb.get(j).getBundle_id());
                       // }
                }

                for (int j = 0; j < lockedApps.size(); j++) {
                        Log.e("locked app",lockedApps.get(j)+")");
                        for (int k = 0; k < installedAppsList.size(); k++) {
                                Log.e("packname installed",installedAppsList.get(k).getPackageName()+ " "+lockedApps.get(j));
                                if(installedAppsList.get(k).getPackageName().equals(lockedApps.get(j))){
                                        Log.e("packnameremoved",installedAppsList.get(k).getPackageName());
                                        installedAppsList.remove(k);
                                }
                        }


                }
              /*  if (installedAppsList != null && appsListFromFireDb != null) {
                        for (int i = 0; i < installedAppsList.size(); i++) {
                                Log.e(" app naaameeee",appName);
                                appName = installedAppsList.get(i).getAppName();
                                for (int j = 0; j < appsListFromFireDb.size(); j++) {
                                        if (appsListFromFireDb.get(j).getAppName().equals(appName)) {
                                             Log.e("installed app",appName+" from db "+appsListFromFireDb.get(j).getAppName()+" "+appsListFromFireDb.get(j).getIsAppLocked());
                                                if (!appsListFromFireDb.get(j).getIsAppLocked()) {
                                                        Log.e("installed app",appName+")");
                                                        apps.add(installedAppsList.get(i));
                                                        //Log.d("$$validateApps:: ","LockedApp:: "+app.getBundle_id());
                                                }

                                                Log.d("$$validateApps:: ",appsListFromFireDb.get(j).getBundle_id());
                                        }
                                }
                        }
                }*/
                displayingAppsList=installedAppsList;
                Log.d("$$validateApps:: ","Lockedapplistsize:: "+lockedApps.size());
                this.notifyDataSetChanged();
//        List<String> prefLockedAppList = SharedPrefUtil.getInstance(this).getLockedAppsList();
               // Log.d("apps size",""+apps.size());
               // notifyDataSetChanged();
               /* for (AppData app: commonList) {
                        if (app.getIsAppLocked()) {
                                lockedApps.add(app.getBundle_id());
                                Log.d("$$validateApps:: ","LockedApp:: "+app.getBundle_id());
                        }
                }*/
                //SharedPrefUtil.getInstance(MainActivity.this).createLockedAppsList(lockedApps);
        }
        public void filterInstalledApps(String filterText){

        }
        public void getInstalledApps() {
              /*  List<String> prefLockedAppList = SharedPrefUtil.getInstance(this).getLockedAppsList();
                *//*List<ApplicationInfo> packageInfos = getPackageManager().getInstalledApplications(0);*//*
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

                }*/


        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                //This is what adds the code we've written in here to our target view
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_view_list, parent, false);


                return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

                AppModel app = displayingAppsList.get(position);
               // String appLabel = appsList.get(position).label.toString();
                String appLabel =app.getAppName() ;
               String appPackage = app.getPackageName();
                Drawable appIcon = app.getIcon();
                TextView textView = holder.textView;
                textView.setText(appLabel);
                ImageView imageView = holder.img;
                imageView.setImageDrawable(appIcon);

                holder.img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackage);

                                if (intent != null) {
                                        // If the intent is not null (i.e., the app exists), start the activity
                                        context.startActivity(intent);
                                } else {
                                        // If the intent is null (i.e., the app does not exist), display a message or take appropriate action
                                       // Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show();
                                }
                                //((MainActivity)ctx).updateLockedAppsNotification();
                        }
                        /* }*/
                });
        }

        @Override
        public int getItemCount() {
                return displayingAppsList.size();
        }

        @Override
        public void onfilter(String text) {
                filterdAppsList.clear();
                for (AppModel item : installedAppsList) {
                        // checking if the entered string matched with any item of our recycler view.
                        if (item.getAppName().toLowerCase().contains(text.toLowerCase())) {
                                // if the item is matched we are
                                // adding it to our filtered list.
                                filterdAppsList.add(item);
                        }
                }
                if(text==""){
                        displayingAppsList=installedAppsList;
                }else{
                displayingAppsList=filterdAppsList;
                }
                Log.e("displayingAppsList",""+displayingAppsList.size());
                notifyDataSetChanged();


        }


        public class ViewHolder extends RecyclerView.ViewHolder {

                public TextView textView;
                public ImageView img;

                public ViewHolder(View itemView) {
                        super(itemView);

                        //Finds the views from our row.xml
                        textView =  itemView.findViewById(R.id.tv_app_name);
                        img = itemView.findViewById(R.id.app_icon);



                }


        }


}

