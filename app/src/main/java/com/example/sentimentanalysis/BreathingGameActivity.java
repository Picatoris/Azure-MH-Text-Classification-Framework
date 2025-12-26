package com.example.sentimentanalysis;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class BreathingGameActivity extends AppCompatActivity {

    private View breathingCircle;
    private ProgressBar progressRing;
    private KonfettiView konfettiView;
    private TextView instructionText, scoreText, streakText, sessionText;
    private Button btnBack;

    private MediaPlayer waveSound, victorySound;
    private Vibrator vibrator;
    private SharedPreferences prefs;

    private int score = 0;
    private int bestScore = 0;
    private int streak = 0;
    private int sessionsToday = 0;
    private CountDownTimer cycleTimer;

    private final long INHALE = 4000;
    private final long HOLD = 4000;
    private final long EXHALE = 6000;
    private final long CYCLE = INHALE + HOLD + EXHALE;
    private final int MAX_CYCLES = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_game);

        initViews();
        initSounds();
        loadData();
        updateStreakAndSessions();
        startBreathingSession();
    }

    private void initViews() {
        breathingCircle = findViewById(R.id.breathingCircle);
        progressRing = findViewById(R.id.progressRing);
        konfettiView = findViewById(R.id.konfettiView);
        instructionText = findViewById(R.id.instructionText);
        scoreText = findViewById(R.id.scoreText);
        streakText = findViewById(R.id.streakText);
        sessionText = findViewById(R.id.sessionText);
        btnBack = findViewById(R.id.btnBack);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        prefs = getSharedPreferences("BreathingQuest", MODE_PRIVATE);

        btnBack.setOnClickListener(v -> finish());
    }

    private void initSounds() {
        if (waveSound != null) {
            waveSound.setLooping(true);
            waveSound.start();
        }
    }

    private void loadData() {
        bestScore = prefs.getInt("bestScore", 0);
        streak = prefs.getInt("streak", 0);
        String lastDate = prefs.getString("lastDate", "");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!today.equals(lastDate)) {
            sessionsToday = 0;
        } else {
            sessionsToday = prefs.getInt("sessionsToday", 0);
        }
    }

    private void updateStreakAndSessions() {
        streakText.setText("Streak: " + streak + " days");
        sessionText.setText("Sessions today: " + (sessionsToday + 1) + "/5");
        scoreText.setText("Score: 0/" + MAX_CYCLES);
    }

    private void startBreathingSession() {
        score = 0;
        scoreText.setText("Score: 0/" + MAX_CYCLES);
        instructionText.setText("Get Ready...");
        progressRing.setProgress(0);

        new CountDownTimer(3000, 1000) {
            public void onTick(long m) { instructionText.setText("Starting in " + (m/1000 + 1)); }
            public void onFinish() {
                startBreathingCycle();
            }
        }.start();
    }

    private void startBreathingCycle() {
        if (score >= MAX_CYCLES) {
            endSession(true);
            return;
        }

        // Inhale
        animateBreathing(1.0f, 1.8f, INHALE, "Breathe In", 0xFF64B5F6, 0xFF9575CD, () -> {
            vibrate();
            // Hold
            animateBreathing(1.8f, 1.8f, HOLD, "Hold", 0xFF9575CD, 0xFF4FC3F7, () -> {
                // Exhale
                animateBreathing(1.8f, 0.9f, EXHALE, "Breathe Out", 0xFF4FC3F7, 0xFF64B5F6, () -> {
                    vibrate();
                    score++;
                    scoreText.setText("Score: " + score + "/" + MAX_CYCLES);
                    progressRing.setProgress((score * 100) / MAX_CYCLES);

                    if (score < MAX_CYCLES) {
                        startBreathingCycle();
                    } else {
                        endSession(true);
                    }
                });
            });
        });
    }

    private void animateBreathing(float from, float to, long duration, String text, int colorStart, int colorEnd, Runnable onEnd) {
        instructionText.setText(text);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(breathingCircle, "scaleX", from, to);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(breathingCircle, "scaleY", from, to);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        ValueAnimator colorAnim = ValueAnimator.ofArgb(colorStart, colorEnd);
        colorAnim.addUpdateListener(anim -> {
            GradientDrawable bg = (GradientDrawable) breathingCircle.getBackground();
            bg.setColor((int) anim.getAnimatedValue());
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, colorAnim);
        set.setDuration(duration);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onEnd.run();
            }
        });
        set.start();
    }

    private void vibrate() {
        if (vibrator != null) vibrator.vibrate(50);
    }

    private void endSession(boolean perfect) {
        if (waveSound != null) waveSound.pause();

        sessionsToday++;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("sessionsToday", sessionsToday);
        editor.putString("lastDate", today);
        if (score > bestScore) {
            bestScore = score;
            editor.putInt("bestScore", bestScore);
        }
        if (sessionsToday >= 3 && streak < 100) {
            streak++;
            editor.putInt("streak", streak);
        }
        editor.apply();

        String msg = perfect ?
                "PERFECT SESSION! 10/10\nBest Score: " + bestScore + "\nStreak: " + streak + " days" :
                "Great session! Score: " + score + "/10\nBest: " + bestScore;

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        if (perfect) {
            triggerVictory();
        }

        btnBack.setText("Amazing! Back");
    }

    private void triggerVictory() {
        if (victorySound != null) victorySound.start();

        konfettiView.build()
                .addColors(0xFF64B5F6, 0xFF9575CD, 0xFF4FC3F7, 0xFFFFD700)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 6f)
                .setFadeOutEnabled(true)
                .setTimeToLive(4000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 6))
                .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                .streamFor(300, 5000L);
    }

    @Override
    protected void onDestroy() {
        if (waveSound != null) { waveSound.stop(); waveSound.release(); }
        if (victorySound != null) victorySound.release();
        if (cycleTimer != null) cycleTimer.cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (waveSound != null) waveSound.pause();
        super.onBackPressed();
    }
}