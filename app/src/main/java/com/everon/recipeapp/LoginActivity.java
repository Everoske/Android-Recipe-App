package com.everon.recipeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.everon.recipeapp.data.UserWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Activity for logging a user into their account using Google Authentication.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private MaterialButton loginButton;
    private TextView forgotPasswordTextView;
    private MaterialButton signupButton;

    // Firebase variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = database.collection("users");

    /**
     * Establish the login page.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.loginActivityEmailEditText);
        passwordEditText = findViewById(R.id.loginActivityPasswordEditText);
        loginButton = findViewById(R.id.loginActivityLoginButton);
        forgotPasswordTextView = findViewById(R.id.loginActivityForgotPasswordTextView);
        signupButton = findViewById(R.id.loginActivitySignUpButton);

        signupButton.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, CreateAccount.class));
        });

        loginButton.setOnClickListener(view -> {
            if(validateInformation()) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                loginWithEmailPassword(email, password);
            }
        });
    }

    /**
     * Check if a user is already signed in. If so, skip this activity
     * and take them to the Feed Activity.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            // If User not null move to feed activity
            final String currentUserId = currentUser.getUid();
            authorizeUser(currentUserId);
        }
    }

    /**
     * Login the user using their email and password.
     * @param email User email.
     * @param password User password.
     */
    private void loginWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null) {
                            final String currentUserId = user.getUid();
                            // Check collection for user reference
                            authorizeUser(currentUserId);
                        } else {
                            Toast.makeText(LoginActivity.this, "Wrong email or password", Toast.LENGTH_SHORT).show();
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
     * Validate that all fields are properly filled.
     * @return True if all fields filled.
     */
    private boolean validateInformation() {
        if(!TextUtils.isEmpty(emailEditText.getText().toString()) &&
        !TextUtils.isEmpty(passwordEditText.getText().toString())) return true;

        Toast.makeText(LoginActivity.this, "Parameters missing!", Toast.LENGTH_SHORT).show();

        return false;
    }

    /**
     * Authorize the logged-in user as the current user and take them to the
     * Feed Activity.
     * @param currentUserId Id of logged-in user.
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
                            startActivity(new Intent(LoginActivity.this, FeedActivity.class));
                            finish();
                        }
                    }
                });
    }
}