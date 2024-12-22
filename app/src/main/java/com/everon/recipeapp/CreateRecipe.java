package com.everon.recipeapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.everon.recipeapp.adapters.ItemCreationAdapter;
import com.everon.recipeapp.adapters.OnRecipeItemChange;
import com.everon.recipeapp.adapters.RecyclerRowMoveCallback;
import com.everon.recipeapp.data.ItemModel;
import com.everon.recipeapp.data.Recipe;
import com.everon.recipeapp.data.RecipeItemType;
import com.everon.recipeapp.data.UserWrapper;
import com.everon.recipeapp.nav.NavigationManager;
import com.everon.recipeapp.nav.OnNavigationClickListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Activity for creating a recipe.
 */
public class CreateRecipe extends AppCompatActivity implements
        OnRecipeItemChange, OnNavigationClickListener, DeleteDialog.DeleteDialogListener {
    public static final String RECIPE_REFERENCE = "recipeReference";
    public final String TAG = "CreateRecipeActivity";
    private boolean isEdit;
    private String recipeReference;
    private String originalImageUrl;

    RecyclerView ingredientRecyclerView;
    RecyclerView instructionRecyclerView;
    private List<ItemModel> ingredientList = new ArrayList<>();
    private List<ItemModel> instructionList = new ArrayList<>();

    private FloatingActionButton ingredientFab;
    private FloatingActionButton instructionFab;

    private AddRecipeItemFragment addIngredientFragment;
    private ItemCreationAdapter ingredientAdapter;
    private AddRecipeItemFragment addInstructionFragment;
    private ItemCreationAdapter instructionAdapter;

    private EditText creatorEditText;
    private EditText nameEditText;

    private ImageView imageView;
    private ImageView imageButton;
    private Uri imageUri;

    private MaterialButton submitButton;
    private MaterialButton deleteButton;

    private BottomNavigationView navBar;

    private String currentUserId;
    private String currentUsername;

    // Firebase variables
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = database.collection("recipes");

    /**
     * Gets an image from the user's photo collection and sets it as
     * the current image.
     */
    private ActivityResultLauncher<String> launchGalleryActivity = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if(result != null) {
                        imageUri = result;
                        imageView.setImageURI(imageUri);
                    }
                }
            });


    /**
     * Establishes the Create Recipe Activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        ingredientFab = findViewById(R.id.createRecipeIngredientFab);
        ImageViewCompat.setImageTintList(ingredientFab, ColorStateList.valueOf(Color.WHITE));
        addIngredientFragment = new AddRecipeItemFragment(this, RecipeItemType.INGREDIENT);

        instructionFab = findViewById(R.id.createRecipeInstructionFab);
        ImageViewCompat.setImageTintList(instructionFab, ColorStateList.valueOf(Color.WHITE));
        addInstructionFragment = new AddRecipeItemFragment(this, RecipeItemType.INSTRUCTION);

        ingredientRecyclerView = findViewById(R.id.createRecipeIngredientsRecyclerView);
        LinearLayoutManager ingredientManager = new LinearLayoutManager(this);
        ingredientRecyclerView.setLayoutManager(ingredientManager);
        ingredientAdapter = new ItemCreationAdapter(ingredientList, RecipeItemType.INGREDIENT, this);

        instructionRecyclerView = findViewById(R.id.createRecipeInstructionsRecyclerView);
        LinearLayoutManager instructionManager = new LinearLayoutManager(this);
        instructionRecyclerView.setLayoutManager(instructionManager);
        instructionAdapter = new ItemCreationAdapter(instructionList, RecipeItemType.INSTRUCTION, this);

        ItemTouchHelper.Callback ingredientCallback = new RecyclerRowMoveCallback(ingredientAdapter);
        ItemTouchHelper ingredientTouchHelper = new ItemTouchHelper(ingredientCallback);
        ingredientTouchHelper.attachToRecyclerView(ingredientRecyclerView);

        ItemTouchHelper.Callback instructionCallback = new RecyclerRowMoveCallback(instructionAdapter);
        ItemTouchHelper instructionTouchHelper = new ItemTouchHelper(instructionCallback);
        instructionTouchHelper.attachToRecyclerView(instructionRecyclerView);

        creatorEditText = findViewById(R.id.createRecipeCreatorEditText);
        nameEditText = findViewById(R.id.createRecipeNameEditText);
        submitButton = findViewById(R.id.createRecipeSubmitButton);
        deleteButton = findViewById(R.id.createRecipeDeleteButton);

        imageView = findViewById(R.id.createRecipeImageView);
        imageButton = findViewById(R.id.createRecipePostImageButton);

        ingredientRecyclerView.setAdapter(ingredientAdapter);
        instructionRecyclerView.setAdapter(instructionAdapter);

        navBar = findViewById(R.id.createRecipeNavBar);
        NavigationManager navigationManager = new NavigationManager(this, navBar);
        navigationManager.setNavigationView();

        // Firebase stuff
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = storage.getReference();
        user = firebaseAuth.getCurrentUser();

        if(user == null) {
            // Launch Login Activity
            startActivity(new Intent(CreateRecipe.this, LoginActivity.class));
            finish();
        }

        Bundle bundle = getIntent().getExtras();


        // Check if user is editing document and set accordingly
        if(bundle != null) {
            isEdit = true;
            recipeReference = bundle.getString(RECIPE_REFERENCE);
        } else {
            isEdit = false;
        }

        ingredientFab.setOnClickListener(view -> {
            showIngredientSheetDialog();
        });

        instructionFab.setOnClickListener(view -> {
            showInstructionSheetDialog();
        });

        currentUsername = UserWrapper.getInstance().getUsername();

        if(!TextUtils.isEmpty(currentUsername)) {
            creatorEditText.setText(currentUsername);
        }

        submitButton.setOnClickListener(view -> {
            if(validateInformation()) {
                if(isEdit) {
                    updateRecipe();
                } else {
                    saveRecipe();
                }
            } else {
                Toast.makeText(this, "Missing Parameters!", Toast.LENGTH_SHORT).show();
            }
        });

        imageButton.setOnClickListener(view -> {
            // Get Image from Gallery/Phone
            launchGalleryActivity.launch("image/*");
        });
    }

    /**
     * Loads an existing recipe if this is an edit.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (!isEdit) return;

        collectionReference.document(recipeReference)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()) {
                            Recipe recipe = documentSnapshot.toObject(Recipe.class);
                            if(recipe != null) {
                                displayRecipe(recipe);
                                originalImageUrl = recipe.getImageUrl();
                                deleteButton.setVisibility(View.VISIBLE);
                                deleteButton.setOnClickListener(view -> {
                                    openDeleteDialog();
                                });
                            }
                        } else {
                            isEdit = false;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Create Recipe Activity", "Error Accessing Users Files: " + e.getMessage());
                    }
                });
    }

    /**
     * Saves the current recipe.
     */
    private void saveRecipe() {
        // Create Recipe
        if(imageUri == null) {
            imageUri = drawableToUri(R.drawable.default_recipe_image);
        }
        String name = nameEditText.getText().toString().trim();
        String creator = creatorEditText.getText().toString().trim();
        currentUserId = UserWrapper.getInstance().getUserId();
        currentUsername = UserWrapper.getInstance().getUsername();

        if(TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(currentUsername)) fetchCurrentUser();

        ArrayList<String> ingredients = itemToStringList(ingredientList);
        ArrayList<String> instructions = itemToStringList(instructionList);

        StorageReference filepath = storageReference
                .child("recipe_images")
                // Ensure our file has a unique id by using a timestamp
                .child("my_recipe_" + Timestamp.now().getSeconds()); // my_recipe_812131313

        // Submit to Firebase
        filepath.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();

                                // Create Recipe Object
                                Recipe recipe = new Recipe();
                                recipe.setName(name);
                                recipe.setCreator(creator);
                                recipe.setImageUrl(imageUrl);
                                recipe.setTimeAdded(new Timestamp(new Date()));
                                recipe.setUserId(currentUserId);
                                recipe.setUsername(currentUsername);
                                recipe.setIngredients(ingredients);
                                recipe.setSteps(instructions);
                                recipe.setPrivate(false);

                                // Invoke collectionReference
                                collectionReference.add(recipe)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                // Launch new activity
                                                Intent intent = new Intent(CreateRecipe.this, RecipeActivity.class);
                                                intent.putExtra(RecipeActivity.DOCUMENT_REFERENCE, documentReference.getId());
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: " + e.getMessage());
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "on Failure: " + e.getMessage());
                    }
                });
    }

    /**
     * TODO: Implement ability to edit existing recipe
     */
    private void updateRecipe() {

    }

    /**
     * Opens a confirmation dialog to confirm the user wants to delete an
     * existing recipe.
     */
    private void openDeleteDialog() {
        DeleteDialog deleteDialog = new DeleteDialog();
        deleteDialog.show(getSupportFragmentManager(), "Delete Dialog");
    }

    /**
     * Deletes an existing recipe.
     */
    private void deleteRecipe() {
        StorageReference imageRef = storage.getReferenceFromUrl(originalImageUrl);

        collectionReference.document(recipeReference)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("CreateRecipe:", "DocumentSnapshot successfully deleted");

                        imageRef.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("CreateRecipe:", "Image successfully deleted");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("CreateRecipe:", "Error deleting image");
                                    }
                                });

                        startActivity(new Intent(CreateRecipe.this, FeedActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("CreateRecipe:", "Error deleting document");
                    }
                });
    }

    /**
     * Converts a list of recipe items to a list of strings.
     * @param itemList List of recipe items.
     * @return List of string representations of recipe items.
     */
    private ArrayList<String> itemToStringList(List<ItemModel> itemList) {
        ArrayList<String> stringList = new ArrayList<>();
        for(ItemModel model : itemList) {
            stringList.add(model.getInformation());
        }
        return stringList;
    }

    /**
     * Opens a fragment to add ingredients to a recipe.
     */
    private void showIngredientSheetDialog() {
        addIngredientFragment.show(getSupportFragmentManager(), addIngredientFragment.getTag());
    }

    /**
     * Opens a fragment to add steps to a recipe.
     */
    private void showInstructionSheetDialog() {
        addInstructionFragment.show(getSupportFragmentManager(), addInstructionFragment.getTag());
    }

    /**
     * Updates the ingredients and steps with new recipe item data.
     * @param itemList List of items.
     * @param type Ingredients or steps.
     */
    @Override
    public void updateItemList(List<ItemModel> itemList, RecipeItemType type) {
        switch(type) {
            case INGREDIENT:
                this.ingredientList = itemList;
                break;
            case INSTRUCTION:
                this.instructionList = itemList;
                break;
            default:
                break;
        }
    }

    /**
     * Adds an item to ingredients or steps.
     * @param item Item to add.
     * @param type Ingredients or steps.
     */
    @Override
    public void addItem(ItemModel item, RecipeItemType type) {
        switch(type) {
            case INGREDIENT:
                this.ingredientList.add(item);
                ingredientAdapter.notifyItemInserted(ingredientList.size() - 1);
                break;
            case INSTRUCTION:
                this.instructionList.add(item);
                instructionAdapter.notifyItemInserted(instructionList.size() - 1);
                break;
            default:
                break;
        }
    }

    /**
     * Validates that there are no empty fields.
     * @return True if all fields are filled.
     */
    private boolean validateInformation() {
        return !TextUtils.isEmpty(creatorEditText.getText()) && !TextUtils.isEmpty(nameEditText.getText())
        && !ingredientList.isEmpty() && !instructionList.isEmpty();
    }

    /**
     * Build drawable resource.
     * @param resId Drawable id.
     * @return URI.
     */
    private Uri drawableToUri(int resId) {
        Resources resources = getResources();
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resId))
                .appendPath(resources.getResourceTypeName(resId))
                .appendPath(resources.getResourceEntryName(resId))
                .build();
    }

    /**
     * Finds the current user.
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
                                currentUserId = userId;
                                currentUsername = snapshot.getString("username");
                                UserWrapper.getInstance().setUserId(currentUserId);
                                UserWrapper.getInstance().setUsername(currentUsername);
                            }
                        }
                    }
                });
    }

    /**
     * Display an existing recipe.
     * @param recipe Existing recipe from database.
     */
    private void displayRecipe(Recipe recipe) {
        String creator = recipe.getCreator();
        String name = recipe.getName();

        nameEditText.setText(name);
        creatorEditText.setText(creator);

        for(int i = 0; i < recipe.getIngredients().size(); i++) {
            ItemModel ingredientModel = new ItemModel(i, recipe.getIngredients().get(i));
            ingredientList.add(ingredientModel);
        }
        ingredientAdapter.notifyDataSetChanged();

        for(int i = 0; i < recipe.getSteps().size(); i++) {
            ItemModel stepModel = new ItemModel(i, recipe.getSteps().get(i));
            instructionList.add(stepModel);
        }
        instructionAdapter.notifyDataSetChanged();

        StorageReference reference = storage.getReferenceFromUrl(recipe.getImageUrl());

        GlideApp.with(this)
                .load(reference)
                .placeholder(R.drawable.default_recipe_image)
                .into(imageView);
    }

    /**
     * TODO: Implement search function.
     */
    @Override
    public void onSearchClick() {

    }

    /**
     * Launch Feed Activity.
     */
    @Override
    public void onFeedClick() {
        startActivity(new Intent(CreateRecipe.this, FeedActivity.class));
        finish();
    }

    /**
     * Launch Profile Activity.
     */
    @Override
    public void onProfileClick() {
        startActivity(new Intent(CreateRecipe.this, ProfileActivity.class));
    }

    /**
     * This is not accessible in the Create Recipe Activity.
     */
    @Override
    public void onRecipeCreateClick() {
        // Do Nothing
    }

    /**
     * Delete recipe on confirmation.
     */
    @Override
    public void onYesClicked() {
        deleteRecipe();
    }
}