package com.example.taskmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private Switch themeSwitch;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().hide();


        // Initialisation du SharedPreferences pour stocker la préférence de thème
        sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);

        // Récupérer l'état actuel du thème
        int currentMode = sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(currentMode);

        // Initialisation du Switch
        themeSwitch = findViewById(R.id.themeSwitch);

        // Définir l'état initial du Switch en fonction du mode actuel
        themeSwitch.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        // Définir un OnCheckedChangeListener pour le Switch
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newMode;
            if (isChecked) {
                // Passer en mode sombre
                newMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                // Passer en mode clair
                newMode = AppCompatDelegate.MODE_NIGHT_NO;
            }

            // Appliquer le nouveau mode
            AppCompatDelegate.setDefaultNightMode(newMode);

            // Sauvegarder la préférence dans SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("theme", newMode);
            editor.apply();
        });

        // Récupération du TextView
        TextView manageCategoriesButton = findViewById(R.id.manage_categories_button);
        manageCategoriesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageCategoriesActivity.class);
            startActivity(intent);
        });
    }
}
