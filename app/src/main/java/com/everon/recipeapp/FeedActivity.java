package com.everon.recipeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.everon.recipeapp.adapters.OnRecipeModelClickListener;
import com.everon.recipeapp.adapters.RecipeRowAdapter;
import com.everon.recipeapp.data.Recipe;
import com.everon.recipeapp.data.RecipeModel;
import com.everon.recipeapp.data.UserWrapper;
import com.everon.recipeapp.nav.NavigationManager;
import com.everon.recipeapp.nav.OnNavigationClickListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Feed for viewing all recipes in the database unless they are marked as private.
 */
public class FeedActivity extends AppCompatActivity implements OnRecipeModelClickListener, OnNavigationClickListener {
    private List<RecipeModel> recipeList;
    private RecyclerView recyclerView;
    private RecipeRowAdapter recipeRowAdapter;
    private TextView noRecipesTextView;
    private BottomNavigationView navBar;

    // Firebase variables
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = database.collection("recipes");

    /**
     * Establishes the Feed Activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        firebaseAuth = FirebaseAuth.getInstance();
        recipeList = new ArrayList<>();

        noRecipesTextView = findViewById(R.id.feedNoRecipeTextView);
        navBar = findViewById(R.id.feedNavBar);

        NavigationManager navigationManager = new NavigationManager(this, navBar);
        navigationManager.setNavigationView();

        recyclerView = findViewById(R.id.feedRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Load recipes not marked as private into the recycler view.
     */
    @Override
    protected void onStart() {
        super.onStart();
        collectionReference.whereEqualTo("privateRecipe", false)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()) {
                            // Prevents duplication onResume()
                            if(!recipeList.isEmpty()) recipeList.clear();

                            for(QueryDocumentSnapshot recipes : queryDocumentSnapshots) {
                                Recipe recipe = recipes.toObject(Recipe.class);
                                DocumentReference documentReference = recipes.getReference();
                                RecipeModel recipeItem = new RecipeModel(recipe, documentReference);
                                recipeList.add(recipeItem);
                            }
                            // Create RecyclerView
                            recipeRowAdapter = new RecipeRowAdapter(recipeList, FeedActivity.this, FeedActivity.this);
                            recyclerView.setAdapter(recipeRowAdapter);
                            recipeRowAdapter.notifyDataSetChanged();
                        } else {
                            noRecipesTextView.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    /**
     * Launches the Recipe Activity to view the selected recipe.
     * @param documentReference Reference to the clicked recipe.
     */
    @Override
    public void onRecipeClick(DocumentReference documentReference) {
        Intent intent = new Intent(FeedActivity.this, RecipeActivity.class);
        intent.putExtra(RecipeActivity.DOCUMENT_REFERENCE, documentReference.getId());
        startActivity(intent);
    }

    /**
     * Launches the Profile Activity for the selected user.
     * @param userUid Selected user id.
     * @param username Selected username.
     */
    @Override
    public void onRecipeProfileClick(String userUid, String username) {
        Intent intent = new Intent(FeedActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.PROFILE_UID, userUid);
        intent.putExtra(ProfileActivity.PROFILE_USERNAME, username);
        startActivity(intent);
    }

    /**
     * TODO: Implement search function.
     */
    @Override
    public void onSearchClick() {

    }

    /**
     * Disabled in the Feed Activity.
     */
    @Override
    public void onFeedClick() {

    }

    /**
     * Open the profile of the current user.
     */
    @Override
    public void onProfileClick() {
        startActivity(new Intent(FeedActivity.this, ProfileActivity.class));
    }

    /**
     * Open the Create Recipe Activity.
     */
    @Override
    public void onRecipeCreateClick() {
        startActivity(new Intent(FeedActivity.this, CreateRecipe.class));
    }
}