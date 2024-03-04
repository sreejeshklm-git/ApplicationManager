package com.example.appblockr.ui.stats;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.appblockr.HomeActivity;
import com.example.appblockr.R;
import com.example.appblockr.adapter.StatsAppListAdapter;
import com.example.appblockr.databinding.ActivityUsesStatsBinding;
import com.example.appblockr.model.AppUsesData;
import com.example.appblockr.model.StatsModel;
import com.example.appblockr.model.UsesStatsDataModel;
import com.example.appblockr.ui.adduser.AppListActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UsesStatsActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityUsesStatsBinding binding;
    private String usersEmail;
    private UsesStatsDataModel statsDataModel;
    private FirebaseFirestore db;
    private StatsAppListAdapter adapter;
    private ArrayList<StatsModel> dayWiseStatsList;
    private ArrayList<StatsModel> finalList;
    private ArrayList<AppUsesData> appUsageList;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("Usage Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#03A9F4")));
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.appBackground));
        }
       // getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
       // getSupportActionBar().setCustomView(R.layout.stats_title_bar);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_uses_stats);

        binding.calenderIconStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerdialog();
            }
        });

        binding.calenderIconEndDate.setOnClickListener(view -> {
            datePickerdialog();
        });

        dialog = new ProgressDialog(UsesStatsActivity.this);
        db = FirebaseFirestore.getInstance();
        usersEmail = getIntent().getStringExtra("email");
        dayWiseStatsList = new ArrayList<StatsModel>();
        finalList = new ArrayList<StatsModel>();
        appUsageList = new ArrayList<AppUsesData>();
        binding.btnToday.setBackgroundResource(R.drawable.active_round);
        SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy");
        String currentDate = sdf.format( new Date());
       /* ImageView icBack= findViewById(R.id.ic_back);
        icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();

            }
        });*/
        getAppListFromDb(currentDate);

        initUi();
    }

    private void datePickerdialog() {

        // Creating a MaterialDatePicker builder for selecting a date range
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
              //  .setSelection(new Pair<>(startmillis,endMillis));
        builder.setTitleText("Select a date range");


        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now());
        builder.setCalendarConstraints(constraintsBuilderRange.build());


        // Building the date picker dialog
        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {

            // Retrieving the selected start and end dates
            Long startDate = selection.first;
            Long endDate = selection.second;

            // Formating the selected dates as strings
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String startDateString = sdf.format(new Date(startDate));
            String endDateString = sdf.format(new Date(endDate));


            // Creating the date range string
            String selectedDateRange = startDateString + " - " + endDateString;
            binding.one.setText(startDateString);
            binding.two.setText(endDateString);
            Toast.makeText(getApplicationContext(), "Your selected Start date  and End date is"+selectedDateRange, Toast.LENGTH_SHORT).show();

            List<Date> multipleSelection=getDates(startDateString,endDateString);
            for(int i=0;i<multipleSelection.size();i++){
                Log.e("ListOfDates==",""+multipleSelection.get(i));

                Date date =multipleSelection.get(i);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String strDate = dateFormat.format(date);
                Log.e("DateFormat==",""+strDate);
            }
        });

        // Showing the date picker dialog
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd hh:mm a zzz");
        Date date = new Date();
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+6:00"));
        return sdf.format(date);
    }



    private static List<Date> getDates(String dateString1, String dateString2)
    {
        ArrayList<Date> dates = new ArrayList<Date>();
        SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yyyy");

        Date date1 = null;
        Date date2 = null;

        try {
            date1 = df1 .parse(dateString1);
            date2 = df1 .parse(dateString2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while(!cal1.after(cal2))
        {
            dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
        }
        return dates;
    }

    private void initUi() {

        binding.btnToday.setOnClickListener(this);
        binding.btnYesterday.setOnClickListener(this);
        binding.btnLastWeek.setOnClickListener(this);
        binding.btnLastMonth.setOnClickListener(this);
//
//        adapter = new StatsAppListAdapter(appUsageList, getApplicationContext());
//        binding.statsAppsList.setLayoutManager(new LinearLayoutManager(UsesStatsActivity.this));
//        binding.statsAppsList.setAdapter(adapter);
    }

    public void getAppListFromDb(String currentDate) {
        dayWiseStatsList.clear();
        appUsageList.clear();
        dialog.show();
        DocumentReference docRef = db.collection(usersEmail).document(currentDate);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    try {
                        if (document.exists()) {
                            statsDataModel = document.toObject(UsesStatsDataModel.class);
                            dayWiseStatsList.addAll(statsDataModel.getDataArrayList());

                            /*ArrayList<String> dates = new ArrayList<>();
                            dates.add("26:02:2024");
                            dates.add("25:02:2024");
                            dates.add("28:02:2024");
                            getRangedDateStats(dates);*/

                            for (StatsModel statsModel : dayWiseStatsList) {
                                if (!appUsageList.contains(statsModel.getDataArrayList())) {
                                    appUsageList.addAll(statsModel.getDataArrayList());
                                }
                            }



                            adapter = new StatsAppListAdapter(appUsageList, getApplicationContext());
                            binding.statsAppsList.setLayoutManager(new LinearLayoutManager(UsesStatsActivity.this));
                            binding.statsAppsList.setAdapter(adapter);

                        } else {
                            Toast.makeText(UsesStatsActivity.this, "No Apps found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i("TAG", "get failed with ", task.getException());
                }
            }
        });
        dialog.dismiss();
    }

    /*
        Yesterday or last 7 days or last month dates should pass as array to getRangedDateStats() method
        Ex Yesterday : [26:02:2023]
        Ex last 7days : [20:02:2023,21:02:2023,22:02:2023,23:02:2023,24:02:2023,25:02:2023,26:02:2023]
        We have ${dayWiseStatsList} hold date and daily stats
     */
    private void getRangedDateStats(ArrayList<String> dates) {
        Log.d("##getRangedDateStats",dates.toString());
        if (finalList != null || !finalList.isEmpty()) {
            finalList.clear();
        }
//        ArrayList<StatsModel> resultStats = new ArrayList();
        for (int x= 0;x<dayWiseStatsList.size();x++) {

            Log.d("##getRangedDateStats",dayWiseStatsList.get(x).getDate()+ "Position :: "+x);
            if (dates.contains(dayWiseStatsList.get(x).getDate())) {
                Log.d("##getRangedDateStats", "Date :: " + dayWiseStatsList.get(x).getDate());
                finalList.add(dayWiseStatsList.get(x));
            }
            Log.d("##getRangedDateStats","resultStatsList Size:: "+ finalList.size());


        }

        //use resultStats list for updating data to Recyclerview
        Log.d("##getRangedDateStats", "Size :: " + finalList.size());
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

            onBackPressed();


        return super.onOptionsItemSelected(item);
    }
    private ArrayList<AppUsesData> getAllStats() {
        ArrayList<AppUsesData> resultStats = new ArrayList();

        Log.d("##getAllStats","resultStatsList Size:: "+ finalList.size());
        for (StatsModel stats : finalList) {

            for (AppUsesData appData : stats.getDataArrayList()) {

                if (resultStats == null || resultStats.isEmpty()) {
                    resultStats.addAll(stats.getDataArrayList());
                    Log.d("##getAllStats","resultStats Size:: "+resultStats.size());

                } else {
                    for (int i = 0; i < resultStats.size(); i++) {
                        AppUsesData tempModel = resultStats.get(i);
                        Log.d("##",tempModel.getAppName());
                        if (tempModel.getBundle_id().equals(appData.getBundle_id())) {
                            resultStats.get(i).setStartTime(appData.getStartTime() + tempModel.getStartTime());
                            resultStats.get(i).setEndTime(appData.getEndTime() + tempModel.getEndTime());
                            resultStats.get(i).setUsageTime(appData.getUsageTime() + tempModel.getUsageTime());
                            resultStats.get(i).setLaunchCount(appData.getLaunchCount() + tempModel.getLaunchCount());
                            resultStats.add(i, resultStats.get(i));
                        }
                    }
                }

            }

        }

        //use resultStats list for updating data to Recyclerview
        Log.d("##getRangedDateStats", "Size :: " + resultStats.size());
        return resultStats;
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
                getAppListFromDb(currentDate);

                binding.btnToday.setBackgroundResource(R.drawable.active_round);
                binding.btnYesterday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastWeek.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastMonth.setBackgroundResource(R.drawable.non_active_round);

                break;
            case R.id.btn_yesterday:
                ArrayList<String> dateList = getYesterdayDateInFormat();
//                Log.d("DateList  -D=  ", dateList.toString());
//                getRangedDateStats(dateList);
//                adapter.addItems(getAllStats());

                getAppListFromDb(dateList.get(0));

                binding.btnToday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnYesterday.setBackgroundResource(R.drawable.active_round);
                binding.btnLastWeek.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastMonth.setBackgroundResource(R.drawable.non_active_round);

                break;
            case R.id.btn_last_week:
                ArrayList<String> lastWeekDates = getLastWeekDatesInFormat();
                Log.d("DateList:: -W=  ", lastWeekDates.toString());
//                getRangedDateStats(lastWeekDates);
//                adapter.addItems(getAllStats());
                getAppTotalListFromDb();

                binding.btnToday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnYesterday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastWeek.setBackgroundResource(R.drawable.active_round);
                binding.btnLastMonth.setBackgroundResource(R.drawable.non_active_round);
                break;
            case R.id.btn_last_month:
                appUsageList.clear();
                ArrayList<String> lastMonthDates = getLastMonthDatesInFormat();
                Log.d("DateList :: -M=  ", lastMonthDates.toString());
//                getRangedDateStats(lastMonthDates);
//                adapter.addItems(getAllStats());

                getAppTotalListFromDb();
                binding.btnToday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnYesterday.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastWeek.setBackgroundResource(R.drawable.non_active_round);
                binding.btnLastMonth.setBackgroundResource(R.drawable.active_round);

                break;
            default:
                Log.d("##", "noting Clicled");
        }
    }


    public void getAppTotalListFromDb() {
        dayWiseStatsList.clear();
        appUsageList.clear();
        dialog.show();
        db.collection(usersEmail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    statsDataModel = document.toObject(UsesStatsDataModel.class);
                                    dayWiseStatsList.addAll(statsDataModel.getDataArrayList());
                                    for (StatsModel statsModel : dayWiseStatsList) {
                                        if (!appUsageList.contains(statsModel.getDataArrayList())) {
                                            appUsageList.addAll(statsModel.getDataArrayList());
                                        }
                                    }
                                    adapter = new StatsAppListAdapter(appUsageList, getApplicationContext());
                                    binding.statsAppsList.setLayoutManager(new LinearLayoutManager(UsesStatsActivity.this));
                                    binding.statsAppsList.setAdapter(adapter);

                                } else {
                                    Toast.makeText(UsesStatsActivity.this, "No Apps found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {

                        }
                    }
                });
        dialog.dismiss();
    }
}