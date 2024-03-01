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

import com.example.appblockr.services.ForegroundService;
import com.example.appblockr.shared.SharedPrefUtil;
import com.example.appblockr.ui.HomeScreenFragment;


public class HomeActivity extends AppCompatActivity {
    SharedPrefUtil prefUtil;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
         if (getSupportActionBar() != null) {
             getSupportActionBar().hide();
        }
        prefUtil = new SharedPrefUtil(getApplicationContext());
        String userName = prefUtil.getUserName("userName");
        String password = prefUtil.getPassword("password");
        String userType = prefUtil.getUserType("user_type");
        String email = prefUtil.getEmail("email");
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
}