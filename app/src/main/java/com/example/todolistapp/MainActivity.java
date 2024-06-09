package com.example.todolistapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.todolistapp.Adapter.Adapter;
import com.example.todolistapp.Model.Model;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListener {

    FirebaseAuth auth; // firebase auth for user authentication
    Button button; // logout button
    TextView textView; // displays user info
    FirebaseUser user; // current signed-in user
    private RecyclerView taskRecyclerView;
    private FloatingActionButton addTaskButton;
    private FirebaseFirestore db; // firestore instance for data handling

    private Adapter tasksAdapter; // adapter for recyclerview
    private List<Model> tasksList; // list of tasks
    private Query tasksQuery; // query for firestore
    private ListenerRegistration listenerRegistration; // firestore listener

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // set layout

        // initializing UI components
        taskRecyclerView = findViewById(R.id.recyclerView); // Make sure ID matches your layout
        addTaskButton = findViewById(R.id.floatingActionButton);
        db = FirebaseFirestore.getInstance();
        taskRecyclerView.setHasFixedSize(true);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        // floating action button click listener for adding new tasks
        addTaskButton.setOnClickListener(v -> AddTask.newInstance().show(getSupportFragmentManager(), AddTask.TAG));

        // setup firebase auth and user info display
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.Logout);
        textView = findViewById(R.id.WelcomeMessage);
        user = auth.getCurrentUser();

        // redirect to login if no user is signed in
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        } else {
            // display user email as username
            String email = user.getEmail();
            if (email != null) {
                String[] parts = email.split("@");
                String username = parts[0];
                textView.setText("welcome " + username);
            }
        }

        // logout button click listener
        button.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        // account button for navigating to account details
        Button accountButton = findViewById(R.id.accountButton); // Make sure ID matches your layout
        accountButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AccountActivity.class)));

        // setup task list and adapter
        tasksList = new ArrayList<>();
        tasksAdapter = new Adapter(MainActivity.this, tasksList);

        // touch helper for swipe actions on tasks
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(taskRecyclerView);
        taskRecyclerView.setAdapter(tasksAdapter);

        // load tasks from firestore
        loadingTask();
    }

    private void loadingTask() {
        // Check if user is authenticated
        if (user != null) {
            // Get the user's unique ID
            String userId = user.getUid();
            tasksQuery = db.collection("task")// Query tasks collection for tasks belonging to this user, ordered by time
                    .whereEqualTo("userId", userId)
                    .orderBy("time", Query.Direction.DESCENDING);

            // Query tasks collection for tasks belonging to this user, ordered by time
            listenerRegistration = tasksQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    // Check for errors
                    if (error != null) {
                        return;
                    }

                    // Iterate over changes in the documents returned by the query
                    for (DocumentChange documentChange : value.getDocumentChanges()) {
                        // Check if a document was added
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            // Get the ID of the added document
                            String id = documentChange.getDocument().getId();
                            // Convert the document to a Model object and associate it with its ID
                            Model model = documentChange.getDocument().toObject(Model.class).withId(id);
                            // Add the Model object to the tasksList
                            tasksList.add(model);
                            // Notify the adapter that the dataset has changed
                            tasksAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        tasksList.clear();
        loadingTask();
        tasksAdapter.notifyDataSetChanged();
    }
}
