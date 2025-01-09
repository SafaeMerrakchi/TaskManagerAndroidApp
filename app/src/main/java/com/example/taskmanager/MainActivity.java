package com.example.taskmanager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.Manifest;


public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    private FloatingActionButton fabAddTask;


    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabAddTask = findViewById(R.id.addButton);


        getSupportActionBar().hide();

        // Initialiser le lanceur de demande de permission
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        // Gérer le cas où l'utilisateur refuse la permission
                        Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Vérifiez et demandez la permission si nécessaire
        checkAndRequestNotificationPermission();


        // Initialisation des composants
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Configuration de l'adaptateur pour ViewPager2
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // Connexion entre le TabLayout et le ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Dashboard");
                        break;
                    case 1:
                        tab.setText("Tasks");
                        break;
                    case 2:
                        tab.setText("Thanks");
                        break;
                }
            }
        }).attach();

        // Récupérer le LottieAnimationView
        LottieAnimationView settingsButton = findViewById(R.id.settingsButton);

        // Définir un OnClickListener pour ouvrir SettingsActivity
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent); // Lance l'activité des paramètres
        });

        LottieAnimationView searchButton = findViewById(R.id.searchButton);
        LottieAnimationView notificationButton = findViewById(R.id.notificationButton);

        // Définir un OnClickListener pour ouvrir SearchActivity
        searchButton.setOnClickListener(v -> {
            Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(searchIntent); // Lance l'activité de recherche
        });

        // Définir un OnClickListener pour ouvrir NotificationActivity
        notificationButton.setOnClickListener(v -> {
            Intent notificationIntent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(notificationIntent); // Lance l'activité des notifications
        });



        // Configurer le bouton flottant
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });

    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Demander la permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
