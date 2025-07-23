package com.example.sentimentanalysis;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
    Button loginButton, phoneButton;
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
                .requestIdToken(getString(R.string.default_web_client_id))  // from google-services.json
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleButton = findViewById(R.id.googleButton);

        // Google Button Click
        googleButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

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

        // Auto-fill if remembered
        if (sharedPreferences.getBoolean(KEY_REMEMBER, false)) {
            emailInput.setText(sharedPreferences.getString(KEY_EMAIL, ""));
            passwordInput.setText(sharedPreferences.getString(KEY_PASSWORD, ""));
            rememberMeCheckBox.setChecked(true);
            loginUser();  // Auto-login
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

    private void loginUser() {
        String inputEmail = emailInput.getText().toString().trim();
        String inputPassword = passwordInput.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
            emailInput.setError("Enter a valid email");
            return;
        }
        if (inputPassword.isEmpty()) {
            passwordInput.setError("Enter your password");
            return;
        }

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;

                for (DataSnapshot usernameSnap : snapshot.getChildren()) {
                    String email = usernameSnap.child("email").getValue(String.class);
                    String password = usernameSnap.child("password").getValue(String.class);

                    if (email != null && password != null &&
                            email.equals(inputEmail) && password.equals(inputPassword)) {

                        String username = usernameSnap.getKey();
                        String phone = usernameSnap.child("phone").getValue(String.class);
                        String regNo = usernameSnap.child("regNo").getValue(String.class);

                        if (rememberMeCheckBox.isChecked()) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(KEY_EMAIL, inputEmail);
                            editor.putString(KEY_PASSWORD, inputPassword);
                            editor.putBoolean(KEY_REMEMBER, true);
                            editor.apply();
                        } else {
                            sharedPreferences.edit().clear().apply();
                        }

                        Toast.makeText(UserLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("email", email);
                        intent.putExtra("phone", phone);
                        intent.putExtra("regNo", regNo);
                        startActivity(intent);
                        finish();
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(UserLoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserLoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                Log.e("GOOGLE_SIGN_IN", "signInResult:failed code=" + e.getStatusCode(), e);
                Toast.makeText(this, "Google Sign In failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String username = firebaseUser.getDisplayName();
                            String email = firebaseUser.getEmail();

                            // Save to Firebase Realtime Database
                            assert username != null;
                            usersRef.child(username).child("username").setValue(username);
                            usersRef.child(username).child("email").setValue(email);
                            usersRef.child(username).child("password").setValue("NA");
                            usersRef.child(username).child("phone").setValue("0");
                            usersRef.child(username).child("regNo").setValue("NA");

                            // Navigate to dashboard
                            Intent intent = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("email", email);
                            intent.putExtra("phone", "0");
                            intent.putExtra("regNo", "NA");
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(UserLoginActivity.this, "Firebase Auth failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
