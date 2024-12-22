package com.everon.recipeapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

/**
 * Confirmation dialog to delete an existing recipe.
 */
public class DeleteDialog extends AppCompatDialogFragment {
    private DeleteDialogListener listener;

    /**
     * Establish delete dialog.
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     * @return Delete dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning").setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Cancel", (((dialogInterface, i) -> { })))
                .setNegativeButton("Okay", ((dialogInterface, i) -> {listener.onYesClicked();}));
        return builder.create();
    }

    /**
     * Listener for yes clicked.
     */
    public interface DeleteDialogListener {
        /**
         * Respond to yes confirmation.
         */
        void onYesClicked();
    }

    /**
     * Attach listener to a context that implements DeleteDialogListener.
     * @param context View that implements DeleteDialogListener.
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (DeleteDialogListener) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context + " must implement delete dialog listener");
        }
    }
}
