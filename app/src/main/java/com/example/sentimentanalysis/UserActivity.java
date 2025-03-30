package com.example.sentimentanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initializing card views
        CardView cardUserLogin = findViewById(R.id.cardUserLogin);
        CardView cardUserSignUp = findViewById(R.id.cardUserSignUp);

        // Set click listeners for the login card
        cardUserLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserActivity.this, UserLoginActivity.class));
            }
        });

        // Set click listeners for the sign-up card
        cardUserSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserActivity.this, UserSignUpActivity.class));
            }
        });
    }
}