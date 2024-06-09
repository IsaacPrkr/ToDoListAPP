package com.example.todolistapp.Model;

import com.google.firebase.firestore.Exclude;
import org.checkerframework.checker.nullness.qual.NonNull;

public class TaskID {
    @Exclude // exclude TaskID field from Firestore
    public String TaskID; // field to store task ID

    // sets TaskID and returns the object, supports method chaining in subclasses
    public <T extends TaskID> T withId(@NonNull final String id) {
        this.TaskID = id;
        return (T) this;
    }
}
