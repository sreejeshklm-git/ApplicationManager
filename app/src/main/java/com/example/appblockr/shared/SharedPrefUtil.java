package com.example.appblockr.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.appblockr.model.BlockProfile;

import java.util.ArrayList;
import java.util.List;

public class SharedPrefUtil {
    private static final String SHARED_APP_PREFERENCE_NAME = "SharedPref";
    Context cxt;
    private final String EXTRA_LAST_APP = "EXTRA_LAST_APP";
    private final String USER_TYPE= "user_type";
    private final String USER_NAME= "userName";
    private final String PASSWORD= "password";
    private final String EMAIL = "email";
    private final SharedPreferences pref;
    private SharedPreferences.Editor mEditor;
    BlockProfile blockProfile;

    public SharedPrefUtil(Context context) {
        this.pref = context.getSharedPreferences(SHARED_APP_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPrefUtil getInstance(Context context) {
        return new SharedPrefUtil(context);
    }


    public void putString(String key, String value) {
        pref.edit().putString(key, value).apply();
    }

    public void putInteger(String key, int value) {
        pref.edit().putInt(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        pref.edit().putBoolean(key, value).apply();
    }

    public String getString(String key) {
        return pref.getString(key, "");
    }

    public int getInteger(String key) {
        return pref.getInt(key, 0);
    }

    public boolean getBoolean(String key) {
        return pref.getBoolean(key, false);
    }

    public String getLastApp() {
        return getString(EXTRA_LAST_APP);
    }

    public void setLastApp(String packageName) {
        putString(EXTRA_LAST_APP, packageName);
    }

    public void clearLastApp() {
        pref.edit().remove(EXTRA_LAST_APP);
    }

    //add apps to locked list
    public void createLockedAppsList(List<String> appList) {
        for (int i = 0; i < appList.size(); i++) {
            putString("app_" + i, appList.get(i));
        }
        putInteger("listSize", appList.size());
    }

    //get apps from locked list
    public List<String> getLockedAppsList() {
        List<String> temp = new ArrayList<>();
        int size = getInteger("listSize");
        for (int i = 0; i < size; i++) {
            temp.add(getString("app_" + i));
        }
        return temp;
    }

    public void setLockedAppsListProfile(List<String> appList) {
        for (int i = 0; i < appList.size(); i++) {
            putString("profileApp_" + i, appList.get(i));
        }
        putInteger("profileListSize", appList.size());
    }

    public List<String> getLockedAppsListProfile() {
        List<String> temp = new ArrayList<>();
        int size = getInteger("profileListSize");
        for (int i = 0; i < size; i++) {
            temp.add(getString("profileApp_" + i));
        }
        return temp;
    }
    public void setDaysList(List<String> daysList) {
        for (int i = 0; i < daysList.size(); i++) {
            putString("day_" + i, daysList.get(i));
        }
        putInteger("daysListSize", daysList.size());
    }

    public List<String> getDaysList() {
        List<String> temp = new ArrayList<>();
        int size = getInteger("daysListSize");
        for (int i = 0; i < size; i++) {
            temp.add(getString("day_" + i));
        }
        return temp;
    }
    //start time
    public void setStartTimeHour(String date) {
       putString("start_hour", date);
    }
    public String getStartTimeHour() {
       return getString("start_hour");
    }
    public void setStartTimeMinute(String date) {
        putString("start_minute", date);
    }
    public String getStartTimeMinute() {
        return getString("start_minute");
    }
    //endTime
    public void setEndTimeHour(String date) {
         putString("end_hour", date);
    }
    public String getEndTimeHour() {
        return getString("end_hour");
    }
    public void setEndTimeMinute(String date) {
        putString("end_minute", date);
    }
    public String getEndTimeMinute() {
        return getString("end_minute");
    }

    public void putUserType(String key, String value) {
        pref.edit().putString(key, value).apply();
    }
    public String getUserType(String userType) {
        return getString(userType);
    }
    public void setUserType(String userType) {
        putUserType(USER_TYPE, userType);
    }
    public void putUserName(String key, String value) {
        pref.edit().putString(key, value).apply();
    }
    public String getUserName(String userName){
        return getString(userName);
    }
    public void setUserName(String userName) {
        putUserName(USER_NAME, userName);
    }
    public void putPassword(String key, String value) {
        pref.edit().putString(key, value).apply();
    }
    public String getPassword(String password){
        return getString(password);
    }
    public void setPassword(String password) {
        putPassword(PASSWORD, password);
    }
    public void putEmail(String key, String value) {
        pref.edit().putString(key, value).apply();
    }
    public String getEmail(String email){
        return getString(email);
    }
    public void setEmail(String email) {
        putEmail(EMAIL, email);
    }
}