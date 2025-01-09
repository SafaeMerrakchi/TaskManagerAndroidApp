package com.example.taskmanager;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditTask extends AppCompatActivity {

    private TextView selectedDateTextView;
    private TextView selectedTimeTextView;
    private Spinner categorySpinner;
    private Spinner prioritySpinner;
    private EditText notesEditText;
    private TextView text_view_task;
    private TaskDBHelper dbHelper;

    private Calendar calendar;
    String task;
    Intent intent;
    private int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task_activity);



        intent = getIntent();
        task = intent.getStringExtra("task");

        text_view_task = findViewById(R.id.text_view_task);
        text_view_task.setText(task);
        selectedDateTextView = findViewById(R.id.selected_date_text_view);
        selectedTimeTextView = findViewById(R.id.selected_time_text_view);
        categorySpinner = findViewById(R.id.category_spinner);
        prioritySpinner = findViewById(R.id.priority_spinner);
        notesEditText = findViewById(R.id.notes_edit_text);
        Button selectDateButton = findViewById(R.id.button_select_due_date);
        Button selectTimeButton = findViewById(R.id.button_select_due_time);
        Button addTaskButton = findViewById(R.id.button_add_task);

        dbHelper = new TaskDBHelper(this);

        calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        // Charger les catégories dynamiquement
        populateCategorySpinner();

        // Charger les priorités depuis les ressources
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.priorities_array,
                android.R.layout.simple_spinner_item
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        updateDateAndTimeTextViews();

        selectDateButton.setOnClickListener(v -> showDatePickerDialog());
        selectTimeButton.setOnClickListener(v -> showTimePickerDialog());

        addTaskButton.setOnClickListener(v -> {
            editTask(task);
            Toast.makeText(EditTask.this, "Task edited", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(EditTask.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void populateCategorySpinner() {
        List<String> categories = loadCategoriesFromDB();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private List<String> loadCategoriesFromDB() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM categories", null);

        while (cursor.moveToNext()) {
            categories.add(cursor.getString(0)); // Récupérer la colonne "name"
        }
        cursor.close();
        db.close();
        return categories;
    }

    private void updateDateAndTimeTextViews() {
        String dateString = String.format(Locale.getDefault(), "%04d-%02d-%02d", mYear, mMonth + 1, mDay);
        selectedDateTextView.setText(dateString);

        // Mise à jour de l'heure avec un format valide
        String timeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", mHour, mMinute, 0);
        selectedTimeTextView.setText(timeString);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    mYear = year;
                    mMonth = month;
                    mDay = dayOfMonth;
                    updateDateAndTimeTextViews();
                },
                mYear, mMonth, mDay
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    mHour = hourOfDay;
                    mMinute = minute;
                    updateDateAndTimeTextViews();
                },
                mHour, mMinute,
                true
        );
        timePickerDialog.show();
    }

    private void editTask(String task) {
        String category = categorySpinner.getSelectedItem().toString();
        String priority = prioritySpinner.getSelectedItem().toString();
        String notes = notesEditText.getText().toString().trim();
        String dueDate = selectedDateTextView.getText().toString().trim();
        String dueTime = selectedTimeTextView.getText().toString().trim();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_CATEGORY, category);
        values.put(TaskContract.TaskEntry.COLUMN_PRIORITY, priority);
        values.put(TaskContract.TaskEntry.COLUMN_NOTES, notes);
        values.put(TaskContract.TaskEntry.COLUMN_DUE_DATE, dueDate);
        values.put(TaskContract.TaskEntry.COLUMN_DUE_TIME, dueTime);

        db.update(TaskContract.TaskEntry.TABLE_NAME, values, TaskContract.TaskEntry.COLUMN_TASK + " = ?", new String[]{task});
        db.close();

        scheduleNotifications(task);
    }

    private void scheduleNotifications(String taskName) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth, mDay, mHour, mMinute, 0);

        long triggerAtMillis = calendar.getTimeInMillis();

        // Planifier les trois notifications
        scheduleNotification(taskName, triggerAtMillis, "The task is due now! ");
        scheduleNotification(taskName, triggerAtMillis - 60 * 60 * 1000, "One hour before the deadline!");
        scheduleNotification(taskName, triggerAtMillis - 24 * 60 * 60 * 1000, "One day before the deadline!");
    }

    private void scheduleNotification(String taskName, long triggerAtMillis, String timeRemainingMessage) {
        // Obtenez le temps actuel en millisecondes
        long currentTimeMillis = System.currentTimeMillis();

        // Vérifiez si la date est déjà passée
        if (triggerAtMillis <= currentTimeMillis) {
            return; // Ne planifiez pas de notification
        }

        // Créez l'intention de rappel
        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        intent.putExtra("task_name", taskName);
        intent.putExtra("time_remaining", timeRemainingMessage);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_IMMUTABLE // Choisissez l'un des deux
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        }
    }
}
