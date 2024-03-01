package com.example.appblockr.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appblockr.R;
import com.example.appblockr.ui.adduser.AppListActivity;

import java.util.ArrayList;


public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.ViewHolder> {
    ArrayList<String> userName,emailList;
    Context context;

    // Constructor for initialization
    public AdminAdapter(Context context, ArrayList<String> userName, ArrayList<String> emailList) {
        this.context = context;
       this.userName = userName;
       this.emailList=emailList;
    }


    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.username_inflater, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // Binding data to the into specified position
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TypeCast Object to int type
        holder.userName.setText(userName.get(position));
        holder.userEmail.setText(emailList.get(position));
        holder.parentView.setOnClickListener(view -> {
                 Intent intent= new Intent(context, AppListActivity.class);
                 intent.putExtra("email", emailList.get(position));
               context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        // Returns number of items
        // currently available in Adapter
        return userName.size();
    }

    // Initializing the Views
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userEmail;
        CardView parentView;

        public ViewHolder(View view) {
            super(view);
            userName =view.findViewById(R.id.userName);
            userEmail = view.findViewById(R.id.userEmail);
            parentView =view.findViewById(R.id.parent_view);

        }
    }
}
