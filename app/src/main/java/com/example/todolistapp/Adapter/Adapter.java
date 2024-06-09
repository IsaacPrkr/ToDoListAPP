package com.example.todolistapp.Adapter;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolistapp.AddTask;
import com.example.todolistapp.MainActivity;
import com.example.todolistapp.Model.Model;
import com.example.todolistapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

// adapter class for RecyclerView to display tasks
public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder>{

    private List<Model> todoList; // list of tasks
    private MainActivity mainactivity; // reference to MainActivity for context
    private FirebaseFirestore firestore; // Firestore instance for database operations

    // constructor initializing the main activity context and task list
    public Adapter(MainActivity mainActivity, List<Model> todoList){
        mainactivity = mainActivity;
        this.todoList = todoList;
    }

    // inflates the task layout and returns a ViewHolder
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mainactivity).inflate(R.layout.task_design, parent, false);
        firestore = FirebaseFirestore.getInstance(); // initialize Firestore
        return new MyViewHolder(view);
    }

    // deletes a task from Firestore and updates the RecyclerView
    public void deleteTask(int position){
        Model model = todoList.get(position);
        firestore.collection("task").document(model.TaskID).delete(); // delete from Firestore
        todoList.remove(position); // remove from local list
        notifyItemRemoved(position); // notify RecyclerView of item removal
    }

    // returns the context of the activity
    public Context getContext(){
        return mainactivity;
    }

    // sets up a dialog to edit a task
    public void editTask(int position){
        Model model = todoList.get(position);

        Bundle bundle = new Bundle(); // create a bundle to pass task details
        bundle.putString("task", model.getTask());
        bundle.putString("due", model.getDue());
        bundle.putString("id", model.TaskID);
        bundle.putString("description", model.getDescription());
        bundle.putString("progression", model.getProgression());

        AddTask addTask = new AddTask(); // create an instance of AddNewTask
        addTask.setArguments(bundle); // setting arguments for task details
        addTask.show(mainactivity.getSupportFragmentManager(), addTask.getTag()); // show dialog to edit task
    }

    // binds task data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Model model = todoList.get(position);
        holder.checkbox.setText(model.getTask()); // set task title
        holder.DueDate.setText("Due : " + model.getDue()); // set due date

        holder.checkbox.setChecked(toBoolean(model.getStatus())); // set checkbox status based on task completion

        // listens for checkbox changes and updates task status in Firestore
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    firestore.collection("task").document(model.TaskID).update("status", 1); // set status to completed
                }else{
                    firestore.collection("task").document(model.TaskID).update("status", 0); // set status to not completed
                }

            }
        });
    }

    // converts status integer to boolean for checkbox
    private boolean toBoolean(int status){
        return status != 0;
    }

    // returns the number of tasks
    @Override
    public int getItemCount() {
        return todoList.size();
    }

    // viewHolder class for task items
    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView DueDate; // TextView for due date
        CheckBox checkbox; // CheckBox for task completion

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            DueDate = itemView.findViewById(R.id.DueDateDesign); // initialize due date TextView
            checkbox = itemView.findViewById(R.id.checkbox); // initialize task CheckBox
        }
    }
}
