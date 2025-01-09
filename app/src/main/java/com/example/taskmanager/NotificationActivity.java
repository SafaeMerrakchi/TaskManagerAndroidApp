package com.example.taskmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.List;


public class NotificationActivity extends AppCompatActivity {
    private TaskNotificationManager taskNotificationManager;
    private NotificationAdapter adapter;
    private TextView helpText;  // Référence au TextView d'aide



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        getSupportActionBar().hide();

        // Initialiser le TaskNotificationManager
        taskNotificationManager = new TaskNotificationManager(this);

        // Récupérer la liste des notifications
        List<NotificationItem> notifications = taskNotificationManager.getNotifications();

        // Configurer le RecyclerView
        RecyclerView recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications);
        recyclerView.setAdapter(adapter);


        LottieAnimationView helpButton = findViewById(R.id.helpButton);


        helpText = findViewById(R.id.helpText);

        // Configurer le clic sur le bouton Help
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helpText.getVisibility() == View.VISIBLE) {
                    helpText.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(() -> helpText.setVisibility(View.GONE))
                            .start();
                } else {
                    helpText.setAlpha(0f);
                    helpText.setVisibility(View.VISIBLE);
                    helpText.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                }
            }
        });
    }
}
