package com.everon.recipeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.everon.recipeapp.data.UserWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Create Account Activity Controller
 */
public class CreateAccount extends AppCompatActivity {
    private ImageView backArrowImageView;
    private EditText emailEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private MaterialButton createAccountButton;


    // Firebase variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = database.collection("users");

    /**
     * Establishes the Create Account Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();

        backArrowImageView = findViewById(R.id.createAccountReturnImageButton);
        emailEditText = findViewById(R.id.createAccountActivityEmailEditText);
        usernameEditText = findViewById(R.id.createAccountActivityUsernameEditText);
        passwordEditText = findViewById(R.id.createAccountActivityPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.createAccountActivityConfirmPasswordEditText);
        createAccountButton = findViewById(R.id.createAccountActivityCreateAccountButton);

        backArrowImageView.setOnClickListener(view -> {
            startActivity(new Intent(CreateAccount.this, LoginActivity.class));
            finish();
        });

        createAccountButton.setOnClickListener(view -> {
            if(validateInformation()) {
                String email = emailEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                createAccount(email, username, password);
            }
        });
    }

    /**
     * Attempt to create a new account for a user
     * @param email User email
     * @param username Username
     * @param password User password
     */
    private void createAccount(String email, String username, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            String userUid = user.getUid();

                            // Create a user map so we can create a user in the user collection
                            Map<String, String> userObj = new HashMap<>();
                            userObj.put("userId", userUid);
                            userObj.put("username", username);

                            // Save to our Firestore database
                            collectionReference.add(userObj)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            documentReference.get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if(task.getResult().exists()) {
                                                                String name = task.getResult()
                                                                        .getString("username");

                                                                // Add stuff to API and take to new activity
                                                                authorizeUser(userUid);
                                                            }
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("CreateAccount", "Error adding user to users collection");
                                        }
                                    });
                        } else {
                            Toast.makeText(CreateAccount.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
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
     * Validate that all necessary fields are filled out by user
     * @return True if all fields filled out correctly
     */
    private boolean validateInformation() {
        if(!TextUtils.isEmpty(emailEditText.getText().toString())
                && !TextUtils.isEmpty(usernameEditText.getText().toString())
                && !TextUtils.isEmpty(passwordEditText.getText().toString())
                && !TextUtils.isEmpty(confirmPasswordEditText.getText().toString())) {

            // Validate Passwords
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if(password.equals(confirmPassword)) {
                return true;
            }

            Toast.makeText(CreateAccount.this, "Passwords must match!",
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        Toast.makeText(CreateAccount.this, "There are fields missing!",
                Toast.LENGTH_SHORT).show();

        return false;
    }

    /**
     * Set the active user to the user who logged in and launch the
     * Feed Activity for the current user
     * @param currentUserId
     */
    private void authorizeUser(String currentUserId) {
        collectionReference
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException error) {
                        assert queryDocumentSnapshots != null;
                        if(!queryDocumentSnapshots.isEmpty()) {
                            // progressBar.setVisibility(View.INVISIBLE);
                            // Set User Data on Wrapper
                            for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                UserWrapper userWrapper = UserWrapper.getInstance();
                                userWrapper.setUsername(snapshot.getString("username"));
                                userWrapper.setUserId(snapshot.getString("userId"));
                            }

                            // Launch Feed Activity
                            startActivity(new Intent(CreateAccount.this, FeedActivity.class));
                            finish();
                        }
                    }
                });
    }
}