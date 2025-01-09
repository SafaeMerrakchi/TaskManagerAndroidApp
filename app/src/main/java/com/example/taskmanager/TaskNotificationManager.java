package com.example.taskmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskNotificationManager {
    private static final String PREFS_NAME = "NotificationsPrefs";
    private static final String KEY_NOTIFICATIONS = "notifications";

    private List<NotificationItem> notifications;
    private Context context;

    public TaskNotificationManager(Context context) {
        this.context = context;
        this.notifications = loadNotifications();
    }

    // Charger les notifications depuis SharedPreferences
    private List<NotificationItem> loadNotifications() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_NOTIFICATIONS, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<NotificationItem>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    // Sauvegarder les notifications dans SharedPreferences
    private void saveNotifications() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(notifications);
        editor.putString(KEY_NOTIFICATIONS, json);
        editor.apply();
    }

    // Ajouter une notification
    public void addNotification(String taskName, String timeRemaining) {
        String currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        notifications.add(new NotificationItem(currentDateTime, "Reminder!! Don't forget task : ''" + taskName + "''. " + timeRemaining));
        saveNotifications(); // Sauvegarder la liste mise à jour
        showNotification(taskName, timeRemaining); // Afficher la notification
    }

    // Afficher la notification
    public void showNotification(String taskName, String timeRemaining) {
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra("task_name", taskName);
        intent.putExtra("time_remaining", timeRemaining);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Créer la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "TASK_MANAGER_CHANNEL")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Task Reminder!!")
                .setContentText("Don't forget : " + taskName + " (" + timeRemaining + ")")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Pour Android 8.0 et plus, utiliser un canal de notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("TASK_MANAGER_CHANNEL",
                    "Task Manager Notifications", NotificationManager.IMPORTANCE_HIGH);
            // Obtenez l'instance du NotificationManager système
            android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        // Afficher la notification
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    // Récupérer les notifications pour l'affichage dans la NotificationActivity
    public List<NotificationItem> getNotifications() {
        Collections.reverse(notifications); // Inverser l'ordre pour que les plus récentes apparaissent en haut
        return notifications;
    }
}
