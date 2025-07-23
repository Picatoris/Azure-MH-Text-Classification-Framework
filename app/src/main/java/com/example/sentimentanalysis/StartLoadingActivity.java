package com.example.sentimentanalysis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class StartLoadingActivity extends Activity {

    private static final int SPLASH_DISPLAY_LENGTH = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_loading);

        // Handler to delay the launch of the MainActivity
        new Handler().postDelayed(() -> {
            Intent mainIntent = new Intent(StartLoadingActivity.this, OnboardingActivity.class);
            startActivity(mainIntent);
            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}