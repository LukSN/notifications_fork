package com.mythichelm.localnotifications.factories;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import com.mythichelm.localnotifications.LocalNotificationsPlugin;
import com.mythichelm.localnotifications.TestActivity;
import com.mythichelm.localnotifications.entities.NotificationAction;
import com.mythichelm.localnotifications.entities.NotificationSettings;

public class NotificationFactory implements INotificationFactory {

    @Override
    public Notification createNotification(NotificationSettings settings, Context context) {
        LocalNotificationsPlugin.customLog("Creating Notification from settings: "
                + new Gson().toJson(settings));

        int appIconResId = getApplicationIconResourceId(context);

        Intent notificationIntent = new Intent(context, TestActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, settings.Channel)
                .setContentTitle(settings.Title)
                .setContentText(settings.Body)
                .setSmallIcon(appIconResId)
                .setOngoing(settings.IsOngoing)
                .setAutoCancel(false)
                .setContentIntent(settings.OnNotificationClick.getIntent(context))
                .setPriority(settings.Priority);
                //.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        setVibratePattern(builder, settings);
        setLargeIcon(builder, settings);
        setTicker(builder, settings);
        addActions(builder, settings, context);

        LocalNotificationsPlugin.customLog("Finished creating Notification from NotificationSettings");
        Notification notification = builder.build();
        return  notification;
    }

    private void setVibratePattern(NotificationCompat.Builder builder, NotificationSettings settings) {
        if (settings.UseDefaultVibratePattern) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        } else {
            builder.setVibrate(settings.VibratePattern);
        }
    }

    private void addActions(NotificationCompat.Builder builder, NotificationSettings settings,
                            Context context) {
        for (NotificationAction extraAction : settings.ExtraActions) {
            PendingIntent intent = extraAction.getIntent(context);
            NotificationCompat.Action action = new NotificationCompat.Action
                    .Builder(0, extraAction.actionText, intent).build();
            builder.addAction(action);
        }
    }

    private void setTicker(NotificationCompat.Builder builder, NotificationSettings settings) {
        if (settings.Ticker != null)
            builder.setTicker(settings.Ticker);
    }

    private void setLargeIcon(NotificationCompat.Builder builder, NotificationSettings settings) {
        if (settings.LargeIcon != null)
            builder.setLargeIcon(settings.LargeIcon);
    }

    private int getApplicationIconResourceId(Context context) {
        int resId = 0;

        try {
            resId = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                    .icon;
        } catch (PackageManager.NameNotFoundException ignored) {}

        return resId;
    }

}
