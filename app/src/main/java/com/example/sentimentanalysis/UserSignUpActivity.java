package com.example.sentimentanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserSignUpActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etPhone, etRegno;
    private Button btnRegister;

    // Firebase Instances
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);

        // 1. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // 2. Initialize Views
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etRegno = findViewById(R.id.etRegno);
        btnRegister = findViewById(R.id.btnRegister); // Ensure your XML button has this ID
        TextView tvLoginLink = findViewById(R.id.tvLoginLink); // Ensure your XML "Login" text has this ID

        // 3. Set Click Listeners
        btnRegister.setOnClickListener(v -> performRegistration());

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(UserSignUpActivity.this, UserLoginActivity.class));
            finish();
        });
    }

    private void performRegistration() {
        // Get data safely (avoid Objects.requireNonNull crash)
        String username = getTextSafe(etUsername);
        String email = getTextSafe(etEmail);
        String password = getTextSafe(etPassword);
        String phone = getTextSafe(etPhone);
        String regNo = getTextSafe(etRegno);

        // --- VALIDATIONS ---
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || regNo.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase keys cannot contain these characters
        if (username.contains(".") || username.contains("#") || username.contains("$") || username.contains("[") || username.contains("]")) {
            etUsername.setError("Username cannot contain . # $ [ ]");
            etUsername.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid Email");
            etEmail.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password too short (min 6 chars)");
            etPassword.requestFocus();
            return;
        }

        if (phone.length() != 10) {
            etPhone.setError("Phone must be 10 digits");
            etPhone.requestFocus();
            return;
        }

        // --- THE FIX: AUTHENTICATION FIRST ---
        btnRegister.setEnabled(false); // Prevent double clicking
        Toast.makeText(this, "Creating Account...", Toast.LENGTH_SHORT).show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 1. Auth Successful
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        // 2. Now Save to Database
                        saveUserToRealtimeDatabase(username, email, phone, password, regNo);
                    } else {
                        // Auth Failed
                        btnRegister.setEnabled(true);
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                        Toast.makeText(UserSignUpActivity.this, "Registration Failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToRealtimeDatabase(String username, String email, String phone, String password, String regNo) {
        UserHelperClass user = new UserHelperClass(username, email, phone, password, regNo);

        // Saving under 'username' as the key (Consistent with your Login Activity)
        databaseReference.child(username).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UserSignUpActivity.this, "Registered Successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to Dashboard or Login
                        Intent intent = new Intent(UserSignUpActivity.this, UserLoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        btnRegister.setEnabled(true);
                        Toast.makeText(UserSignUpActivity.this, "DB Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper to get text safely
    private String getTextSafe(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) return "";
        return editText.getText().toString().trim();
    }
}

// --- USER MODEL ---
// (Kept non-public so it fits in this file comfortably)
class UserHelperClass {
    public String username, email, phone, password, regNo;

    public UserHelperClass() { } // Required for Firebase

    public UserHelperClass(String username, String email, String phone, String password, String regNo) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.regNo = regNo;
    }
}