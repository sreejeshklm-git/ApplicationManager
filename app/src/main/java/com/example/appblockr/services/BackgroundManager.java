package com.example.appblockr.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.example.appblockr.broadcast.ReceiverApplock;
import com.example.appblockr.broadcast.RestartServiceWhenStopped;

public class BackgroundManager {
    private static final int period = 1000;
    private static final int ALARM_ID = 159874;


    private static BackgroundManager instance;
    private Context context;

    public static BackgroundManager getInstance() {
        if (instance == null) {
            instance = new BackgroundManager();
        }
        return instance;
    }

    public BackgroundManager init(Context ctx) {
        context = ctx;
        return this;
    }

    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MIN_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public static void clearQueue(Context context) {
        JobScheduler jobScheduler = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            jobScheduler.cancelAll();
        }
    }
    public void startService() {
        Log.e("start","service");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isServiceRunning(ServiceApplockJobIntent.class)) {
                //clearQueue(context);
                Log.e("start","service started");
                //Intent intent = new Intent(context, ServiceApplockJobIntent.class);
                //ServiceApplockJobIntent.enqueueWork(context, intent);

            }
        } else {
            if (!isServiceRunning(ServiceApplock.class)) {
                context.startService(new Intent(context, ServiceApplock.class));
            }
        }
    }

    public void stopService(Class<?> serviceClass) {
        if (isServiceRunning(serviceClass)) {
            context.stopService(new Intent(context, serviceClass));
        }
    }

    public void startAlarmManager() {
        Log.e("alaram","manager");
        Intent intent = new Intent(context, RestartServiceWhenStopped.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, intent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, period,period, pendingIntent);
    }

    public void stopAlarm() {
        Intent intent = new Intent(context, RestartServiceWhenStopped.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, intent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

}
