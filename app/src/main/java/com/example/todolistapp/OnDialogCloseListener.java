package com.example.todolistapp;

import android.content.DialogInterface;

// interface for handling dialog close events
public interface OnDialogCloseListener {

    // method to be called when a dialog is closed
    void onDialogClose(DialogInterface dialogInterface);
}
