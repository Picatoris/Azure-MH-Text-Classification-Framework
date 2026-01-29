package com.example.sentimentanalysis;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WaterQuestActivity extends AppCompatActivity {

    private View viewWaterLevel;
    private FrameLayout waterContainer; // The parent container
    private TextView tvWaterCount, tvGoal;
    private SharedPreferences prefs;
    private int maxContainerHeight = 0; // Dynamic height
    private int currentGlasses = 0;
    private final int GOAL = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_quest);

        // UI Initialization
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Water Quest");
        }

        viewWaterLevel = findViewById(R.id.viewWaterLevel);
        waterContainer = findViewById(R.id.waterContainer);
        tvWaterCount = findViewById(R.id.tvWaterCount);
        tvGoal = findViewById(R.id.tvGoal);
        Button btnDrink = findViewById(R.id.btnDrinkWater);
        Button btnUndo = findViewById(R.id.btnUndo);

        // Load Data
        prefs = getSharedPreferences("wellness", MODE_PRIVATE);
        checkDailyReset();
        currentGlasses = prefs.getInt("water_today", 0);

        // 1. DYNAMIC HEIGHT CALCULATION
        // We must wait for the layout to be drawn to get the actual height in pixels
        waterContainer.post(() -> {
            maxContainerHeight = waterContainer.getHeight();
            updateWaterLevel(currentGlasses, false); // No animation on initial load
        });

        updateUIText();

        // 2. LISTENERS
        btnDrink.setOnClickListener(v -> {
            if (currentGlasses < GOAL) {
                currentGlasses++;
                saveProgress();
                updateUIText();
                updateWaterLevel(currentGlasses, true); // Animate

                if (currentGlasses == GOAL) {
                    triggerCelebration();
                }
            } else {
                Toast.makeText(this, "You are fully hydrated!", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. UNDO FEATURE
        btnUndo.setOnClickListener(v -> {
            if (currentGlasses > 0) {
                currentGlasses--;
                saveProgress();
                updateUIText();
                updateWaterLevel(currentGlasses, true);
            }
        });
    }

    private void checkDailyReset() {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String lastDay = prefs.getString("last_day", "");
        if (!today.equals(lastDay)) {
            prefs.edit().putInt("water_today", 0).putString("last_day", today).apply();
            currentGlasses = 0;
        }
    }

    private void saveProgress() {
        prefs.edit().putInt("water_today", currentGlasses).apply();
    }

    private void updateUIText() {
        tvWaterCount.setText(currentGlasses + " / " + GOAL);
        if (currentGlasses >= GOAL) {
            tvGoal.setText("Daily Goal Completed! \uD83C\uDF89"); // Party popper emoji
            tvGoal.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvGoal.setText("Stay hydrated!");
            tvGoal.setTextColor(getResources().getColor(R.color.blue_500)); // Use your app color
        }
    }

    private void updateWaterLevel(int glasses, boolean animate) {
        if (maxContainerHeight == 0) return; // Safety check

        // Calculate target height based on percentage of container
        float percentage = (float) glasses / GOAL;
        int targetHeight = (int) (maxContainerHeight * percentage);

        if (animate) {
            // Use current height as start point for smoothness
            int currentHeight = viewWaterLevel.getHeight();

            ValueAnimator anim = ValueAnimator.ofInt(currentHeight, targetHeight);
            anim.setDuration(800); // Faster, more responsive
            // Overshoot makes it look like liquid splashing up
            anim.setInterpolator(new OvershootInterpolator());

            anim.addUpdateListener(a -> {
                ViewGroup.LayoutParams params = viewWaterLevel.getLayoutParams();
                params.height = (Integer) a.getAnimatedValue();
                viewWaterLevel.setLayoutParams(params);
            });
            anim.start();
        } else {
            // Instant update (for onCreate)
            ViewGroup.LayoutParams params = viewWaterLevel.getLayoutParams();
            params.height = targetHeight;
            viewWaterLevel.setLayoutParams(params);
        }
    }

    private void triggerCelebration() {
        showConfetti();
        Toast.makeText(this, "Hydration Goal Reached!", Toast.LENGTH_LONG).show();
    }

    private void showConfetti() {
        LinearLayout container = findViewById(R.id.confettiContainer);
        container.removeAllViews(); // Clear previous runs
        container.setVisibility(View.VISIBLE);

        // Create 40 particles
        for (int i = 0; i < 40; i++) {
            ImageView confetti = new ImageView(this);
            // Ensure you have a circle or square drawable
            confetti.setImageResource(R.drawable.ic_confetti_dot);

            // Random Size
            int size = 20 + (int)(Math.random() * 20);
            confetti.setLayoutParams(new ViewGroup.LayoutParams(size, size));

            // Random Color (Visual Polish)
            int[] colors = {0xFF42A5F5, 0xFF66BB6A, 0xFFFFA726, 0xFFEF5350};
            confetti.setColorFilter(colors[(int)(Math.random() * colors.length)]);

            // Random Position
            container.addView(confetti);
            confetti.setX((float) (Math.random() * container.getWidth()));
            confetti.setY(-100);

            // Animation
            ObjectAnimator fall = ObjectAnimator.ofFloat(confetti, "translationY", -100, container.getHeight() + 100);
            ObjectAnimator rotate = ObjectAnimator.ofFloat(confetti, "rotation", 0, 360 * (float)Math.random());

            AnimatorSet set = new AnimatorSet();
            set.playTogether(fall, rotate);
            set.setDuration(2000 + (int) (Math.random() * 2000));
            set.start();
        }

        // Hide container after animation
        container.postDelayed(() -> container.setVisibility(View.GONE), 4500);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}