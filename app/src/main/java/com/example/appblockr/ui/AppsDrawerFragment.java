package com.example.appblockr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appblockr.AdminActivity;
import com.example.appblockr.FilterListener;
import com.example.appblockr.HomeActivity;
import com.example.appblockr.LoginPage;
import com.example.appblockr.MainActivity;
import com.example.appblockr.R;
import com.example.appblockr.adapter.AppsDrawerAdapter;
import com.example.appblockr.services.ForegroundService;
import com.example.appblockr.shared.SharedPrefUtil;


public class AppsDrawerFragment extends Fragment {
    RecyclerView recyclerView;
    SearchView searchView;
    ImageView settings;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    public AppsDrawerFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_apps_drawer,container,false);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        searchView= view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.appDrawer_recylerView);
     settings = view.findViewById(R.id.settings);
        TextView userName= view.findViewById(R.id.uname);
        TextView userEMail= view.findViewById(R.id.uemail);
        SharedPrefUtil prefUtil = new SharedPrefUtil(getContext());
        String userType = prefUtil.getUserType("user_type");
        String email = prefUtil.getEmail("email");
        String uName= prefUtil.getUserName("userName");
        userName.setText(uName);
        userEMail.setText(email);
        ImageView lout= view.findViewById(R.id.logout);

        lout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                prefUtil.setUserName("");
                prefUtil.setPassword("");
                if (userType.equals("2")) {
                    try {
                        Intent intents = new Intent(getContext(), ForegroundService.class);
                        intents.setAction(ForegroundService.ACTION_STOP_FOREGROUND_SERVICE);
                        getContext().startService(intents);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Intent intent=new Intent(getContext(), LoginPage.class);
                startActivity(intent);


            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (userType.equals("1")) {

                    Intent intent = new Intent(getContext(), AdminActivity.class);
                    startActivity(intent);
                    //finishAffinity();

                } else if (userType.equals("2")) {

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    //finishAffinity();
                }

            }
        });
        adapter = new AppsDrawerAdapter(getContext());

       // layoutManager = new LinearLayoutManager(getContext());
        layoutManager=new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        //recyclerView.setLayoutManager(layoutManager);




        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                FilterListener filterListener =(AppsDrawerAdapter) adapter;
                filterListener.onfilter(newText);
                return false;
            }
        });
        recyclerView.setAdapter(adapter);



    }
}

