package com.example.taskmanager;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DashboardFragment extends Fragment {

    private TaskDBHelper dbHelper;
    private List<Data> taskData;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;

    private PieChart pieChart;  // Add PieChart reference


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gonfler la vue du fragment
        View view = inflater.inflate(R.layout.fragment_accueil, container, false);

        // Initialisation des variables
        dbHelper = new TaskDBHelper(getContext());
        taskData = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerview);
        pieChart = view.findViewById(R.id.pieChart);  // Initialize the PieChart


        // Ajouter un OnClickListener
        pieChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ouvrir l'activité de statistiques
                Intent intent = new Intent(getContext(), StatisticsActivity.class);
                startActivity(intent);
            }
        });


        // Configurer RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(getContext(), taskData);
        recyclerView.setAdapter(adapter);

        // Charger les tâches depuis SQLite
        loadTasksFromSQLite(taskData);

        // Charger et afficher les statistiques
        loadStatistics();

        // Configurer les clics pour l'adaptateur
        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                Intent intent = new Intent(getActivity(), EditTask.class);
                intent.putExtra("task", taskData.get(position).getName());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                deleteTask(position);
            }

            @Override
            public void onCheckboxClick(int position) {
                toggleTaskCompletion(position);
            }
        });



        return view;

    }


    // Load statistics into the pie chart
    private void loadStatistics() {

        // Effacer les anciens segments
        pieChart.clear();

        // Utilisation de countTasksByStatus
        int completed = dbHelper.countTasksByStatus("completed");
        int inProgress = dbHelper.countTasksByStatus("in_progress");
        int overdue = dbHelper.countTasksByStatus("overdue");

        // Calculer le total
        int total = completed + inProgress + overdue;
        if (total == 0) {
            total = 1; // Éviter la division par zéro
        }

        // Ajouter des segments au diagramme avec pourcentage
        Segment completedSegment = new Segment("Completed (" + getPercentage(completed, total) + "%)", completed);
        Segment inProgressSegment = new Segment("In Progress (" + getPercentage(inProgress, total) + "%)", inProgress);
        Segment overdueSegment = new Segment("Overdue (" + getPercentage(overdue, total) + "%)", overdue);

        // Définir les couleurs pour chaque segment
        pieChart.addSegment(completedSegment, new SegmentFormatter(getResources().getColor(android.R.color.holo_green_light)));
        pieChart.addSegment(inProgressSegment, new SegmentFormatter(getResources().getColor(android.R.color.holo_orange_light)));
        pieChart.addSegment(overdueSegment, new SegmentFormatter(getResources().getColor(android.R.color.holo_red_light)));

        pieChart.getBackgroundPaint().setColor(Color.TRANSPARENT);

        // Redessiner le graphique
        pieChart.redraw();
    }

    private int getPercentage(int count, int total) {
        return (int) Math.round((count / (double) total) * 100);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("DashboardFragment", "onResume called: Refreshing data.");
        taskData.clear(); // Effacer les données actuelles
        loadTasksFromSQLite(taskData); // Recharger les données depuis la base de données
        adapter.notifyDataSetChanged(); // Notifier l'adaptateur pour actualiser l'affichage

        loadStatistics();

    }


    private void deleteTask(int position) {
        String taskName = taskData.get(position).getName();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE_NAME,
                TaskContract.TaskEntry.COLUMN_TASK + " = ?",
                new String[]{taskName});
        db.close();
        taskData.remove(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(getContext(), "Task Deleted", Toast.LENGTH_SHORT).show();
    }

    private void loadTasksFromSQLite(List<Data> data) {
        // Récupérer la date actuelle du système
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Ouvrir la base de données en lecture
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Modifier la requête SQL pour ne récupérer que les tâches dont la due_date est égale à la date actuelle
        Cursor cursor = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME +
                " WHERE " + TaskContract.TaskEntry.COLUMN_DUE_DATE + " = ?", new String[]{currentDate});

        // Parcourir le curseur et ajouter les tâches filtrées à la liste
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK));
            @SuppressLint("Range") String taskDate = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_DATE));
            @SuppressLint("Range") String taskTime = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_TIME));
            @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_CATEGORY));
            @SuppressLint("Range") String priority = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY));
            @SuppressLint("Range") String notes = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NOTES));
            @SuppressLint("Range") boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_COMPLETED)) == 1;

            // Ajouter la tâche à la liste
            data.add(new Data(taskName, taskDate, taskTime, category, priority, notes, completed));
        }

        // Fermer le curseur et la base de données
        cursor.close();

        loadStatistics();
    }


    private void toggleTaskCompletion(int position) {
        DashboardFragment.Data task = taskData.get(position);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Récupérer l'état actuel de completed depuis la base de données
        boolean isCompleted = getTaskCompletedState(task.getName());

        ContentValues values = new ContentValues();
        if (isCompleted) {
            // Si la tâche est déjà terminée, la marquer comme "non terminée"
            values.put(TaskContract.TaskEntry.COLUMN_COMPLETED, 0);
            Toast.makeText(getContext(), "Task marked as not completed", Toast.LENGTH_SHORT).show();
        } else {
            // Sinon, la marquer comme "terminée"
            values.put(TaskContract.TaskEntry.COLUMN_COMPLETED, 1);
            Toast.makeText(getContext(), "Task marked as completed", Toast.LENGTH_SHORT).show();
        }

        // Mise à jour dans la base de données
        db.update(TaskContract.TaskEntry.TABLE_NAME,
                values,
                TaskContract.TaskEntry.COLUMN_TASK + " = ?",
                new String[]{task.getName()});


        // Recharger les tâches pour mettre à jour l'interface
        loadTasksFromSQLite(taskData);
    }

    private boolean getTaskCompletedState(String taskName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + TaskContract.TaskEntry.COLUMN_COMPLETED +
                " FROM " + TaskContract.TaskEntry.TABLE_NAME +
                " WHERE " + TaskContract.TaskEntry.COLUMN_TASK + " = ?", new String[]{taskName});

        boolean isCompleted = false;
        if (cursor.moveToFirst()) {
            isCompleted = cursor.getInt(0) == 1; // Vérifier si "completed" est égal à 1
        }

        cursor.close();

        return isCompleted;
    }



    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    public static class Data {
        String name;
        String date;
        String time;
        String category;
        String priority;
        String notes;
        boolean completed; // Nouveau champ

        Data(String name, String date, String time, String category, String priority, String notes, boolean completed) {
            this.name = name;
            this.date = date;
            this.time = time;
            this.category = category;
            this.priority = priority;
            this.notes = notes;
            this.completed = completed; // Initialiser l'état
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getCategory() {
            return category;
        }

        public String getPriority() {
            return priority;
        }

        public String getNotes() {
            return notes;
        }
    }
}
