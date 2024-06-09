package com.example.todolistapp;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// login screen activity
public class LoginActivity extends AppCompatActivity {

    // fields for user input, auth, and ui
    TextInputEditText editTextEmail, editTextPassword;
    Button logInButton;
    FirebaseAuth firebaseAuth; // firebase authentication instance
    ProgressBar signInProgressBar; // progress bar for loading indication


    @Override
    public void onStart() {
        super.onStart();
        // check if user is already logged in and redirect
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // set layout for login activity

        // initializing UI components by id
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        logInButton = findViewById(R.id.loginButton);
        firebaseAuth = FirebaseAuth.getInstance(); // get firebase auth instance
        signInProgressBar = findViewById(R.id.loginProgressBar); // progress bar for loading indication
        TextView textView = findViewById(R.id.register); // text view for navigating to register activity

        // set on click listener for "register now" text view
        textView.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            finish();
        });

        // set on click listener for login button
        logInButton.setOnClickListener(view -> {
            signInProgressBar.setVisibility(View.VISIBLE); // show progress bar
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // validate email and password input
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "enter email", Toast.LENGTH_SHORT).show();
                signInProgressBar.setVisibility(View.GONE); // hide progress bar on error
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "enter password", Toast.LENGTH_SHORT).show();
                signInProgressBar.setVisibility(View.GONE); // hide progress bar on error
                return;
            }

            // authenticate user with firebase
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        signInProgressBar.setVisibility(View.GONE); // hide progress bar after auth attempt
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
