package com.example.sentimentanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        // Initializing UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        // Set onClickListeners
        btnLogin.setOnClickListener(this::loginUser);
        tvSignUp.setOnClickListener(this::openUserSignUpActivity);
        tvForgotPassword.setOnClickListener(this::resetPassword);
    }

    public void loginUser(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query Firebase to check if user exists
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String storedPassword = userSnapshot.child("password").getValue(String.class);
                        String regNo = userSnapshot.child("regNo").getValue(String.class);
                        if (password.equals(storedPassword)) {
                            // Authentication success
                            Intent intent = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
                            intent.putExtra("regNo", regNo);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            Toast.makeText(UserLoginActivity.this, "Log In Successful!", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    // Password doesn't match
                    Toast.makeText(UserLoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                } else {
                    // User not found in the database
                    Toast.makeText(UserLoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserLoginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openUserSignUpActivity(View view) {
        Intent intent = new Intent(this, UserSignUpActivity.class);
        startActivity(intent);
    }

    public void resetPassword(View view) {
        // Navigate to Forgot Password activity
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
}