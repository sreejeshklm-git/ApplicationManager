package com.example.appblockr.ui.stats;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appblockr.R;
import com.example.appblockr.adapter.StatsAppListAdapter;
import com.example.appblockr.databinding.ActivityUsesStatsBinding;
import com.example.appblockr.model.AppUsesData;
import com.example.appblockr.model.StatsModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UsesStatsActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityUsesStatsBinding binding;
    private String usersEmail;

    private FirebaseFirestore db;
    private StatsAppListAdapter adapter;

//    private ArrayList<AppUsesData> appUsageList;
    private ProgressDialog dialog;
    String yesterday;

    HashMap<String,StatsModel> finalDocsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.stats_title_bar);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_uses_stats);
        dialog = new ProgressDialog(UsesStatsActivity.this);

        db = FirebaseFirestore.getInstance();
        usersEmail = getIntent().getStringExtra("email");

        binding.btnToday.setBackgroundResource(R.drawable.active_round);

        SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        yesterday = sdf.format(cal.getTime());

        fetchAllStatsFromDb();

        initUi();


    }

    private void initUi() {

        binding.btnToday.setOnClickListener(this);
        binding.btnYesterday.setOnClickListener(this);
        binding.btnLastWeek.setOnClickListener(this);
        binding.btnLastMonth.setOnClickListener(this);
//
        adapter = new StatsAppListAdapter(new ArrayList<AppUsesData>(), getApplicationContext());
        binding.statsAppsList.setLayoutManager(new LinearLayoutManager(UsesStatsActivity.this));
        binding.statsAppsList.setAdapter(adapter);

        binding.btnYesterday.setBackgroundResource(R.drawable.active_round);
        getYesterdayStatFromDb();
    }


    private void getRangedDateStats(ArrayList<String> dates) {
        HashMap<String,AppUsesData> aggregatedMap = new HashMap<>();

        for (Map.Entry<String, StatsModel> entry : finalDocsMap.entrySet()) {
            if (dates.contains(entry.getKey())) {
//                appUsageList.addAll(entry.getValue().getDataArrayList());

                for (AppUsesData model : entry.getValue().getDataArrayList()) {
                    String name = model.getAppName();
                    int count = model.getLaunchCount();
                    String usageTime = model.getUsageTime();
                    //check if name already exists
                    if (aggregatedMap.containsKey(name)) {
                        AppUsesData existingStat = aggregatedMap.get(name);
                        assert existingStat != null;
                        existingStat.setLaunchCount(existingStat.getLaunchCount() +  count);
                        existingStat.setUsageTime(existingStat.getUsageTime() +  usageTime);
                    }else{
                        aggregatedMap.put(name,model);
                    }
                }

                //adapter.addItems(appUsageList);
            }
        }

        ArrayList<AppUsesData> aggregateList = new ArrayList<>();
        for (AppUsesData item : aggregatedMap.values()) {
            aggregateList.add(item);
        }

        adapter.addItems(aggregateList);
    }

    private ArrayList<String> getLastMonthDatesInFormat() {
        ArrayList<String> lastMonthList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);

        int lastMonthMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= lastMonthMaxDay; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault());
            String formattedDate = sdf.format(calendar.getTime());
            lastMonthList.add(formattedDate);
        }

        return lastMonthList;
    }

    private ArrayList<String> getLastWeekDatesInFormat() {
        ArrayList<String> lastWeekList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 1; i <= 7; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault());
            String formattedDate = sdf.format(calendar.getTime());
            lastWeekList.add(formattedDate);
        }

        return lastWeekList;
    }

    private ArrayList<String> getYesterdayDateInFormat() {
        ArrayList<String> yesterday = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault());
        String formattedDate = sdf.format(calendar.getTime());
        yesterday.add(formattedDate);

        return yesterday;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_today:
                SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy");
                String currentDate = sdf.format( new Date());

               /* appUsageList.clear();
                adapter.addItems(null);

                for (Map.Entry<String, StatsModel> entry : finalDocsMap.entrySet()) {
                    if (entry.getKey().equals(currentDate)) {
                        appUsageList = entry.getValue().getDataArrayList();
                        adapter.addItems(appUsageList);
                    }
                }*/

//                binding.btnToday.setBackgroundResource(R.drawable.active_round);
                binding.btnYesterday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastWeek.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastMonth.setBackgroundResource(R.drawable.non_active_round);

                break;
            case R.id.btn_yesterday:
                ArrayList<String> dateList = getYesterdayDateInFormat();
                getRangedDateStats(dateList);

//                binding.btnToday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnYesterday.setBackgroundResource(R.drawable.active_round);
                binding.btnLastWeek.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastMonth.setBackgroundResource(R.drawable.non_active_round);

                break;
            case R.id.btn_last_week:
                ArrayList<String> lastWeekDates = getLastWeekDatesInFormat();

                Log.d("DateList:: -W=  ", "Size:: "+lastWeekDates.size());
                Log.d("DateList:: -W=  ", lastWeekDates.toString());

                getRangedDateStats(lastWeekDates);
//                binding.btnToday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnYesterday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastWeek.setBackgroundResource(R.drawable.active_round);
                binding.btnLastMonth.setBackgroundResource(R.drawable.non_active_round);
                break;
            case R.id.btn_last_month:

                ArrayList<String> lastMonthDates = getLastMonthDatesInFormat();
                Log.d("DateList :: -M=  ", lastMonthDates.toString());
                Log.d("DateList:: -W=  ", "Size:: "+lastMonthDates.size());

                getRangedDateStats(lastMonthDates);

                binding.btnToday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnYesterday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastWeek.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastMonth.setBackgroundResource(R.drawable.active_round);

                break;
            default:
                Log.d("##", "noting Clicled");
        }
    }


    public void getYesterdayStatFromDb() {
        dialog.show();
        db.collection(usersEmail).document(yesterday)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                StatsModel model = document.toObject(StatsModel.class);
                                if (model != null) {
//                                    appUsageList = model.getDataArrayList();
                                    adapter.addItems(model.getDataArrayList());
                                    /*adapter = new StatsAppListAdapter(appUsageList, getApplicationContext());
                                    binding.statsAppsList.setLayoutManager(new LinearLayoutManager(UsesStatsActivity.this));
                                    binding.statsAppsList.setAdapter(adapter);*/
                                } else {
                                    Toast.makeText(UsesStatsActivity.this, "No Apps found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(UsesStatsActivity.this, "No Document found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
        dialog.dismiss();
    }

    private void fetchAllStatsFromDb(){
        ArrayList<String> docDateList = new ArrayList<>();
        db.collection(usersEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("##getAllDocs", document.getId() + " => " + document.getData());
                        docDateList.add(document.getId());
                        finalDocsMap.put(document.getId(),document.toObject(StatsModel.class));

                    }
                }
            }
        });
    }
}