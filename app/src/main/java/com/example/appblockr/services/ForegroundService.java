package com.example.appblockr.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.appblockr.broadcast.ReceiverApplock;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ForegroundService extends Service {

    private Timer timer = new Timer();
    private boolean isTimerStarted=false;
    private long timerReload = 500L;

    private ArrayList currentAppActivityList = new ArrayList();

    public void onCreate() {
        super.onCreate();
        String channelId = "AppLock-10";
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(channelId, (CharSequence)"Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
        }
        Object var10000 = this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (var10000 == null) {
            throw new NullPointerException("null cannot be cast to non-null type android.app.NotificationManager");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ((NotificationManager) var10000).createNotificationChannel(channel);

                Notification var4 = (new NotificationCompat.Builder((Context) this, channelId)).setContentTitle((CharSequence) "").setContentText((CharSequence) "").build();
                //Intrinsics.checkNotNullExpressionValue(var4, "NotificationCompat.Build…etContentText(\"\").build()");
                Notification notification = var4;
                this.startForeground(1, notification);
                Log.e("amma", "amma");
                this.startMyOwnForeground();
            }
        }
    }

    public int onStartCommand(@NotNull Intent intent, int flags, int startId) {
      //  Intrinsics.checkNotNullParameter(intent, "intent");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private final void startMyOwnForeground() {
        this.timerRun();
    }
    private final void timerRun() {
        this.timer.scheduleAtFixedRate((TimerTask)(new TimerTask() {
            public void run() {
                ForegroundService.this.setTimerStarted(true);
                ForegroundService.this.isServiceRunning();
            }
        }), 0L, this.timerReload);
    }
    public final void setTimerStarted(boolean var1) {
        this.isTimerStarted = var1;
    }
    public final void isServiceRunning() {
        long endTime = System.currentTimeMillis() + 1000;
        synchronized (this) {
            try {
                 Intent intent = new Intent(this, ReceiverApplock.class);
                sendBroadcast(intent);

                wait(endTime - System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onDestroy() {
        this.timer.cancel();
        super.onDestroy();
    }
}

