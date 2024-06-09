package com.example.todolistapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// registration activity for new users
public class RegisterActivity extends AppCompatActivity {

    // ui elements for email and password input, registration button, progress bar, and login text view
    TextInputEditText emailEditText, passwordEditText;
    Button registerButton;
    FirebaseAuth firebaseAuth; // firebase authentication instance
    ProgressBar progressBar; // progress indicator for registration process
    TextView textView; // text view for switching to login

    @Override
    public void onStart() {
        super.onStart();
        // check if user is already logged in and redirect to main activity
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // sets the layout for the registration activity

        // initializing ui components
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        registerButton = findViewById(R.id.registerButton);
        firebaseAuth = FirebaseAuth.getInstance(); // get instance of firebase auth
        progressBar = findViewById(R.id.registerProgressBar); // progress bar to indicate loading
        TextView textView = findViewById(R.id.login); // text view for navigating to login activity

        // set on click listener for the "login now" text view to navigate to login activity
        textView.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        });

        // set on click listener for the registration button
        registerButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE); // show progress bar
            String email = emailEditText.getText().toString().trim(); // get email from text input
            String password = passwordEditText.getText().toString().trim(); // get password from text input

            // check if email or password is empty and show toast if true
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(RegisterActivity.this, "enter email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE); // hide progress bar if validation fails
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(RegisterActivity.this, "enter password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE); // hide progress bar if validation fails
                return;
            }

            // create user with email and password
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE); // hide progress bar after attempt to register
                            if (task.isSuccessful()) {
                                // registration successful, navigate to main activity
                                Toast.makeText(RegisterActivity.this, "account created.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                // registration failed, show error message
                                Toast.makeText(RegisterActivity.this, "authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }
}
