package com.example.taskmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    public static void showNotification(Context context, String title, String message) {
        // Créer un Intent pour l'activité NotificationActivity
        Intent activityIntent = new Intent(context, NotificationActivity.class);
        activityIntent.putExtra("task_name", title);
        activityIntent.putExtra("time_remaining", message);

        // Utilisez un PendingIntent pour ouvrir l'activité
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Récupérer le NotificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Créer le canal de notification (si nécessaire)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "task_reminder_channel",
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Créer et afficher la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_reminder_channel")
                .setSmallIcon(R.drawable.ic_notification)  // Assurez-vous d'avoir une icône valide
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Afficher la notification
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }


}
