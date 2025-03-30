package com.example.sentimentanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void startAdminActivity(View view) {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }

    public void startUserActivity(View view) {
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
    }
}
