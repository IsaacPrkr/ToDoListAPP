package com.example.todolistapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// activity to change user password
public class ChangePasswordActivity extends AppCompatActivity {

    // ui elements for password input and action buttons
    private EditText currentPasswordInput, newPasswordInput, confirmNewPasswordInput;
    private Button submitNewPasswordButton, backButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password); // set layout

        setTitle("Change Password"); // set title

        // initialize ui components
        currentPasswordInput = findViewById(R.id.currentPassword);
        newPasswordInput = findViewById(R.id.newPassword);
        confirmNewPasswordInput = findViewById(R.id.confirmNewPassword);
        submitNewPasswordButton = findViewById(R.id.newPasswordButton);
        backButton = findViewById(R.id.backButton);

        // handle new password submission
        submitNewPasswordButton.setOnClickListener(v -> {
            // get text from edit texts and trim
            String currentPassword = currentPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmNewPassword = confirmNewPasswordInput.getText().toString().trim();

            // check if new passwords match and meet firebase length requirement
            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(this, "passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // get current user and create credential for re-authentication
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            // re-authenticate and proceed with password update upon success
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(this, "password updated successfully", Toast.LENGTH_SHORT).show();
                            finish(); // close activity on success
                        } else {
                            Toast.makeText(this, "password update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "authentication failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // handle back navigation
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AccountActivity.class));
            finish(); // close this activity
        });
    }
}
