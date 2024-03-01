package com.example.appblockr.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.appblockr.R;
import com.example.appblockr.model.AppUsesData;

import java.util.ArrayList;

public class StatsAppListAdapter extends
        RecyclerView.Adapter<StatsAppListAdapter.ViewHolder> {

    private ArrayList<AppUsesData> appUsageList;
    private Context context;


    // Pass in the contact array into the constructor
    public StatsAppListAdapter(ArrayList<AppUsesData> appUsageList, Context context) {
        this.appUsageList = appUsageList;
        this.context = context;
    }

    public void addItems(ArrayList<AppUsesData> list){
        appUsageList.clear();
        appUsageList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View contactView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stats_layout, parent, false);
        return new ViewHolder(contactView);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppUsesData appInfo = appUsageList.get(position);
        holder.appName.setText(appInfo.getAppName());
        holder.appPackage.setText(appInfo.getBundle_id().toString());
        holder.duration.setText(appInfo.getUsageTime());
        holder.clickCount.setText(appInfo.getLaunchCount().toString());
        /*if (appInfo.getIsAppLocked()) {
            Drawable img = context.getResources().getDrawable(R.drawable.baseline_lock_24);
            holder.isLocked.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            holder.isLocked.setText("Locked");
        } else {
            Drawable img = context.getResources().getDrawable(R.drawable.baseline_lock_open_24);
            holder.isLocked.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
            holder.isLocked.setText("Open");;
        }*/
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return appUsageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView appName;
        public TextView appPackage;
        public TextView duration;
        public TextView clickCount;
        public TextView isLocked;

        public ViewHolder(View itemView) {
            super(itemView);
            appName = (TextView) itemView.findViewById(R.id.app_name);
            appPackage = (TextView) itemView.findViewById(R.id.app_package);
            duration = (TextView) itemView.findViewById(R.id.duration);
            clickCount = itemView.findViewById(R.id.clickCount);
            isLocked = itemView.findViewById(R.id.isLocked);

        }
    }
}
