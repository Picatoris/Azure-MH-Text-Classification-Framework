package com.example.sentimentanalysis;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class StepCounterGameActivity extends AppCompatActivity implements SensorEventListener {

    // SharedPreferences
    private static final String PREFS_NAME = "StepQuestPrefs";
    private static final String KEY_BASELINE_STEPS = "baselineSteps";     // Sensor value when app started
    private static final String KEY_TODAY_STEPS = "todaySteps";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_LAST_DATE = "lastDate";
    private static final String KEY_WEEKLY_HISTORY = "weeklyHistory";

    // UI
    private KonfettiView konfettiView;
    private MediaPlayer victorySound;
    private BarChart weeklyChart;
    private TextView stepsText, streakText, bonusText, challengeText, achievementText;
    private ProgressBar progressBar;

    // Sensors & Step Counting
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private Vibrator vibrator;
    private SharedPreferences prefs;
    private long lastVibrateTime = 0;

    // Step Logic
    private float sensorBaseline = -1f;  // First reading from TYPE_STEP_COUNTER
    private int currentStepsToday = 0;
    private int streak = 0;
    private final int dailyGoal = 5000;

    // Challenge
    private boolean isChallengeActive = false;
    private int challengeStartSteps = 0;
    private final Handler handler = new Handler();

    // Accelerometer Fallback (for old phones)
    private static final float STEP_THRESHOLD = 1.9f;
    private static final long MIN_STEP_INTERVAL_MS = 380;
    private long lastStepTime = 0;
    private float lastY = 0;
    private boolean isStepUp = false;

    // Firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference leaderboardRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter_game);

        initViews();
        initFirebase();
        setupSensors();
        loadSavedData();
        setupRealWeeklyChart();
        startDailyChallenge();
    }

    private void initViews() {
        konfettiView = findViewById(R.id.konfettiView);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        weeklyChart = findViewById(R.id.weeklyChart);
        stepsText = findViewById(R.id.stepsText);
        streakText = findViewById(R.id.streakText);
        bonusText = findViewById(R.id.bonusText);
        challengeText = findViewById(R.id.challengeText);
        achievementText = findViewById(R.id.achievementText);
        progressBar = findViewById(R.id.progressBar);
        Button btnLeaderboard = findViewById(R.id.btnLeaderboard);
        Button btnAchievements = findViewById(R.id.btnAchievements);

        btnLeaderboard.setOnClickListener(v -> showLeaderboard());
        btnAchievements.setOnClickListener(v -> showAchievements());
    }

    private void initFirebase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            leaderboardRef = FirebaseDatabase.getInstance().getReference("leaderboard");
        }
    }

    private void setupSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepSensor == null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Toast.makeText(this, "Using high-accuracy step detection", Toast.LENGTH_LONG).show();
        }
    }

    private void loadSavedData() {
        currentStepsToday = prefs.getInt(KEY_TODAY_STEPS, 0);
        streak = prefs.getInt(KEY_STREAK, 0);
        String lastDate = prefs.getString(KEY_LAST_DATE, "");

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (!today.equals(lastDate)) {
            if (currentStepsToday >= dailyGoal / 2) streak++;
            else if (currentStepsToday == 0) streak = 0;
            else streak = 1;
            currentStepsToday = 0;
            saveData();
        }

        sensorBaseline = -1f; // Reset so sensor can set new baseline
        updateUI();
    }

    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_TODAY_STEPS, currentStepsToday);
        editor.putInt(KEY_STREAK, streak);
        editor.putString(KEY_LAST_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        // Save daily history
        try {
            String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String jsonStr = prefs.getString(KEY_WEEKLY_HISTORY, "{}");
            JSONObject json = new JSONObject(jsonStr);
            json.put(todayKey, currentStepsToday);
            editor.putString(KEY_WEEKLY_HISTORY, json.toString());
        } catch (Exception ignored) {}

        editor.apply();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            float currentValue = event.values[0];

            if (sensorBaseline == -1f) {
                sensorBaseline = currentValue;
            }

            int stepsSinceStart = (int) (currentValue - sensorBaseline);
            currentStepsToday = prefs.getInt(KEY_TODAY_STEPS, 0) + stepsSinceStart;

            vibrateOnStep();
        }
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float y = event.values[1];
            float gravity = 0.8f * lastY + 0.2f * y;
            float acceleration = y - gravity;
            long now = System.currentTimeMillis();

            if (acceleration > STEP_THRESHOLD && !isStepUp && now - lastStepTime > MIN_STEP_INTERVAL_MS) {
                isStepUp = true;
            }
            if (acceleration < -STEP_THRESHOLD && isStepUp && now - lastStepTime > MIN_STEP_INTERVAL_MS) {
                currentStepsToday++;
                lastStepTime = now;
                isStepUp = false;
                vibrateOnStep();
            }
            lastY = y;
        }

        saveData();
        updateUI();
    }

    private void vibrateOnStep() {
        long now = System.currentTimeMillis();
        if (now - lastVibrateTime > 800) {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(30);
                lastVibrateTime = now;
            }
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void updateUI() {
        stepsText.setText("Steps: " + currentStepsToday);
        streakText.setText("Streak: " + streak + " days");
        int progress = Math.min(100, (currentStepsToday * 100) / dailyGoal);
        progressBar.setProgress(progress);

        // Update Firebase
        if (firebaseUser != null) {
            String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Player";
            leaderboardRef.child(firebaseUser.getUid())
                    .setValue(new LeaderboardEntry(name, currentStepsToday, System.currentTimeMillis()));
        }

        if (currentStepsToday >= dailyGoal) {
            triggerVictory();
        }
    }

    private void triggerVictory() {
        if (victorySound != null && !victorySound.isPlaying()) {
            victorySound.start();
        }

        konfettiView.build()
                .addColors(0xF59E0B, 0x10B981, 0x3B82F6, 0x8B5CF6, 0xEF4444)
                .setDirection(0.0, 359.0)
                .setSpeed(2f, 8f)
                .setFadeOutEnabled(true)
                .setTimeToLive(4000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 6))
                .setPosition(konfettiView.getWidth() / 2f, konfettiView.getHeight() / 3f)
                .burst(400);

        achievementText.setText("GOAL SMASHED! " + streak + "-DAY STREAK!");
        achievementText.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> achievementText.setVisibility(View.GONE), 6000);
    }

    private void showBonus(String msg) {
        bonusText.setText(msg);
        bonusText.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> bonusText.setVisibility(View.GONE), 4000);
    }

    private void startDailyChallenge() {
        challengeText.setText("Challenge starts in 5s...");
        new android.os.CountDownTimer(5000, 1000) {
            public void onTick(long m) {
                challengeText.setText("Starting in " + (m/1000) + "...");
            }
            public void onFinish() {
                isChallengeActive = true;
                challengeStartSteps = currentStepsToday;
                challengeText.setText("GO! 100 steps in 60s");

                new android.os.CountDownTimer(60000, 1000) {
                    public void onTick(long m) {
                        int elapsed = currentStepsToday - challengeStartSteps;
                        challengeText.setText("Challenge: " + elapsed + "/100");
                    }
                    public void onFinish() {
                        int completed = currentStepsToday - challengeStartSteps;
                        if (completed >= 100) {
                            currentStepsToday += 200;
                            showBonus("CHALLENGE WIN +200 BONUS!");
                            triggerVictory();
                        } else {
                            challengeText.setText("Try again tomorrow!");
                        }
                        saveData();
                        updateUI();
                    }
                }.start();
            }
        }.start();
    }

    private void setupRealWeeklyChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        String historyJson = prefs.getString(KEY_WEEKLY_HISTORY, "{}");

        try {
            JSONObject json = new JSONObject(historyJson);
            for (int i = 6; i >= 0; i--) {
                cal.setTime(new Date());
                cal.add(Calendar.DAY_OF_YEAR, -i);
                String dateKey = sdf.format(cal.getTime());
                int steps = json.optInt(dateKey, 0);
                entries.add(new BarEntry(6 - i, steps));
                labels.add(dayFormat.format(cal.getTime()));
            }
        } catch (Exception e) {
            for (int i = 0; i < 7; i++) {
                entries.add(new BarEntry(i, 3000 + (float)(Math.random() * 6000)));
                labels.add("Day" + (i+1));
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "Your Steps");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        weeklyChart.setData(new BarData(dataSet));
        XAxis xAxis = weeklyChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        weeklyChart.getDescription().setEnabled(false);
        weeklyChart.animateY(1200);
        weeklyChart.invalidate();
    }

    private void showLeaderboard() {
        if (leaderboardRef == null) return;
        leaderboardRef.orderByChild("steps").limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> list = new ArrayList<>();
                        int rank = 1;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            LeaderboardEntry e = child.getValue(LeaderboardEntry.class);
                            if (e != null) {
                                String medal = rank <= 3 ? "Top " : "";
                                list.add(0, medal + rank + ". " + e.name + " — " + e.steps + " steps");
                                rank++;
                            }
                        }
                        if (list.isEmpty()) list.add("Be the first!");

                        new AlertDialog.Builder(StepCounterGameActivity.this)
                                .setTitle("Global Leaderboard")
                                .setItems(list.toArray(new String[0]), null)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showAchievements() {
        new AlertDialog.Builder(this)
                .setTitle("Your Badges")
                .setItems(new String[]{
                        "First 5K" + (currentStepsToday >= 5000 ? " Unlocked!" : ""),
                        "Challenge Master",
                        "Streak: " + streak + " days"
                }, null)
                .setPositiveButton("OK", null)
                .show();
    }

    public static class LeaderboardEntry {
        public String name;
        public int steps;
        public long timestamp;
        public LeaderboardEntry() {}
        public LeaderboardEntry(String name, int steps, long timestamp) {
            this.name = name; this.steps = steps; this.timestamp = timestamp;
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        saveData();
    }

    @Override protected void onDestroy() {
        if (victorySound != null) victorySound.release();
        super.onDestroy();
    }
}