package com.example.taskmanager;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private TaskDBHelper dbHelper;
    private List<DashboardFragment.Data> taskData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialisation des variables
        dbHelper = new TaskDBHelper(this);
        taskData = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview_search);
        SearchView searchView = findViewById(R.id.search_view);

        // Configuration du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, taskData);
        recyclerView.setAdapter(adapter);

        // Configurer le SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTasks(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTasks(newText);
                return false;
            }
        });
    }

    private void searchTasks(String query) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TaskContract.TaskEntry.TABLE_NAME +
                " WHERE " + TaskContract.TaskEntry.COLUMN_TASK + " LIKE ?", new String[]{"%" + query + "%"});

        taskData.clear();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TASK));
            @SuppressLint("Range") String taskDate = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_DATE));
            @SuppressLint("Range") String taskTime = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DUE_TIME));
            @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_CATEGORY));
            @SuppressLint("Range") String priority = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY));
            @SuppressLint("Range") String notes = cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_NOTES));
            @SuppressLint("Range") boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_COMPLETED)) == 1;

            taskData.add(new DashboardFragment.Data(taskName, taskDate, taskTime, category, priority, notes, completed));
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }
}