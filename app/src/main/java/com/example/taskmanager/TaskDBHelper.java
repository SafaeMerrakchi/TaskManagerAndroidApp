package com.example.taskmanager;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasklist.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_TASK_TABLE =
            "CREATE TABLE " + TaskContract.TaskEntry.TABLE_NAME + " (" +
                    TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TaskContract.TaskEntry.COLUMN_TASK + " TEXT NOT NULL, " +
                    TaskContract.TaskEntry.COLUMN_CATEGORY + " TEXT, " +
                    TaskContract.TaskEntry.COLUMN_PRIORITY + " TEXT, " +
                    TaskContract.TaskEntry.COLUMN_NOTES + " TEXT, " +
                    TaskContract.TaskEntry.COLUMN_DUE_DATE + " TEXT, " +
                    TaskContract.TaskEntry.COLUMN_DUE_TIME + " TEXT, " +
                    TaskContract.TaskEntry.COLUMN_COMPLETED + " INTEGER DEFAULT 0);";

    private static final String SQL_CREATE_CATEGORY_TABLE =
            "CREATE TABLE categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE);";

    public TaskDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TASK_TABLE);
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);
        // Ajouter des catégories par défaut
        db.execSQL("INSERT INTO categories (name) VALUES ('Work'), ('Personal'), ('Shopping'), ('School')");
    }

    public Cursor getAllTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TaskContract.TaskEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Gérer les mises à jour de la base de données si nécessaire
    }

    // Méthode pour récupérer la date actuelle du téléphone
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Méthode pour récupérer l'heure actuelle du téléphone
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public int countTasksByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;

        // Récupérer la date et l'heure actuelles du téléphone
        String currentDate = getCurrentDate();
        String currentTime = getCurrentTime();

        switch (status) {
            case "completed":
                query = "SELECT COUNT(*) FROM tasks WHERE completed = 1";
                break;
            case "in_progress":
                query = "SELECT COUNT(*) FROM tasks WHERE completed = 0 AND " +
                        "(due_date > ? OR (due_date = ? AND due_time > ?))";
                break;
            case "overdue":
                query = "SELECT COUNT(*) FROM tasks WHERE completed = 0 AND " +
                        "(due_date < ? OR (due_date = ? AND due_time < ?))";
                break;
            default:
                return 0;
        }

        // Exécuter la requête avec les paramètres
        Cursor cursor;
        if (status.equals("in_progress") || status.equals("overdue")) {
            cursor = db.rawQuery(query, new String[]{currentDate, currentDate, currentTime});
        } else {
            cursor = db.rawQuery(query, null);
        }

        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT category FROM tasks", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                categories.add(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            }
            cursor.close();
        }
        return categories;
    }

    public int countIncompleteTasksByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM tasks WHERE category = ? AND completed = 0",
                new String[]{category}
        );
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            return count;
        }
        return 0;
    }
}

