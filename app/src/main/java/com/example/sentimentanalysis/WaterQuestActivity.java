package com.example.sentimentanalysis;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
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
    private TextView tvWaterCount;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_quest);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewWaterLevel = findViewById(R.id.viewWaterLevel);
        tvWaterCount = findViewById(R.id.tvWaterCount);
        Button btnDrinkWater = findViewById(R.id.btnDrinkWater);
        TextView tvGoal = findViewById(R.id.tvGoal);

        prefs = getSharedPreferences("wellness", MODE_PRIVATE);
        final int[] waterCount = {prefs.getInt("water_today", 0)};

        // Daily reset
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String lastDay = prefs.getString("last_day", "");
        if (!today.equals(lastDay)) {
            waterCount[0] = 0;
            prefs.edit().putInt("water_today", 0).putString("last_day", today).apply();
        }

        tvWaterCount.setText(waterCount[0] + " / 8");
        tvGoal.setText(waterCount[0] >= 8 ? "Goal Completed!" : "Keep going!");
        updateWaterLevel(waterCount[0]);

        btnDrinkWater.setOnClickListener(v -> {
            if (waterCount[0] < 8) {
                waterCount[0]++;
                prefs.edit().putInt("water_today", waterCount[0]).apply();
                tvWaterCount.setText(waterCount[0] + " / 8");
                tvGoal.setText(waterCount[0] >= 8 ? "Goal Completed!" : "Great job!");

                animateWaterFill(waterCount[0]);

                if (waterCount[0] == 8) {
                    showConfetti();
                    Toast.makeText(this, "8 glasses completed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateWaterLevel(int count) {
        viewWaterLevel.getLayoutParams().height = (int) (380 * (count / 8.0f));
        viewWaterLevel.requestLayout();
    }

    private void animateWaterFill(int count) {
        int targetHeight = (int) (380 * (count / 8.0f));
        ValueAnimator anim = ValueAnimator.ofInt(viewWaterLevel.getHeight(), targetHeight);
        anim.setDuration(1600);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(a -> {
            viewWaterLevel.getLayoutParams().height = (Integer) a.getAnimatedValue();
            viewWaterLevel.requestLayout();
        });
        anim.start();
    }

    private void showConfetti() {
        // Simple confetti using multiple ImageViews
        LinearLayout container = findViewById(R.id.confettiContainer);
        container.setVisibility(View.VISIBLE);

        for (int i = 0; i < 30; i++) {
            ImageView confetti = new ImageView(this);
            confetti.setImageResource(R.drawable.ic_confetti);
            confetti.setLayoutParams(new ViewGroup.LayoutParams(30, 30));

            int x = (int) (Math.random() * 1000);
            int duration = 2000 + (int) (Math.random() * 1000);

            container.addView(confetti);
            confetti.setX(x);
            confetti.setY(-100);

            ObjectAnimator fall = ObjectAnimator.ofFloat(confetti, "translationY", -100, 1500);
            ObjectAnimator rotate = ObjectAnimator.ofFloat(confetti, "rotation", 0, 360);
            ObjectAnimator fade = ObjectAnimator.ofFloat(confetti, "alpha", 1f, 0f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(fall, rotate, fade);
            set.setDuration(duration);
            set.start();

            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    container.removeView(confetti);
                    if (container.getChildCount() == 0) {
                        container.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}