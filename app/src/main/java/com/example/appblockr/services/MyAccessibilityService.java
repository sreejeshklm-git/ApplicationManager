package com.example.appblockr.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.appblockr.broadcast.ReceiverApplock;
import com.example.appblockr.shared.SharedPrefUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyAccessibilityService extends AccessibilityService {
    AccessibilityServiceInfo info;

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();
        SharedPrefUtil prefUtil = new SharedPrefUtil(this);
        List<String> lockedApps = prefUtil.getLockedAppsList();
        if(lockedApps.size()>0) {
            String[] array = lockedApps.toArray(new String[0]);
            Log.e("arry", array.toString());
            info = getServiceInfo();
             info.packageNames =array;
            //info.packageNames = new String[]{"com.whatsapp"};
            setServiceInfo(info);
        }

    }
    public static void killApp(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            Log.e("packName inside kill",packageName.toString());
            activityManager.killBackgroundProcesses(packageName);
        }
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.e("packName b4",accessibilityEvent.getPackageName().toString());

        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Check if the user has switched to a different app

            CharSequence packageName = accessibilityEvent.getPackageName();
            Log.e("packName", packageName.toString());
            if (packageName != null) {
                // Trigger WorkManager task here
                // triggerWork();
                Log.e("packName inside", packageName.toString());
                long endTime = System.currentTimeMillis() + 210;

                    synchronized (this) {
                        try {
                            Intent intent = new Intent(this, ReceiverApplock.class);
                            sendBroadcast(intent);
                            Log.e("broadcast", " running ");
                            wait(endTime - System.currentTimeMillis());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // killApp(this, packageName.toString());

            }
        }
    }

    @Override
    public void onInterrupt() {
        // Handle interruption
    }

    private void triggerWork() {
        // Create a WorkRequest and enqueue it with WorkManager
       // OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
              //  .build();
       // WorkManager.getInstance(this).enqueue(workRequest);
    }


}