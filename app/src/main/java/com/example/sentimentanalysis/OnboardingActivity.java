package com.example.sentimentanalysis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private LinearLayout dotsLayout;
    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if already onboarded
        SharedPreferences prefs = getSharedPreferences("onboarding", MODE_PRIVATE);
        if (prefs.getBoolean("isFirstTime", false)) {
            startActivity(new Intent(this, UserLoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        btnGetStarted = findViewById(R.id.btn_get_started);

        OnboardingAdapter onboardingAdapter = new OnboardingAdapter(this);
        viewPager.setAdapter(onboardingAdapter);

        setupDots(0);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                btnGetStarted.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
            }
        });

        btnGetStarted.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstTime", true);
            editor.apply();

            startActivity(new Intent(OnboardingActivity.this, UserLoginActivity.class));
            finish();
        });
    }

    private void setupDots(int position) {
        dotsLayout.removeAllViews();
        for (int i = 0; i < 3; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
            params.setMargins(10, 0, 10, 0);
            dot.setLayoutParams(params);
            dot.setBackground(ContextCompat.getDrawable(this,
                    i == position ? R.drawable.dot_selected : R.drawable.dot_unselected));
            dotsLayout.addView(dot);
        }
    }
}