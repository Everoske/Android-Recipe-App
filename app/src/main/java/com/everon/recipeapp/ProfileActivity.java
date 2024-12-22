package com.everon.recipeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.everon.recipeapp.adapters.OnRecipeModelClickListener;
import com.everon.recipeapp.adapters.ProfileRecipeRowAdapter;
import com.everon.recipeapp.data.Recipe;
import com.everon.recipeapp.data.RecipeModel;
import com.everon.recipeapp.data.UserWrapper;
import com.everon.recipeapp.nav.NavigationManager;
import com.everon.recipeapp.nav.OnNavigationClickListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity for viewing a user's profile and their recipes.
 */
public class ProfileActivity extends AppCompatActivity implements OnRecipeModelClickListener, OnNavigationClickListener {
    public static final String PROFILE_UID = "profileUid";
    public static final String PROFILE_USERNAME = "profileUsername";
    private String profileUid;
    private boolean isCurrentUser;
    private List<RecipeModel> recipeList;
    private String profileUsername;
    private int recipeCount = 0;
    private int followerCount = 0;
    private ProfileRecipeRowAdapter rowAdapter;

    private CircleImageView profileImageView;
    private TextView usernameTextView;
    private TextView recipeCountTextView;
    private TextView followerCountTextView;
    private RecyclerView recyclerView;
    private Button logoutSubscribeButton;
    private Button editProfileButton;
    private BottomNavigationView navBar;


    // Firebase variables
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private final CollectionReference recipeDatabaseReference = database.collection("recipes");
    private final CollectionReference userDatabaseReference = database.collection("users");

    /**
     * Establish the Profile Activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        recipeList = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            profileUid = bundle.getString(PROFILE_UID);
            profileUsername = bundle.getString(PROFILE_USERNAME);
        } else {
            // No Document Reference, return to feed
            Log.d("ProfileActivity:", "No Bundle, fetching user.");
            fetchUserInformation();
        }

        profileImageView = findViewById(R.id.profileAvatarImageView);
        usernameTextView = findViewById(R.id.profileNameTextView);
        recipeCountTextView = findViewById(R.id.profileRecipeCountTextView);
        followerCountTextView = findViewById(R.id.profileFollowerCountTextView);
        logoutSubscribeButton = findViewById(R.id.profileLogoutFollowButton);
        editProfileButton = findViewById(R.id.profileEditProfileButton);
        navBar = findViewById(R.id.profileActivityNavBar);

        usernameTextView.setText(profileUsername);

        setCountViews();
        setButtons();

        NavigationManager navigationManager = new NavigationManager(this, navBar);
        navigationManager.setNavigationView();

        recyclerView = findViewById(R.id.profileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Load the recipes belonging to the user being viewed.
     */
    @Override
    protected void onStart() {
        super.onStart();

        recipeDatabaseReference.whereEqualTo("userId", profileUid)
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
                                if(isCurrentUser || !recipe.isPrivateRecipe()) {
                                    recipeList.add(recipeItem);
                                }
                            }

                            recipeCount = recipeList.size();
                            String recipeCountText = "Recipes: " + recipeCount;
                            recipeCountTextView.setText(recipeCountText);

                            // Create RecyclerView
                            rowAdapter = new ProfileRecipeRowAdapter(recipeList, ProfileActivity.this, ProfileActivity.this);
                            recyclerView.setAdapter(rowAdapter);
                            rowAdapter.notifyDataSetChanged();
                        } else {
                            // noRecipesTextView.setVisibility(View.VISIBLE);
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
     * Set the number of recipes owned by the users. Display number of followers
     * when this feature is implemented.
     */
    private void setCountViews() {
        String recipeCountText = "Recipes: " + recipeCount;
        String followerCountText = "Followers: " + followerCount;

        recipeCountTextView.setText(recipeCountText);
        followerCountTextView.setText(followerCountText);
    }

    /**
     * Set the buttons based on whether the current user is the owner
     * of the profile being viewed.
     */
    private void setButtons() {
        setIsCurrentUser();
        if(!isCurrentUser) {
            // Not the owner
            editProfileButton.setVisibility(View.INVISIBLE);
            logoutSubscribeButton.setText(R.string.followUserText);
            logoutSubscribeButton.setOnClickListener(view -> {
                // Subscribe User Here
            });
        } else {
            editProfileButton.setOnClickListener(view -> {
                // User Can Edit Profile Here
            });
            logoutSubscribeButton.setOnClickListener(view -> {
                // Logout User
                firebaseAuth.signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            });
        }
    }

    /**
     * Determine if the current user owns the profile being viewed.
     */
    private void setIsCurrentUser() {
        user = firebaseAuth.getCurrentUser();
        if(user != null) {
            isCurrentUser = user.getUid().equals(profileUid);
        } else {
            isCurrentUser = false;
        }
    }

    /**
     * Fetch information on the user being viewed.
     */
    private void fetchUserInformation() {
        user = firebaseAuth.getCurrentUser();
        profileUid = UserWrapper.getInstance().getUserId();
        profileUsername = UserWrapper.getInstance().getUsername();

        if(user == null) {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        } else if (TextUtils.isEmpty(profileUid) || TextUtils.isEmpty(profileUsername)){
            // Assume the Current User is owner of the profile
            final String userId = user.getUid();
            userDatabaseReference
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
                                    profileUid = userId;
                                    profileUsername = snapshot.getString("username");
                                    UserWrapper.getInstance().setUserId(profileUid);
                                    UserWrapper.getInstance().setUsername(profileUsername);
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Launch the Recipe Activity to view recipe being clicked.
     * @param documentReference
     */
    @Override
    public void onRecipeClick(DocumentReference documentReference) {
        Intent intent = new Intent(ProfileActivity.this, RecipeActivity.class);
        intent.putExtra(RecipeActivity.DOCUMENT_REFERENCE, documentReference.getId());
        startActivity(intent);
        finish();
    }

    /**
     * Not accessible on the Profile Activity because the recipes already belong
     * to the user being viewed.
     * @param userUid User id.
     * @param username Username.
     */
    @Override
    public void onRecipeProfileClick(String userUid, String username) {

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
        startActivity(new Intent(ProfileActivity.this, FeedActivity.class));
        finish();
    }

    /**
     * Launch the current user's Profile Activity if the user being viewed is not
     * the current user.
     */
    @Override
    public void onProfileClick() {
        if(!isCurrentUser) {
            startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
        }
    }

    /**
     * Launch the Create Recipe Activity.
     */
    @Override
    public void onRecipeCreateClick() {
        startActivity(new Intent(ProfileActivity.this, CreateRecipe.class));
        finish();
    }
}