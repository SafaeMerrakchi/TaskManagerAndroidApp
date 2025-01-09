package com.example.taskmanager;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.androidplot.util.PixelUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private PieChart pieChartOverall;
    private PieChart pieChartByCategory;
    private TaskDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        getSupportActionBar().hide();

        pieChartOverall = findViewById(R.id.pieChartOverall);
        pieChartByCategory = findViewById(R.id.pieChartByCategory);
        dbHelper = new TaskDBHelper(this);

        // Charger et afficher les statistiques
        loadOverallStatistics();
        loadCategoryStatistics();
    }

    private void loadOverallStatistics() {
        pieChartOverall.clear();

        // Récupérer les statistiques des tâches
        int completed = dbHelper.countTasksByStatus("completed");
        int inProgress = dbHelper.countTasksByStatus("in_progress");
        int overdue = dbHelper.countTasksByStatus("overdue");

        int total = completed + inProgress + overdue;
        if (total == 0) {
            total = 1; // Éviter la division par zéro
        }

        // Ajouter les segments avec pourcentage et nombre exact
        addSegmentWithLabel(pieChartOverall, "Completed", completed, total, Color.parseColor("#4CAF50")); // Vert
        addSegmentWithLabel(pieChartOverall, "In Progress", inProgress, total, Color.parseColor("#FFC107")); // Jaune
        addSegmentWithLabel(pieChartOverall, "Overdue", overdue, total, Color.parseColor("#F44336")); // Rouge

        pieChartOverall.getBackgroundPaint().setColor(Color.TRANSPARENT);
        pieChartOverall.redraw();

        // Ajouter les légendes sous le graphique
        addLegend(pieChartOverall, "Completed", completed, total, Color.parseColor("#4CAF50"));
        addLegend(pieChartOverall, "In Progress", inProgress, total, Color.parseColor("#FFC107"));
        addLegend(pieChartOverall, "Overdue", overdue, total, Color.parseColor("#F44336"));
    }

    private void loadCategoryStatistics() {
        pieChartByCategory.clear();

        // Récupérer les catégories et le nombre de tâches incomplètes pour chaque catégorie
        List<String> categories = dbHelper.getCategories();
        Map<String, Integer> categoryCounts = new HashMap<>();
        int totalIncomplete = 0;

        for (String category : categories) {
            int count = dbHelper.countIncompleteTasksByCategory(category);
            categoryCounts.put(category, count);
            totalIncomplete += count;
        }

        if (totalIncomplete == 0) {
            totalIncomplete = 1; // Éviter la division par zéro
        }

        // Ajouter les segments pour chaque catégorie
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            String category = entry.getKey();
            int count = entry.getValue();
            addSegmentWithLabel(pieChartByCategory, category, count, totalIncomplete, getCategoryColor(category));
        }

        pieChartByCategory.getBackgroundPaint().setColor(Color.TRANSPARENT);
        pieChartByCategory.redraw();

        // Ajouter les légendes sous le graphique par catégorie
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            String category = entry.getKey();
            int count = entry.getValue();
            addLegend(pieChartByCategory, category, count, totalIncomplete, getCategoryColor(category));
        }
    }

    private void addLegend(PieChart pieChart, String label, int count, int total, int color) {
        // Créer un LinearLayout pour chaque légende
        LinearLayout legendLayout = new LinearLayout(this);
        legendLayout.setOrientation(LinearLayout.HORIZONTAL);
        legendLayout.setPadding(10, 10, 10, 10); // Espacement entre chaque légende
        legendLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Ajouter un carré coloré à gauche de la légende
        View colorSquare = new View(this);
        colorSquare.setLayoutParams(new LinearLayout.LayoutParams(30, 30)); // Carré de 30px x 30px
        colorSquare.setBackgroundColor(color);
        legendLayout.addView(colorSquare);

        // Créer un TextView pour afficher la légende avec le texte
        TextView legend = new TextView(this);
        legend.setText(label + " (" + count + ", " + getPercentage(count, total) + "%)");
        legend.setTextColor(color);
        legend.setTextSize(16); // Taille du texte
        legend.setPadding(20, 0, 0, 0); // Espacement après le carré coloré
        legend.setTypeface(null, Typeface.BOLD); // Texte en gras

        // Ajouter la légende dans le LinearLayout
        legendLayout.addView(legend);

        // Ajouter le LinearLayout dans la vue de légende appropriée
        if (pieChart == pieChartOverall) {
            LinearLayout legendOverall = findViewById(R.id.legendOverall);
            legendOverall.addView(legendLayout);
        } else if (pieChart == pieChartByCategory) {
            LinearLayout legendCategory = findViewById(R.id.legendCategory);
            legendCategory.addView(legendLayout);
        }
    }

    private void clearLegends() {
        // Supprimer toutes les anciennes légendes avant d'ajouter de nouvelles
        LinearLayout legendOverall = findViewById(R.id.legendOverall);
        legendOverall.removeAllViews();

        LinearLayout legendCategory = findViewById(R.id.legendCategory);
        legendCategory.removeAllViews();
    }


    private void addSegmentWithLabel(PieChart pieChart, String label, int count, int total, int color) {
        // Calculer le pourcentage
        int percentage = getPercentage(count, total);

        // Créer un segment avec une étiquette qui inclut le nombre exact et le pourcentage
        Segment segment = new Segment(label + " (" + count + ", " + percentage + "%)", count);

        // Formatter pour définir la couleur et les labels
        SegmentFormatter formatter = new SegmentFormatter(color); // Utiliser directement la couleur
        formatter.getLabelPaint().setTextSize(PixelUtils.dpToPix(14)); // Taille du texte
        formatter.getLabelPaint().setColor(Color.GRAY); // Couleur du texte

        // Ajouter le segment au graphique
        pieChart.addSegment(segment, formatter);
    }

    private int getPercentage(int count, int total) {
        return (int) Math.round((count / (double) total) * 100);
    }

    private int getCategoryColor(String category) {
        // Palette de couleurs prédéfinies
        int[] colors = {
                Color.parseColor("#FF6F61"), // Rouge corail
                Color.parseColor("#6B5B95"), // Violet
                Color.parseColor("#88B04B"), // Vert
                Color.parseColor("#F7CAC9"), // Rose pâle
                Color.parseColor("#92A8D1"), // Bleu pastel
                Color.parseColor("#955251"), // Marron
                Color.parseColor("#B565A7"), // Mauve
                Color.parseColor("#009B77"), // Vert émeraude
                Color.parseColor("#DD4124"), // Rouge vif
                Color.parseColor("#D65076")  // Rose
        };

        // Utiliser le hashcode de la catégorie pour sélectionner une couleur dans la palette
        int index = Math.abs(category.hashCode()) % colors.length;
        return colors[index];
    }

    @Override
    public void onResume() {
        super.onResume();
        clearLegends(); // Supprimer les légendes existantes
        loadOverallStatistics(); // Recharge les statistiques globales
        loadCategoryStatistics(); // Recharge les statistiques par catégorie
    }
}