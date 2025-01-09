package com.example.taskmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra("task_name");
        String timeRemaining = intent.getStringExtra("time_remaining");

        // Utiliser TaskNotificationManager pour ajouter la notification
        TaskNotificationManager taskNotificationManager = new TaskNotificationManager(context);
        taskNotificationManager.addNotification(taskName, timeRemaining);



    }
}
