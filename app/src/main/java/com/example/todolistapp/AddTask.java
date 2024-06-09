package com.example.todolistapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class AddTask extends BottomSheetDialogFragment {


    public static final String TAG = "AddNewTask";
    private TextView setDueDate;
    private EditText taskEditText, descriptionEditText;
    private Button saveButton;
    private Spinner progressionSpinner;
    private FirebaseFirestore firestore; // firebase firestore instance
    private Context appContext; // context for various operations
    private String dueDate = "";
    private String id = "";
    private String DateUpdate = "";

    // factory method to create a new instance of this fragment
    public static AddTask newInstance() {
        return new AddTask();
    }

    // inflates the layout for this fragment
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_new_task, container, false);
    }

    // setup the UI components once the view is created
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialise components
        setDueDate = view.findViewById(R.id.DueDate);
        taskEditText = view.findViewById(R.id.EnterTask);
        descriptionEditText = view.findViewById(R.id.EnterDescription);
        saveButton = view.findViewById(R.id.buttonSave);
        progressionSpinner = view.findViewById(R.id.spinnerProgress);

        firestore = FirebaseFirestore.getInstance(); // Get Firestore instance

        // setup spinner for task progression with predefined array and default spinner layout
        ArrayAdapter<CharSequence> progressionAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.task_progression, android.R.layout.simple_spinner_item);
        progressionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        progressionSpinner.setAdapter(progressionAdapter);

        // check if this is an update operation
        boolean update = false;
        final Bundle bundle = getArguments();
        if (bundle != null) {
            update = true;
            // extract task details from bundle
            String task = bundle.getString("task");
            String description = bundle.getString("description");
            id = bundle.getString("id");
            DateUpdate = bundle.getString("due");

            // set extracted values to UI components
            taskEditText.setText(task);
            descriptionEditText.setText(description);
            setDueDate.setText(DateUpdate);

            // enable save button if task and description are not empty
            if (!task.isEmpty() && !description.isEmpty()) {
                saveButton.setEnabled(true);
                saveButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
            } else {
                saveButton.setEnabled(false);
                saveButton.setBackgroundColor(Color.GRAY);
            }
        }

        // textwatcher to enable/disable save button based on task title input
        taskEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                // enable save button only if task title is not empty
                if (s.toString().isEmpty()) {
                    saveButton.setEnabled(false);
                    saveButton.setBackgroundColor(Color.GRAY);
                } else {
                    saveButton.setEnabled(true);
                    saveButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        setDueDate.setOnClickListener(v -> { // set up a date picker dialog for due date selection
            Calendar calendar = Calendar.getInstance();
            int MONTH = calendar.get(Calendar.MONTH);
            int YEAR = calendar.get(Calendar.YEAR);
            int DAY = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(appContext, (view1, year, month, dayOfMonth) -> {
                month = month + 1; // month is 0-based, add 1 for display
                setDueDate.setText(dayOfMonth + "/" + month + "/" + year);
                dueDate = dayOfMonth + "/" + month + "/" + year; // store selected due date
            }, YEAR, MONTH, DAY);

            datePickerDialog.show(); // show the date picker dialog
        });


        boolean isUpdated = update; // Save button click listener to add or update task in Firestore
        saveButton.setOnClickListener(v -> {

            String taskText = taskEditText.getText().toString().trim(); // trimming input to remove leading/trailing whitespaces
            String descriptionText = descriptionEditText.getText().toString().trim();
            String progression = progressionSpinner.getSelectedItem().toString(); // get selected progression


            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // get current user UID from Firebase Authentication
            String userId = currentUser != null ? currentUser.getUid() : "";


            if (isUpdated) { // check whether if it's an update operation
                // update task in Firestore
                firestore.collection("task").document(id)
                        .update("task", taskText, "due", dueDate, "userId", userId, "description", descriptionText, "progression", progression)
                        .addOnSuccessListener(aVoid -> Toast.makeText(appContext, "Task updated successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(appContext, "Error updating the task", Toast.LENGTH_SHORT).show());
            } else {

                if (!taskText.isEmpty()) { // add new task to Firestore
                    Map<String, Object> taskMap = new HashMap<>();
                    taskMap.put("task", taskText);
                    taskMap.put("due", dueDate);
                    taskMap.put("status", 0); // default status is 0
                    taskMap.put("time", FieldValue.serverTimestamp()); // server timestamp for creation time
                    taskMap.put("userId", userId);
                    taskMap.put("description", descriptionText);
                    taskMap.put("progression", progression);

                    firestore.collection("task").add(taskMap)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(appContext, "Task added successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Handle failure
                                        if (task.getException() != null) {
                                            Toast.makeText(appContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(appContext, e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    // show toast if task title is empty
                    Toast.makeText(appContext, "Empty tasks aren't allowed...", Toast.LENGTH_SHORT).show();
                }
            }
            dismiss(); // Dismiss
        });
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.appContext = context;
    }

    // callback for when the dialog is dismissed
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();

        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }
}
