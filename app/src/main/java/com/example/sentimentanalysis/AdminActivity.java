package com.example.sentimentanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Finding UI elements
        CardView cardLogin = findViewById(R.id.cardLogin);
        CardView cardSignUp = findViewById(R.id.cardSignUp);

        // Set click listeners for login and signup
        cardLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLoginActivity();
            }
        });

        cardSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignUpActivity();
            }
        });
    }

    // Navigate to AdminLoginActivity
    private void startLoginActivity() {
        Intent intent = new Intent(this, AdminLoginActivity.class);
        intent.putExtra("user_type", "admin");
        startActivity(intent);
    }

    // Navigate to AdminSignUpActivity
    private void startSignUpActivity() {
        Intent intent = new Intent(this, AdminSignUpActivity.class);
        startActivity(intent);
    }

    public void startLoginActivity(View view) {
    }

    public void startSignUpActivity(View view) {
    }
}