package com.everon.recipeapp.adapters;

import com.google.firebase.firestore.DocumentReference;

/**
 * Handles Recipe Click and Profile Click events for recipe objects in
 * a recycler view.
 */
public interface OnRecipeModelClickListener {
    /**
     * Handle recipe clicked.
     * @param documentReference Document pointing to recipe in database.
     */
    void onRecipeClick(DocumentReference documentReference);

    /**
     * Handle user profile clicked.
     * @param userUid User id.
     * @param username Username.
     */
    void onRecipeProfileClick(String userUid, String username);
}
