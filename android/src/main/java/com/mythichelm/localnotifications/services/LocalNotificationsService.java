package com.mythichelm.localnotifications.services;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.IntentService;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.MainThread;
import androidx.core.app.NotificationCompat;

import com.mythichelm.localnotifications.GenerateLocalNotificationsTask;
import com.mythichelm.localnotifications.LocalNotificationsPlugin;
import com.mythichelm.localnotifications.R;
import com.mythichelm.localnotifications.TestActivity;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.common.MethodChannel;

public class LocalNotificationsService extends IntentService {
    private static MethodChannel sSharedChannel;

    public LocalNotificationsService() {
        super("LocalNotificationsService");
    }

    public static MethodChannel getSharedChannel() {
        return sSharedChannel;
    }

    public static void setSharedChannel(MethodChannel channel) {
        if (sSharedChannel != null && sSharedChannel != channel) {
            Log.d(LocalNotificationsPlugin.LOGGING_TAG, "sSharedChannel tried to overwrite an existing Registrar");
            return;
        }
        Log.d(LocalNotificationsPlugin.LOGGING_TAG, "sSharedChannel set");
        sSharedChannel = channel;
    }

    @Override
    public void onHandleIntent(final Intent intent) {
        final LocalNotificationsService service = this;

        LocalNotificationsPlugin.customLog("LocalNotificationsService handling intent in the background");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(sSharedChannel == null){
                    SharedPreferences preferences = service.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE);
                    System.out.println("QueryVocabs: " + preferences.getString("flutter.queryvocabs", "[]"));
                    System.out.println("QueryTimes: " + preferences.getLong("flutter.querytimes", 0));

                    System.out.println("Prefssssss: " + preferences.getBoolean("flutter.afterterminate", false));

                    //sendNotification();

                    preferences.edit().putString("flutter.noticallback", intent.getStringExtra(LocalNotificationsPlugin.PAYLOAD_KEY)).apply();

                    //NotificationService service1 = new NotificationService();

                    Intent intent = new Intent(service, TestActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    /*FlutterEngine flutterEngine = new FlutterEngine(service);

                    flutterEngine
                            .getDartExecutor()
                            .executeDartEntrypoint(
                                    DartExecutor.DartEntrypoint.createDefault()
                            );*/

                    return;

                    //MethodChannel channel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), LocalNotificationsPlugin.CHANNEL_NAME);
                    // setSharedChannel(channel);
                }

                LocalNotificationsPlugin.handleIntent(intent);
            }
        });
    }

    private void sendNotification() {
        Context ctx  = getApplicationContext();


        Intent intent = new Intent(ctx, LocalNotificationsService.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b  = new NotificationCompat.Builder(ctx);

        b.setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.vam_logo_small)
                .setTicker("Hearty365")
                .setContentTitle("Default notification")
                .setContentText("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentInfo("Info");

        NotificationManager notificationManager  = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            b.setChannelId(channelId);
        }

        notificationManager.notify(0, b.build());
    }
}
