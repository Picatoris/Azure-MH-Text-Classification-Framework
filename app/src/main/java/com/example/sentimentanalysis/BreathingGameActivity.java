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
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class BreathingGameActivity extends AppCompatActivity {

    // Views
    private View breathingCircle;
    private ProgressBar progressRing;
    private KonfettiView konfettiView;
    private TextView instructionText, scoreText, streakText, sessionText;
    private Button btnBack;

    // Utilities
    private MediaPlayer waveSound, victorySound;
    private Vibrator vibrator;
    private SharedPreferences prefs;

    // Game State
    private int score = 0;
    private int bestScore = 0;
    private int streak = 0;
    private int sessionsToday = 0;
    private boolean isRunning = false; // Safety flag to stop animations on exit

    // Constants (Milliseconds)
    private final long INHALE_DURATION = 4000;
    private final long HOLD_DURATION = 2000; // 2 seconds hold is safer for beginners
    private final long EXHALE_DURATION = 4000;
    private final int MAX_CYCLES = 5; // 5 cycles is a good session length

    // --- FIX 1: Add variable to hold the current user ---
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_game);

        // --- FIX 2: Retrieve the username passed from UserDashboardActivity ---
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null) {
            currentUsername = "guest";
        }

        initViews();
        initSounds();
        loadData();
        updateStreakAndSessions();

        // Start the game logic
        prepareSession();
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
        // IMPORTANT: Ensure you have 'waves.mp3' and 'victory.mp3' in res/raw/
        // If you don't have them, comment these lines out to prevent crashing.
        try {
            // waveSound = MediaPlayer.create(this, R.raw.waves);
            // if (waveSound != null) waveSound.setLooping(true);

            // victorySound = MediaPlayer.create(this, R.raw.victory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        // --- FIX 3: Load data using USER-SPECIFIC keys ---
        String bestScoreKey = "bestScore_" + currentUsername;
        String streakKey = "streak_" + currentUsername;
        String lastDateKey = "lastDate_" + currentUsername;
        String sessionsKey = "sessionsToday_" + currentUsername;

        bestScore = prefs.getInt(bestScoreKey, 0);
        streak = prefs.getInt(streakKey, 0);
        String lastDate = prefs.getString(lastDateKey, "");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Reset daily session count if it's a new day
        if (!today.equals(lastDate)) {
            sessionsToday = 0;
            // Optionally update the date now, or wait until session end
        } else {
            sessionsToday = prefs.getInt(sessionsKey, 0);
        }
    }

    private void updateStreakAndSessions() {
        streakText.setText("Streak: " + streak + " days");
        sessionText.setText("Sessions today: " + sessionsToday + "/5");
        scoreText.setText("Score: 0/" + MAX_CYCLES);
    }

    private void prepareSession() {
        isRunning = true;
        score = 0;
        scoreText.setText("Score: 0/" + MAX_CYCLES);
        instructionText.setText("Get Ready...");
        progressRing.setProgress(0);

        // Start background sound
        if (waveSound != null) waveSound.start();

        // 3-second countdown before starting
        new CountDownTimer(3000, 1000) {
            public void onTick(long m) {
                if (!isRunning) cancel();
                instructionText.setText("Starting in " + ((m/1000) + 1));
            }
            public void onFinish() {
                if (isRunning) startBreathingCycle();
            }
        }.start();
    }

    // --- THE CORE LOOP ---
    private void startBreathingCycle() {
        if (!isRunning) return;

        // Check Win Condition
        if (score >= MAX_CYCLES) {
            endSession(true);
            return;
        }

        // STEP 1: INHALE
        // Colors: Blue -> Purple
        animateBreathing(1.0f, 1.6f, INHALE_DURATION, "Breathe In...",
                0xFF64B5F6, 0xFF9575CD, () -> {

                    // STEP 2: HOLD
                    if (!isRunning) return;
                    // Colors: Purple -> Purple (Keep same)
                    animateBreathing(1.6f, 1.6f, HOLD_DURATION, "Hold...",
                            0xFF9575CD, 0xFF9575CD, () -> {

                                // STEP 3: EXHALE
                                if (!isRunning) return;
                                // Colors: Purple -> Blue
                                animateBreathing(1.6f, 1.0f, EXHALE_DURATION, "Breathe Out...",
                                        0xFF9575CD, 0xFF64B5F6, () -> {

                                            // CYCLE COMPLETE
                                            if (!isRunning) return;
                                            score++;
                                            scoreText.setText("Score: " + score + "/" + MAX_CYCLES);
                                            progressRing.setProgress((score * 100) / MAX_CYCLES);
                                            vibrate(); // Gentle buzz on complete

                                            // Loop again
                                            startBreathingCycle();
                                        });
                            });
                });
    }

    private void animateBreathing(float fromScale, float toScale, long duration,
                                  String text, int colorStart, int colorEnd, Runnable onEnd) {
        if (!isRunning) return;

        instructionText.setText(text);

        // Scale Animation (Grow/Shrink)
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(breathingCircle, "scaleX", fromScale, toScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(breathingCircle, "scaleY", fromScale, toScale);

        // Color Animation
        ValueAnimator colorAnim = ValueAnimator.ofArgb(colorStart, colorEnd);
        colorAnim.addUpdateListener(anim -> {
            if (breathingCircle.getBackground() instanceof GradientDrawable) {
                ((GradientDrawable) breathingCircle.getBackground()).setColor((int) anim.getAnimatedValue());
            } else {
                breathingCircle.setBackgroundColor((int) anim.getAnimatedValue());
            }
        });

        // Play animations together
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, colorAnim);
        set.setDuration(duration);
        set.setInterpolator(new AccelerateDecelerateInterpolator()); // Smooth ease-in/out
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isRunning && onEnd != null) onEnd.run();
            }
        });
        set.start();
    }

    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrate for 100ms
            vibrator.vibrate(100);
        }
    }

    private void endSession(boolean success) {
        if (!isRunning) return; // Prevent double calling

        isRunning = false; // Stop loops
        if (waveSound != null && waveSound.isPlaying()) waveSound.pause();

        sessionsToday++;

        // --- FIX 4: Save Data using USER-SPECIFIC keys ---
        String bestScoreKey = "bestScore_" + currentUsername;
        String streakKey = "streak_" + currentUsername;
        String lastDateKey = "lastDate_" + currentUsername;
        String sessionsKey = "sessionsToday_" + currentUsername;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(sessionsKey, sessionsToday);
        editor.putString(lastDateKey, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        if (score > bestScore) {
            bestScore = score;
            editor.putInt(bestScoreKey, bestScore);
        }

        // Increase streak if they did at least 1 session today (simplified logic)
        if (sessionsToday == 1) {
            streak++;
            editor.putInt(streakKey, streak);
        }
        editor.apply();

        instructionText.setText("Session Complete!");
        btnBack.setText("Finish");

        if (success) {
            triggerVictory();
            Toast.makeText(this, "Great Job! You feel calmer now.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- UPDATED KONFETTI V2 CODE ---
    private void triggerVictory() {
        if (victorySound != null) victorySound.start();

        // 1. Create Party using PartyFactory (V2 Syntax)
        Party party = new PartyFactory(new Emitter(100L, TimeUnit.MILLISECONDS).max(100))
                .spread(360)
                .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE))
                .colors(Arrays.asList(0xFF64B5F6, 0xFF9575CD, 0xFF4FC3F7, 0xFFFFD700))
                .setSpeedBetween(0f, 30f)
                .position(new Position.Relative(0.5, 0.3)) // Top-Center ish
                .build();

        konfettiView.start(party);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (waveSound != null && waveSound.isPlaying()) {
            waveSound.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false; // Kill all animation loops

        if (waveSound != null) {
            waveSound.release();
            waveSound = null;
        }
        if (victorySound != null) {
            victorySound.release();
            victorySound = null;
        }
    }
}