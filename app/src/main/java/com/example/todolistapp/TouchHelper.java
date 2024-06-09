package com.example.todolistapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolistapp.Adapter.Adapter;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

// helper class for handling swipe actions in recyclerview
public class TouchHelper extends ItemTouchHelper.SimpleCallback {

    private Adapter adapter; // adapter for the recyclerview

    // constructor to set the adapter and swipe directions
    public TouchHelper(Adapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    // not used, required to override for move actions
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    // handles swipe actions for deleting or editing tasks
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.RIGHT) { // swipe right to delete
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setMessage("Are you sure?")
                    .setTitle("Delete Task")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            adapter.deleteTask(position); // delete task at swiped position
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            adapter.notifyItemChanged(position); // revert swipe on "No"
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else { // swipe left to edit
            adapter.editTask(position); // edit task at swiped position
        }
    }

    // customizes the background and icon of swiped items
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeRightActionIcon(R.drawable.baseline_delete_24) // icon for swipe right
                .addSwipeRightBackgroundColor(Color.RED) // background color for swipe right
                .addSwipeLeftActionIcon(R.drawable.baseline_edit_24) // icon for swipe left
                .addSwipeLeftBackgroundColor(Color.GREEN) // background color for swipe left
                .create()
                .decorate(); // apply the decoration

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
