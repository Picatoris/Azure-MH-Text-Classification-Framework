package com.example.sentimentanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class UserSignUpActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etPhone, etRegno;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etRegno = findViewById(R.id.etRegno);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    public void registerUser(View view) {
        String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        String phone = Objects.requireNonNull(etPhone.getText()).toString().trim();
        String regNo = Objects.requireNonNull(etRegno.getText()).toString().trim();

        // Validate input fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || regNo.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters long");
            return;
        }

        if (phone.length() != 10 || !phone.matches("\\d+")) {
            etPhone.setError("Enter a valid 10-digit phone number");
            return;
        }

        // Create User object
        User1 user = new User1(username, email, phone, password, regNo);

        // Store data in Firebase
        databaseReference.child(username).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Signed Up Successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity after successful registration
                    } else {
                        Toast.makeText(getApplicationContext(), "Sign Up Failed. Try Again!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void openLoginActivity(View view) {
        startActivity(new Intent(this, UserLoginActivity.class));
    }
}

class User1 {
    private final String username;
    private final String email;
    private final String password;
    private final String phone;
    private final String regNo;

    public User1(String username, String email, String phone, String password, String regNo) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.regNo = regNo;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getRegNo() {
        return regNo;
    }
}