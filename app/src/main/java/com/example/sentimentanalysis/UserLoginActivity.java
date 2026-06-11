package com.example.sentimentanalysis;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserLoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    ImageView eyeIcon;
    Button loginButton;
    LinearLayout googleButton;
    TextView forgotPassword, signupText;
    CheckBox rememberMeCheckBox;
    boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "loginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";
    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        // Firebase setup
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // View bindings
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        eyeIcon = findViewById(R.id.eyeIcon);
        loginButton = findViewById(R.id.loginButton);
        googleButton = findViewById(R.id.googleButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        signupText = findViewById(R.id.signupText);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Auto-fill logic
        if (sharedPreferences.getBoolean(KEY_REMEMBER, false)) {
            emailInput.setText(sharedPreferences.getString(KEY_EMAIL, ""));
            passwordInput.setText(sharedPreferences.getString(KEY_PASSWORD, ""));
            rememberMeCheckBox.setChecked(true);
            // Optional: Auto-login immediately
            loginUser();
        }

        // Toggle password visibility
        eyeIcon.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIcon.setImageResource(R.drawable.ic_eye_off);
            } else {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIcon.setImageResource(R.drawable.ic_eye_on);
            }
            passwordInput.setSelection(passwordInput.length());
            isPasswordVisible = !isPasswordVisible;
        });

        loginButton.setOnClickListener(v -> loginUser());

        googleButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        forgotPassword.setOnClickListener(v -> {
            EditText resetEmail = new EditText(this);
            resetEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            new AlertDialog.Builder(this)
                    .setTitle("Reset Password")
                    .setMessage("Enter your email to receive reset instructions")
                    .setView(resetEmail)
                    .setPositiveButton("Send", (dialog, which) -> {
                        String email = resetEmail.getText().toString();
                        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            mAuth.sendPasswordResetEmail(email)
                                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Reset link sent", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        } else {
                            Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        signupText.setOnClickListener(v -> startActivity(new Intent(this, UserSignUpActivity.class)));
    }

    // ---------------------------------------------------------
    //  FIX 1: UPDATED LOGIN METHOD (Uses FirebaseAuth)
    // ---------------------------------------------------------
    private void loginUser() {
        String inputEmail = emailInput.getText().toString().trim();
        String inputPassword = passwordInput.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
            emailInput.setError("Enter a valid email");
            emailInput.requestFocus();
            return;
        }
        if (inputPassword.isEmpty()) {
            passwordInput.setError("Enter your password");
            passwordInput.requestFocus();
            return;
        }

        // authenticate with Firebase Auth
        mAuth.signInWithEmailAndPassword(inputEmail, inputPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login Success! Now fetch extra user details (like username/phone) from DB
                        fetchUserDetailsAndRedirect(inputEmail);

                        // Handle "Remember Me"
                        if (rememberMeCheckBox.isChecked()) {
                            sharedPreferences.edit()
                                    .putString(KEY_EMAIL, inputEmail)
                                    .putString(KEY_PASSWORD, inputPassword)
                                    .putBoolean(KEY_REMEMBER, true)
                                    .apply();
                        } else {
                            sharedPreferences.edit().clear().apply();
                        }
                    } else {
                        Toast.makeText(UserLoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserDetailsAndRedirect(String email) {
        // We need to find the user in the DB to get their username/phone
        // NOTE: This query assumes you saved users with "email" as a child node
        usersRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                String username = userSnap.getKey(); // Assuming Key is Username
                                String phone = userSnap.child("phone").getValue(String.class);
                                String regNo = userSnap.child("regNo").getValue(String.class);

                                Intent intent = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
                                intent.putExtra("username", username);
                                intent.putExtra("email", email);
                                intent.putExtra("phone", phone);
                                intent.putExtra("regNo", regNo);
                                startActivity(intent);
                                finish();
                                return; // Stop after finding the user
                            }
                        } else {
                            // Auth successful, but data missing in DB (Rare edge case)
                            startActivity(new Intent(UserLoginActivity.this, UserDashboardActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserLoginActivity.this, "DB Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ---------------------------------------------------------
    //  FIX 2: SAFER GOOGLE AUTH (Prevents data overwrite)
    // ---------------------------------------------------------
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String username = firebaseUser.getDisplayName();
                            String email = firebaseUser.getEmail();

                            // CHECK if user exists before overwriting!
                            assert username != null;
                            usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        // Only create new data if user is NEW
                                        usersRef.child(username).child("username").setValue(username);
                                        usersRef.child(username).child("email").setValue(email);
                                        usersRef.child(username).child("phone").setValue("0");
                                        usersRef.child(username).child("regNo").setValue("NA");
                                    }

                                    // Proceed to Dashboard
                                    Intent intent = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
                                    intent.putExtra("username", username);
                                    intent.putExtra("email", email);
                                    // Pass existing data if available, else defaults
                                    intent.putExtra("phone", snapshot.child("phone").exists() ? snapshot.child("phone").getValue(String.class) : "0");
                                    intent.putExtra("regNo", snapshot.child("regNo").exists() ? snapshot.child("regNo").getValue(String.class) : "NA");
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        }
                    } else {
                        Toast.makeText(UserLoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}