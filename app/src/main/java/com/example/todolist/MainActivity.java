package com.example.todolist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {
    private List<Task> tasks = new ArrayList<>();
    private TaskAdapter taskAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(tasks, this);
        recyclerView.setAdapter(taskAdapter);

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        loadTasks();
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        EditText etTaskTitle = view.findViewById(R.id.etTaskTitle);
        Button btnAdd = view.findViewById(R.id.btnAdd);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.setView(view).create();

        btnAdd.setOnClickListener(v -> {
            String taskTitle = etTaskTitle.getText().toString().trim();
            if (!taskTitle.isEmpty()) {
                String id = UUID.randomUUID().toString();
                tasks.add(new Task(id, taskTitle, false));
                taskAdapter.notifyDataSetChanged();
                dialog.dismiss();
            } else {
                etTaskTitle.setError("Task title cannot be empty");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showEditTaskDialog(int position) {
        Task task = tasks.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        EditText etTaskTitle = view.findViewById(R.id.etTaskTitle);
        Button btnAdd = view.findViewById(R.id.btnAdd);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        etTaskTitle.setText(task.getTitle());
        btnAdd.setText("Update");

        AlertDialog dialog = builder.setView(view).create();

        btnAdd.setOnClickListener(v -> {
            String taskTitle = etTaskTitle.getText().toString().trim();
            if (!taskTitle.isEmpty()) {
                task.setTitle(taskTitle);
                taskAdapter.notifyItemChanged(position);
                dialog.dismiss();
            } else {
                etTaskTitle.setError("Task title cannot be empty");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onTaskClick(int position) {
        showEditTaskDialog(position);
    }

    @Override
    public void onCheckBoxClick(int position, boolean isChecked) {
        tasks.get(position).setCompleted(isChecked);
        taskAdapter.notifyItemChanged(position);
    }

    @Override
    public void onDeleteClick(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    tasks.remove(position);
                    taskAdapter.notifyItemRemoved(position);
                    saveTasks(); // If you're using persistence
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Add these persistence methods at the bottom of the class:
    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences("TodoPrefs", MODE_PRIVATE);
        String json = new Gson().toJson(tasks);
        prefs.edit().putString("tasks", json).apply();
    }

    private void loadTasks() {
        SharedPreferences prefs = getSharedPreferences("TodoPrefs", MODE_PRIVATE);
        String json = prefs.getString("tasks", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Task>>(){}.getType();
            ArrayList<Task> loadedTasks = new Gson().fromJson(json, type);
            tasks.clear();
            tasks.addAll(loadedTasks);
            taskAdapter.notifyDataSetChanged();
        }
    }

    // Update onCreate to load tasks:


    @Override
    protected void onPause() {
        super.onPause();
        saveTasks();  // Add this to save when app goes to background
    }
}