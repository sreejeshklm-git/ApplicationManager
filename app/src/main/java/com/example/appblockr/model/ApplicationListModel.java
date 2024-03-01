package com.example.appblockr.model;

import java.util.ArrayList;

public class ApplicationListModel {
    String email;
    ArrayList<AppData> dataArrayList;

    public ApplicationListModel() {}
    public ApplicationListModel(String email, ArrayList<AppData> dataArrayList) {
        this.email = email;
        this.dataArrayList = dataArrayList;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<AppData> getDataArrayList() {
        return dataArrayList;
    }

    public void setDataArrayList(ArrayList<AppData> dataArrayList) {
        this.dataArrayList = dataArrayList;
    }

    public void setIcon(ArrayList<AppData> dataArrayList) {
        this.dataArrayList = dataArrayList;
    }

    public int compareTo(ApplicationListModel app) {
        return this.getEmail().compareTo(app.email);
    }
}
