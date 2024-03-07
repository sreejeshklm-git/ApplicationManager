package com.example.appblockr.model;

import java.util.ArrayList;

public class StatsModel {
    ArrayList<AppUsesData> appUsageList;

    public StatsModel() {}
    public StatsModel(ArrayList<AppUsesData> appUsesDataArrayList) {

        this.appUsageList = appUsesDataArrayList;
    }

    public ArrayList<AppUsesData> getDataArrayList() {
        return appUsageList;
    }

    public void setDataArrayList(ArrayList<AppUsesData> appUsesDataArrayList) {
        this.appUsageList = appUsesDataArrayList;
    }

}
