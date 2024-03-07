package com.example.appblockr;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.appblockr.model.Contact;
import com.example.appblockr.services.ForegroundService;
import com.example.appblockr.services.StatsWorkerManager;
import com.example.appblockr.shared.SharedPrefUtil;
import com.example.appblockr.ui.HomeScreenFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;


public class HomeActivity extends AppCompatActivity {
    SharedPrefUtil prefUtil;
    Handler handler;

    String  usersEmail=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
         if (getSupportActionBar() != null) {
             getSupportActionBar().hide();
        }

        /* FirebaseFirestore firestore = FirebaseFirestore.getInstance();
         firestore.collection("test").document("today").set(new Contact("Srinivas",false));*/
        prefUtil = new SharedPrefUtil(getApplicationContext());
        String userName = prefUtil.getUserName("userName");
        String password = prefUtil.getPassword("password");
        String userType = prefUtil.getUserType("user_type");
        usersEmail = prefUtil.getEmail("email");
        handler = new Handler();

        //ContextCompat.startForegroundService(this, new Intent(this, ForegroundService.class));

        /*if (userType.equals("1")) {


            Toast.makeText(getApplicationContext(), "Login Succesfull", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AdminActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Login Succesfull", Toast.LENGTH_SHORT).show();
//                                          Intent intent = new Intent(getApplicationContext(), AppListActivity.class);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        }*/

    }

    @Override
    protected void onResume() {
        prefUtil = new SharedPrefUtil(getApplicationContext());
        String userName = prefUtil.getUserName("userName");
        String password = prefUtil.getPassword("password");
        String userType = prefUtil.getUserType("user_type");
        String email = prefUtil.getEmail("email");
        if (userName.length() == 0 || password.length() == 0) {
            Intent intent = new Intent(this, LoginPage.class);
            startActivity(intent);
            //finish();
        } else{
            if (userType.equals("2")){
                ContextCompat.startForegroundService(this, new Intent(this, ForegroundService.class));

                //Initiate Work Manager for pushing stats
                initWorker();
            }
            loadFragment(new HomeScreenFragment());

        }

        super.onResume();
    }

    private boolean loadFragment(Fragment fragment) {

        if (fragment != null) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            return true;

        }

        return false;

    }

    private void initWorker(){
        Data.Builder builder = new Data.Builder();
        builder.putString("KEY_USER_EMAIL",usersEmail);
        Data inputData = builder.build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest
//                .Builder(StatsWorkerManager.class,24, TimeUnit.HOURS)
                .Builder(StatsWorkerManager.class,16, TimeUnit.MINUTES)
                .setInputData(inputData)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork("StatsWork", ExistingPeriodicWorkPolicy.REPLACE,workRequest)
                .getResult();


    }

}