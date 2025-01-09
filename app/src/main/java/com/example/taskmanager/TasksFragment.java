package com.example.taskmanager;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private TaskDBHelper dbHelper;
    private List<DashboardFragment.Data> taskData;
    private Spinner sortSpinner;
    private RadioGroup orderGroup;
    private Button sortButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Initialisation des variables
        dbHelper = new TaskDBHelper(getContext());
        taskData = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerview2);
        sortSpinner = view.findViewById(R.id.sortSpinner);
        orderGroup = view.findViewById(R.id.orderGroup);
        sortButton = view.findViewById(R.id.sortButton);

        // Configuration du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(getContext(), taskData);
        recyclerView.setAdapter(adapter);

        // Charger les tâches depuis la base de données SQLite
        loadTasksFromSQLite();

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

        // Configurer le bouton de tri
        sortButton.setOnClickListener(v -> {
            String selectedOption = sortSpinner.getSelectedItem().toString();
            String order = "ASC";
            int selectedOrderId = orderGroup.getCheckedRadioButtonId();
            if (selectedOrderId != -1) {
                RadioButton selectedOrder = view.findViewById(selectedOrderId);
                order = selectedOrder.getText().toString().equals("Descendant") ? "DESC" : "ASC";
            }
            sortTasks(selectedOption, order);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Effacer les anciennes tâches et recharger les tâches depuis SQLite
        taskData.clear();  // Efface les données actuelles
        loadTasksFromSQLite();  // Recharger les tâches depuis la base de données
        adapter.notifyDataSetChanged();  // Notifier l'adaptateur pour actualiser l'affichage
    }

    private void loadTasksFromSQLite() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME, null);

        taskData.clear();
        while (cursor.moveToNext()) {
            String taskName = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_TASK));
            String taskDate = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_DUE_DATE));
            String taskTime = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_DUE_TIME));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_CATEGORY));
            String priority = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_PRIORITY));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NOTES));
            boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_COMPLETED)) == 1;


            taskData.add(new DashboardFragment.Data(taskName, taskDate, taskTime, category, priority, notes, completed));
        }
        cursor.close();

        adapter.notifyDataSetChanged();
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
        loadTasksFromSQLite();
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

    private void sortTasks(String option, String order) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;

        switch (option) {
            case "Priority":
                cursor = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME +
                        " ORDER BY " + TaskContract.TaskEntry.COLUMN_PRIORITY + " " + order, null);
                break;
            case "Date and Time":
                cursor = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME +
                        " ORDER BY " + TaskContract.TaskEntry.COLUMN_DUE_DATE + " " + order + ", " +
                        TaskContract.TaskEntry.COLUMN_DUE_TIME + " " + order, null);
                break;

            case "Category":
                cursor = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME +
                        " ORDER BY " + TaskContract.TaskEntry.COLUMN_CATEGORY + " " + order, null);
                break;
            case "Name":
                cursor = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME +
                        " ORDER BY " + TaskContract.TaskEntry.COLUMN_TASK + " " + order, null);
                break;
            case "Completed":
                cursor = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME +
                        " WHERE " + TaskContract.TaskEntry.COLUMN_COMPLETED + " = 1", null);
                break;
            case "Not completed":
                cursor = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME +
                        " WHERE " + TaskContract.TaskEntry.COLUMN_COMPLETED + " = 0", null);
                break;
            default:
                cursor = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME, null);
                break;
        }

        taskData.clear();
        while (cursor.moveToNext()) {
            String taskName = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_TASK));
            String taskDate = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_DUE_DATE));
            String taskTime = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_DUE_TIME));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_CATEGORY));
            String priority = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_PRIORITY));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NOTES));
            boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_COMPLETED)) == 1;


            taskData.add(new DashboardFragment.Data(taskName, taskDate, taskTime, category, priority, notes, completed));
        }
        cursor.close();


        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
