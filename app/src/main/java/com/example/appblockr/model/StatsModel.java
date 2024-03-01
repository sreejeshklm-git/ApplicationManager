package com.example.appblockr.model;

import java.util.ArrayList;

public class StatsModel {
    String date;
    ArrayList<AppUsesData> appUsesDataArrayList;

    public StatsModel() {}
    public StatsModel(String date, ArrayList<AppUsesData> appUsesDataArrayList) {
        this.date = date;
        this.appUsesDataArrayList = appUsesDataArrayList;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<AppUsesData> getDataArrayList() {
        return appUsesDataArrayList;
    }

    public void setDataArrayList(ArrayList<AppUsesData> appUsesDataArrayList) {
        this.appUsesDataArrayList = appUsesDataArrayList;
    }


    public int compareTo(StatsModel app) {
        return this.getDate().compareTo(app.date);
    }
}
