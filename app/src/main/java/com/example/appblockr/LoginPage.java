package com.example.appblockr;

import static com.example.appblockr.MainActivity.context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appblockr.databinding.ActivityLoginPageBinding;
import com.example.appblockr.firestore.FireStoreManager;

import com.example.appblockr.model.ApplicationListModel;
import com.example.appblockr.shared.SharedPrefUtil;
import com.example.appblockr.ui.adduser.AppListActivity;
import com.example.appblockr.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import io.born.applicationmanager.firestore.User;

public class LoginPage extends AppCompatActivity {
    private ActivityLoginPageBinding binding;
    private FirebaseFirestore db;
    private SharedPrefUtil prefUtil;
    private String android_id;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().setTitle("Login");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_page);
        binding.progressBarCyclic.setVisibility(View.INVISIBLE);
        prefUtil = new SharedPrefUtil(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.d("##",android_id);
     ///   startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER));
  /*if (!isMyLauncherDefault()) {
           PackageManager p = getPackageManager();
            ComponentName cN = new ComponentName(getApplicationContext(), FakeLauncherActivity.class);
            p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);*/

            //Intent selector = new Intent(Intent.ACTION_MAIN);
            //selector.addCategory(Intent.CATEGORY_HOME);
           // selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(selector);

          /* p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
       }*/
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBarCyclic.setVisibility(View.VISIBLE);
                binding.progressBarCyclic.setProgress(200);
                Utils.hideKeyboard(LoginPage.this);
                String email = binding.editTextEmail.getText().toString().trim();
                String password = binding.editTextPassword.getText().toString().trim();

                if (isValidEmail(email) && isValidPassword(password)) {
                    readDBLogin(email, password);
                } else {
                    binding.progressBarCyclic.setVisibility(View.GONE);
                    if (email.isEmpty()) {
                        binding.editTextEmail.setError("Email is required");
                    }
                    if (password.isEmpty()) {
                        binding.editTextPassword.setError("Password is required");
                    }
                    Toast.makeText(getApplicationContext(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    private boolean isMyLauncherDefault() {
        PackageManager localPackageManager = getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String str = localPackageManager.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        return str.equals(getPackageName());
    }
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email);
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }

    public void readDBLogin(String username, String password) {
        boolean loginSuc;
        db.collection("add_users")
                .whereEqualTo("user_name", username)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot documents = task.getResult();
                            int documentSize = documents.getDocuments().size();
                            Log.e("Size==", "" + documentSize);
                            if (documentSize > 0) {
                                binding.progressBarCyclic.setVisibility(View.GONE);
                                for (DocumentSnapshot document : task.getResult()) {
                                    user = document.toObject(User.class);

                                    if (user.getAndroid_id() == null || user.getAndroid_id().isEmpty()) {

                                        user.setAndroid_id(android_id);
                                        db.collection("add_users").document(user.getEmail()).set(user);

                                    } else if (user.getAndroid_id().equals(android_id)) {
                                        Log.d("##","Same Device");
                                    } else {
                                       // Toast.makeText(LoginPage.this, "User already Logged In with another device", Toast.LENGTH_SHORT).show();
                                        //return;
                                    }

                                    prefUtil.setEmail(user.getEmail());
                                    prefUtil.setUserName(user.getUser_name());
                                    prefUtil.setPassword(user.getPassword());
                                    prefUtil.setUserType(user.getUser_type());

                                    binding.errorText.setVisibility(View.INVISIBLE);
                                    binding.editTextEmail.getText().clear();
                                    binding.editTextPassword.getText().clear();
                                    finish();
                                    if (user.getUser_type().equals("2")) {
                                        Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                                        startActivity(intent);
                                    }
                                   /* if (user.getUser_type().equals("1")) {
                                        Toast.makeText(getApplicationContext(), "Login Succesfull", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
                                        startActivity(intent);
                                        finishAffinity();

                                    } else if (user.getUser_type().equals("2")) {
                                        Toast.makeText(getApplicationContext(), "Login Succesfull", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.putExtra("email", user.getEmail());
                                        startActivity(intent);
                                        finishAffinity();
                                    }*/

                                }
                            } else {
                                binding.progressBarCyclic.setVisibility(View.GONE);
                                binding.errorText.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "Invalid Credentials", Toast.LENGTH_LONG).show();
                            }

                            binding.progressBarCyclic.setVisibility(View.GONE);

                        }
                    }
                });

    }

}