package com.everon.recipeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.everon.recipeapp.data.Recipe;
import com.everon.recipeapp.data.UserWrapper;
import com.everon.recipeapp.nav.NavigationManager;
import com.everon.recipeapp.nav.OnNavigationClickListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

/**
 * Activity for Viewing a Recipe.
 */
public class RecipeActivity extends AppCompatActivity implements OnNavigationClickListener {
    public static final String DOCUMENT_REFERENCE = "documentReference";
    public boolean isOwner;

    private ImageView recipeImage;
    private TextView recipeNameTextView;
    private TextView creatorTextView;
    private TextView ingredientsTextView;
    private TextView instructionsTextView;
    private Button editDownloadButton;
    private BottomNavigationView navBar;

    private Recipe recipe;
    private String documentReference;

    // Firebase variables
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = database.collection("recipes");

    /**
     * Establishes the Recipe Activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            documentReference = bundle.getString(DOCUMENT_REFERENCE);
        } else {
            // No Document Reference, return to feed
            startActivity(new Intent(RecipeActivity.this, CreateRecipe.class));
            finish();
        }

        recipeImage = findViewById(R.id.recipeActivityImageView);
        recipeNameTextView = findViewById(R.id.recipeActivityNameTextView);
        creatorTextView = findViewById(R.id.recipeActivityRecipeCreatorTextView);
        ingredientsTextView = findViewById(R.id.recipeActivityIngredientsTextView);
        instructionsTextView = findViewById(R.id.recipeActivityInstructionsTextView);
        editDownloadButton = findViewById(R.id.recipeActivityEditButton);

        navBar = findViewById(R.id.recipeActivityNavBar);
        NavigationManager navigationManager = new NavigationManager(this, navBar);
        navigationManager.setNavigationView();

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = storage.getReference();
        user = firebaseAuth.getCurrentUser();
    }

    /**
     * Check whether recipe exists and decide whether to
     * load recipe or return to Feed Activity.
     */
    @Override
    protected void onStart() {
        super.onStart();

        collectionReference.document(documentReference)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()) {
                            recipe = documentSnapshot.toObject(Recipe.class);
                            if(recipe != null) {
                                displayRecipe();
                                setEditDownloadButton();
                            }
                        } else {
                            // No recipes found. Return to feed
                            startActivity(new Intent(RecipeActivity.this, FeedActivity.class));
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("RecipeActivity:OnStart:", "Error Accessing Files");
                    }
                });
    }

    /**
     * Display a recipe in the activity.
     */
    private void displayRecipe() {
        String creator = recipe.getCreator();
        String name = recipe.getName();

        creatorTextView.setText("Recipe by " + creator);
        recipeNameTextView.setText(name);

        StorageReference reference = storage.getReferenceFromUrl(recipe.getImageUrl());

        processRecipeImage(reference);
        processRecipeSteps(recipe.getIngredients(), recipe.getSteps());

    }

    /**
     * Download button should show if the user does not 'own'
     * the recipe. Edit should show if the user 'owns' the
     * recipe.
     */
    private void setEditDownloadButton() {
        // Determine ownership
        isOwner = checkOwnership(recipe.getUserId());

        if(!isOwner) {
            editDownloadButton.setText(R.string.downloadRecipeText);
            editDownloadButton.setBackgroundResource(R.drawable.solid_rounded_button);
            editDownloadButton.setTextColor(Color.WHITE);
        }

        editDownloadButton.setOnClickListener(view -> {
            if(isOwner) {
                editRecipe();
            } else {
                downloadRecipe();
            }
        });
    }

    /**
     * Launch the Edit Recipe Activity.
     */
    private void editRecipe() {
        Intent intent = new Intent(RecipeActivity.this, CreateRecipe.class);
        intent.putExtra(CreateRecipe.RECIPE_REFERENCE, documentReference);
        startActivity(intent);
    }

    /**
     * Launch the Download Recipe Activity
     */
    private void downloadRecipe() {
        if(user == null) return;
        copyImageFromFirebase();
    }

    /**
     * Downloads the recipe for a non-owner user by saving the recipe
     * as a new recipe belonging to the current user.
     * @param stream
     */
    private void saveRecipe(InputStream stream) {
        if(UserWrapper.getInstance().checkIfEmpty()) fetchCurrentUser();
        String currentUserId = UserWrapper.getInstance().getUserId();
        String currentUsername = UserWrapper.getInstance().getUsername();

        StorageReference filepath = storageReference
                .child("recipe_images")
                // Ensure our file has a unique id by using a timestamp
                .child("my_recipe_" + Timestamp.now().getSeconds()); // my_recipe_812131313

        // Submit to Firebase
        filepath.putStream(stream)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();

                                Recipe newRecipe = new Recipe();
                                newRecipe.setUserId(currentUserId);
                                newRecipe.setUsername(currentUsername);
                                newRecipe.setCreator(recipe.getCreator());
                                newRecipe.setName(recipe.getName());
                                newRecipe.setSteps(recipe.getSteps());
                                newRecipe.setIngredients(recipe.getIngredients());
                                newRecipe.setImageUrl(imageUrl);
                                newRecipe.setTimeAdded(new Timestamp(new Date()));
                                newRecipe.setPrivate(true);

                                // Invoke collectionReference
                                collectionReference.add(newRecipe)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                // Launch new activity
                                                Intent intent = new Intent(RecipeActivity.this, RecipeActivity.class);
                                                intent.putExtra(RecipeActivity.DOCUMENT_REFERENCE, documentReference.getId());
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("RecipeActivity: Uploading Recipe: ", "onFailure: " + e.getMessage());
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("RecipeActivity: Uploading Image: ", "on Failure: " + e.getMessage());
                    }
                });
    }

    /**
     * Determine who the current user is.
     */
    private void fetchCurrentUser() {
        final String userId = user.getUid();

        collectionReference
                .whereEqualTo("userId", userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        assert queryDocumentSnapshots != null;
                        if(!queryDocumentSnapshots.isEmpty()) {
                            // progressBar.setVisibility(View.INVISIBLE);
                            // Set User Data on Wrapper
                            for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                String currentUsername = snapshot.getString("username");
                                UserWrapper.getInstance().setUserId(userId);
                                UserWrapper.getInstance().setUsername(currentUsername);
                            }
                        }
                    }
                });
    }

    /**
     * Import the recipe image from the images database.
     * @param reference
     */
    private void processRecipeImage(StorageReference reference) {
        // Download directly from StorageReference using Glide
        GlideApp.with(this)
                .load(reference)
                .placeholder(R.drawable.default_recipe_image)
                .into(recipeImage);
    }

    /**
     * Add the recipe steps and ingredients to their respective list views.
     * @param ingredients
     * @param instructions
     */
    private void processRecipeSteps(ArrayList<String> ingredients, ArrayList<String> instructions) {
        StringBuilder ingredientBuilder = new StringBuilder();
        StringBuilder instructionBuilder = new StringBuilder();

        for(int i = 0; i < ingredients.size(); i++) {
            String step = (i + 1) + ". " + ingredients.get(i);
            ingredientBuilder.append(step);
            if((i + 1) < ingredients.size()) {
                ingredientBuilder.append("\n\n");
            }
        }

        for(int i = 0; i < instructions.size(); i++) {
            String step = "Step " + (i + 1) + ": " + instructions.get(i);
            instructionBuilder.append(step);
            if((i + 1) < instructions.size()) {
                instructionBuilder.append("\n\n");
            }
        }

        ingredientsTextView.setText(ingredientBuilder.toString());
        instructionsTextView.setText(instructionBuilder.toString());
    }

    /**
     * Determine if the user is the owner of the recipe.
     * @param userUid
     * @return True if the current user 'owns' the recipe.
     */
    private boolean checkOwnership(String userUid) {
        user = firebaseAuth.getCurrentUser();

        if(user != null) {
            String currentUserId = user.getUid();
            return userUid.equals(currentUserId);
        }
        return false;
    }

    /**
     * Copy an existing image from the images file store to be
     * assigned to a copy of the current recipe.
     */
    private void copyImageFromFirebase() {
        File file;

        try {
            file = File.createTempFile("tempImage_" + Timestamp.now().getSeconds(), ".jpg", this.getCacheDir());
            StorageReference imageRef = storage.getReferenceFromUrl(recipe.getImageUrl());

            imageRef.getFile(file)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d("RecipeActivity:", "Successfully downloaded " + file.toString());
                            try {
                                InputStream stream = new FileInputStream(file);
                                saveRecipe(stream);
                            } catch (FileNotFoundException e) {
                                Log.d("RecipeActivity:", "FileNoteFoundException: " + e.getMessage());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("RecipeActivity:", "Unable to download image");
                        }
                    });
        } catch(IOException e) {
            Log.d("RecipeActivity:", "IOException: Unable to create temp file: " + e.getMessage());
        }
    }

    /**
     * TODO: Implement search functionality.
     */
    @Override
    public void onSearchClick() {

    }

    /**
     * Launch Feed Activity.
     */
    @Override
    public void onFeedClick() {
        startActivity(new Intent(RecipeActivity.this, FeedActivity.class));
        finish();
    }

    /**
     * Launch Profile Activity.
     */
    @Override
    public void onProfileClick() {
        startActivity(new Intent(RecipeActivity.this, ProfileActivity.class));
    }

    /**
     * Launch Create Recipe Activity.
     */
    @Override
    public void onRecipeCreateClick() {
        startActivity(new Intent(RecipeActivity.this, CreateRecipe.class));
        finish();
    }
}