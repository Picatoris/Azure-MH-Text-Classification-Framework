package com.example.sentimentanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminSignUpActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etPhone;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sign_up);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);

        // Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("admin");
    }

    public void registerUser(View view) {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() != 10 || !phone.matches("[0-9]+")) {
            Toast.makeText(this, "Enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user object
        User user = new User(username, email, phone, password);

        // Store user in Firebase with unique username
        databaseReference.child(username).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                if (error != null) {
                    Toast.makeText(AdminSignUpActivity.this, "Registration failed. Try again!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminSignUpActivity.this, "Signed Up Successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AdminSignUpActivity.this, AdminLoginActivity.class));
                    finish();
                }
            }
        });
    }

    public void openAdminLoginActivity(View view) {
        startActivity(new Intent(AdminSignUpActivity.this, AdminLoginActivity.class));
        finish();
    }
}

class User {
    private String username;
    private String email;
    private String phone;
    private String password;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String username, String email, String phone, String password) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }
}