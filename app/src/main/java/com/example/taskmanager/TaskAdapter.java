package com.example.taskmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<DashboardFragment.Data> tasks; // Liste des tâches
    private final Context mContext; // Contexte
    private OnItemClickListener mListener; // Listener pour gérer les événements

    private static final int TYPE_EMPTY = 0;
    private static final int TYPE_TASK = 1;
    private TaskDBHelper dbHelper;


    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onCheckboxClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public TaskAdapter(Context context, List<DashboardFragment.Data> taskList) {
        this.mContext = context;
        this.tasks = taskList;
        this.dbHelper = new TaskDBHelper(context); // Initialisation de dbHelper

    }

    @Override
    public int getItemViewType(int position) {
        return tasks.isEmpty() ? TYPE_EMPTY : TYPE_TASK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_EMPTY) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.empty_view, parent, false);
            return new EmptyViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.task_card_layout, parent, false);
            return new TaskViewHolder(view, mListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_TASK) {
            TaskViewHolder taskViewHolder = (TaskViewHolder) holder;
            DashboardFragment.Data task = tasks.get(position);
            taskViewHolder.bind(task);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.isEmpty() ? 1 : tasks.size();
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextViewName;
        public TextView mTextViewDate;
        public TextView mTextViewTime;
        public TextView text_category;
        public TextView text_priority;
        public TextView text_notes;
        public CheckBox mCheckBox;
        public Button mButtonEdit;
        public Button mButtonDelete;

        public TaskViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            mTextViewName = itemView.findViewById(R.id.text_name);
            mTextViewDate = itemView.findViewById(R.id.text_date);
            mTextViewTime = itemView.findViewById(R.id.text_time);
            text_category = itemView.findViewById(R.id.text_category);
            text_priority = itemView.findViewById(R.id.text_priority);
            text_notes = itemView.findViewById(R.id.text_notes);

            mCheckBox = itemView.findViewById(R.id.check_box);
            ImageButton mButtonEdit = itemView.findViewById(R.id.btn_edit);
            ImageButton mButtonDelete = itemView.findViewById(R.id.btn_delete);

            mButtonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEditClick(position);
                    }
                }
            });

            mButtonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });

            mCheckBox.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCheckboxClick(position);
                    }
                }
            });
        }

        public void bind(DashboardFragment.Data task) {
            mTextViewName.setText(task.getName());
            mTextViewDate.setText("Date: " + task.getDate());
            mTextViewTime.setText("Time: " + task.getTime());
            text_category.setText("Category: " + task.getCategory());
            text_priority.setText("Priority: " + task.getPriority());
            text_notes.setText("Note: " + task.getNotes());
            mCheckBox.setChecked(task.isCompleted()); // Synchroniser l'état "completed"

        }
    }
}
