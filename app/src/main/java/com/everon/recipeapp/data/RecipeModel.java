package com.everon.recipeapp.data;

import com.google.firebase.firestore.DocumentReference;

/**
 * Model for recipes displayed in a recipe recycler view.
 */
public class RecipeModel {
    private Recipe recipe;
    private DocumentReference documentReference;

    public RecipeModel(Recipe recipe, DocumentReference documentReference) {
        this.recipe = recipe;
        this.documentReference = documentReference;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }
}
