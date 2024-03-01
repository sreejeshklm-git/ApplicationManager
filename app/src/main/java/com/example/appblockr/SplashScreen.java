package com.example.appblockr;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.appblockr.services.ForegroundService;
import com.example.appblockr.shared.SharedPrefUtil;

//import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreen extends AppCompatActivity {
    Handler handler;
    SharedPrefUtil prefUtil;
    TextView logText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_layout);
        prefUtil = new SharedPrefUtil(getApplicationContext());

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        handler = new Handler();
        String userName = prefUtil.getUserName("userName");
        String password = prefUtil.getPassword("password");
        String userType = prefUtil.getUserType("user_type");
        String email = prefUtil.getEmail("email");
        //ContextCompat.startForegroundService(this, new Intent(this, ForegroundService.class));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (userName.length() == 0 || password.length() == 0) {
                    Intent intent = new Intent(SplashScreen.this, LoginPage.class);
                    startActivity(intent);
                    finish();
                } else if (userType.equals("1")) {
                    Toast.makeText(getApplicationContext(), "Login Succesfull", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Login Succesfull", Toast.LENGTH_SHORT).show();
//                                        Intent intent = new Intent(getApplicationContext(), AppListActivity.class);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                }
            }
        }, 3000);


    }
}