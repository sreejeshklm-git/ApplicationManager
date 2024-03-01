package com.example.appblockr.ui.adduser;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.example.appblockr.R;
import com.example.appblockr.adapter.UserAdapter;
import com.example.appblockr.model.AppData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;



public class UserAppListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    ArrayList<AppData> appDataList;
//    private UserAdapter userAdapter;
    private String usersEmail;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Users Application");
        setContentView(R.layout.activity_user_app_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFBB86FC")));
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple_200));
        }
//        usersEmail = getIntent().getStringExtra("email");
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.rvContacts);
        appDataList = new ArrayList<AppData>();
        AppData appData=new AppData();
        appData.setAppName("ee");
        appData.setClicksCount("ed");
        appData.setDuration("ddd");
        appData.setEmail("email");
        appData.setBundle_id("appPackage");
        appData.setIsAppLocked(false);
        appDataList.add(appData);
        UserAdapter userAdapter = new UserAdapter(appDataList);
        recyclerView.setAdapter(userAdapter);
       // readDBApp();


    }

    public void readDBApp() {

        db.collection("apps_list")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String appName = document.getString("app_name");
                                String clicksCount = document.getString("clicks_cound");
                                String appDuration = document.getString("duration");
                                String email = document.getString("email");
                                String appPackage = document.getString("package");
                                boolean appIsLock = document.getBoolean("is_app_lock");
                                if (usersEmail.equals(email)) {
                                    AppData appData = new AppData();

                                    appData.setAppName(appName);
                                    appData.setClicksCount(clicksCount);
                                    appData.setDuration(appDuration);
                                    appData.setEmail(email);
                                    appData.setBundle_id(appPackage);
                                    appData.setIsAppLocked(appIsLock);

                                    appDataList.add(appData);
                                }
//                                Log.d("AppData", "userData" + " => " + appData.getAppName()+appData.getDuration()+appData.getIsAppLocked()+appData.getEmail()+appData.getBundle_id()+appData.getClicksCount());
                            }
                            UserAdapter  userAdapter = new UserAdapter(appDataList);
                            recyclerView.setAdapter(userAdapter);
                        } else {
                            Log.w("data", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}