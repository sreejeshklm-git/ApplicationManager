package com.example.appblockr.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appblockr.R;
import com.example.appblockr.model.AppData;

import java.util.ArrayList;



public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    ArrayList applicationName;
    Context context;
    ArrayList<AppData> appData;

    // Constructor for initialization
    public UserAdapter(ArrayList<AppData> appData) {
        this.appData=appData;
//        this.applicationName = applicationName;
    }


    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_name_inflater, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // Binding data to the into specified position
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TypeCast Object to int type
        holder.applicationNameTxt.setText(appData.get(position).getAppName());
        holder.applicationPackage.setText(appData.get(position).getBundle_id());
        holder.durationTxt.setText("Duration: "+""+appData.get(position).getDuration());
    }

    @Override
    public int getItemCount() {
        // Returns number of items
        // currently available in Adapter
        return appData.size();
    }

    // Initializing the Views
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView applicationNameTxt;
        TextView applicationPackage;
        TextView durationTxt;
        public ViewHolder(View view) {
            super(view);
            applicationNameTxt =view.findViewById(R.id.examName);
            durationTxt=view.findViewById(R.id.examMessage);
            applicationPackage=view.findViewById(R.id.examDate);
        }
    }
}
