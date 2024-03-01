package com.example.appblockr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appblockr.adapter.AdminAdapter;
import com.example.appblockr.adapter.UserAdapter;
import com.example.appblockr.model.AppData;
import com.example.appblockr.services.ForegroundService;
import com.example.appblockr.shared.SharedPrefUtil;
import com.example.appblockr.ui.adduser.AdduserActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;





public class AdminActivity extends AppCompatActivity {

    private ArrayList courseNames;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ImageView addIcon,logoutText,icBack;
    private FirebaseFirestore db;
    private TextView headerLable;
    ArrayList<String> usersList,emailList;
    ArrayList<AppData> appDataList;

    SharedPrefUtil prefUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //  setTitle(" Locked Apps");
        setTheme(R.style.Theme_Appsift);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
       /* getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFBB86FC")));
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple_200));
        }*/
        db = FirebaseFirestore.getInstance();
        usersList= new ArrayList<String>();
        emailList= new ArrayList<String>();
        appDataList= new ArrayList<AppData>();
        prefUtil = new SharedPrefUtil(getApplicationContext());
         String userType= prefUtil.getUserType("user_type");

        //toolbar=findViewById(R.id.toolbar);
        recyclerView=findViewById(R.id.recyclerView);
        addIcon = findViewById(R.id.add_icon);
        headerLable= findViewById(R.id.headerLable);
        logoutText = findViewById(R.id.logout);
        icBack = findViewById(R.id.ic_back);

        if(userType.equals("2")){
            addIcon.setVisibility(View.GONE);
            headerLable.setText("Application Data");
        }
        if(userType.equals("2")){
//            UserAdapter userAdapter=new UserAdapter(getApplicationContext());
//            recyclerView.setAdapter(userAdapter);
            readDBApp();
        } else if (userType.equals("1")) {
            readDBUsers();
            headerLable.setText("Admin Dashboard");
        }


        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prefUtil.setUserName("");
                prefUtil.setPassword("");

                Intent intent=new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);

                finish();

            }
        });
        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();

            }
        });
        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), AdduserActivity.class);
                startActivity(intent);
            }
        });


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(AdminActivity.this,
                DividerItemDecoration.VERTICAL));

    }
    public void readDBUsers(){

        db.collection("add_users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userNameDb = document.getString("user_name");
                                String emailListDb= document.getString("email");
                                usersList.add(userNameDb);
                                emailList.add(emailListDb);
                                AdminAdapter adapter = new AdminAdapter(AdminActivity.this,usersList,emailList);
                                recyclerView.setAdapter(adapter);
                                Log.d("Data", "userData" + " => " + usersList.size());
                            }
                        } else {
                            Log.w("data", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
    public void readDBApp(){

        db.collection("apps_list")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String appName= document.getString("appName");
                                String clicksCount= document.getString("clicksCount");
                                String appDuration= document.getString("duration");
                                String email= document.getString("email");
                                String appPackage= document.getString("bundle_id");
                                boolean appIsLock= document.getBoolean("isAppLocked");

                                AppData appData=new AppData();

                                appData.setAppName(appName);
                                appData.setClicksCount(clicksCount);
                                appData.setDuration(appDuration);
                                appData.setEmail(email);
                                appData.setBundle_id(appPackage);
                                appData.setIsAppLocked(appIsLock);

                                appDataList.add(appData);

                                UserAdapter userAdapter=new UserAdapter(appDataList);
                                recyclerView.setAdapter(userAdapter);
                                Log.d("AppData", "userData" + " => " + appData.getAppName()+appData.getDuration()+appData.getIsAppLocked()+appData.getEmail()+appData.getBundle_id()+appData.getClicksCount());
                            }
                        } else {
                            Log.w("data", "Error getting documents.", task.getException());
                        }
                    }
                });
    }


}