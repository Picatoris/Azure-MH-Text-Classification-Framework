package com.example.sentimentanalysis;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class StepCounterGameActivity extends AppCompatActivity implements SensorEventListener {

    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001;
    private static final String TAG = "StepCounter";

    // SharedPreferences - FILE NAME remains constant
    private static final String PREFS_NAME = "StepQuestPrefs";

    // --- FIX 1: Add variable for current user ---
    private String currentUsername;

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
    private float sensorBaseline = -1f;
    private int currentStepsToday = 0;
    private int streak = 0;
    private final int dailyGoal = 5000;
    private int initialSavedSteps = 0; // Steps loaded from prefs at start

    // Challenge
    private boolean isChallengeActive = false;
    private int challengeStartSteps = 0;
    private final Handler handler = new Handler();
    private CountDownTimer challengeTimer;

    // Accelerometer Fallback
    private static final float STEP_THRESHOLD = 11.0f;
    private static final long MIN_STEP_INTERVAL_MS = 380;
    private long lastStepTime = 0;
    private boolean isStepUp = false;

    // Firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference leaderboardRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter_game);

        // --- FIX 2: Retrieve the username ---
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null) {
            currentUsername = "guest";
        }

        initViews();
        initFirebase();
        loadSavedData();

        // Check Permission immediately
        checkPermissionsAndSetupSensors();

        setupRealWeeklyChart();
        startDailyChallenge();
    }

    // ---------------- PERMISSION CHECK ----------------
    private void checkPermissionsAndSetupSensors() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
            } else {
                setupSensors();
            }
        } else {
            setupSensors();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                setupSensors();
            } else {
                Toast.makeText(this, "Permission Denied. Step counting will not work.", Toast.LENGTH_LONG).show();
            }
        }
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
        if(btnLeaderboard != null) btnLeaderboard.setOnClickListener(v -> showLeaderboard());

        Button btnAchievements = findViewById(R.id.btnAchievements);
        if(btnAchievements != null) btnAchievements.setOnClickListener(v -> showAchievements());
    }

    private void initFirebase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            leaderboardRef = FirebaseDatabase.getInstance().getReference("leaderboard");
        }
    }

    private void setupSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Try to get the hardware step counter
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepSensor != null) {
            Log.d(TAG, "Sensor Found: TYPE_STEP_COUNTER");
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Log.d(TAG, "Sensor Not Found: Falling back to ACCELEROMETER");
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (stepSensor != null) {
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_GAME);
                Toast.makeText(this, "Using Accelerometer fallback (Less accurate)", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "No sensors found on this device!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadSavedData() {
        // --- FIX 3: Use user-specific keys ---
        String stepsKey = "todaySteps_" + currentUsername;
        String streakKey = "streak_" + currentUsername;
        String dateKey = "lastDate_" + currentUsername;

        initialSavedSteps = prefs.getInt(stepsKey, 0);
        currentStepsToday = initialSavedSteps;
        streak = prefs.getInt(streakKey, 0);
        String lastDate = prefs.getString(dateKey, "");

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!today.equals(lastDate)) {
            // New day logic
            if (currentStepsToday >= dailyGoal) streak++;
            else streak = 0;

            currentStepsToday = 0;
            initialSavedSteps = 0;
            saveData();
        }

        sensorBaseline = -1f;
        updateUI();
    }

    private void saveData() {
        // --- FIX 4: Save using user-specific keys ---
        String stepsKey = "todaySteps_" + currentUsername;
        String streakKey = "streak_" + currentUsername;
        String dateKey = "lastDate_" + currentUsername;
        String historyKey = "weeklyHistory_" + currentUsername;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(stepsKey, currentStepsToday);
        editor.putInt(streakKey, streak);
        editor.putString(dateKey, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        try {
            String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String jsonStr = prefs.getString(historyKey, "{}");
            JSONObject json = new JSONObject(jsonStr);
            json.put(todayKey, currentStepsToday);
            editor.putString(historyKey, json.toString());
        } catch (Exception ignored) {}

        editor.apply();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) return;

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            float rawSensorValue = event.values[0];

            if (sensorBaseline == -1f) {
                sensorBaseline = rawSensorValue;
            }

            int stepsInSession = (int) (rawSensorValue - sensorBaseline);
            currentStepsToday = initialSavedSteps + stepsInSession;

            Log.d(TAG, "Step Event: " + currentStepsToday);
            vibrateOnStep();
        }
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Fallback logic
            float y = event.values[1];
            float z = event.values[2];

            double magnitude = Math.sqrt(y*y + z*z);
            long now = System.currentTimeMillis();

            if (magnitude > STEP_THRESHOLD && !isStepUp && (now - lastStepTime > MIN_STEP_INTERVAL_MS)) {
                isStepUp = true;
            }
            if (magnitude < STEP_THRESHOLD && isStepUp) {
                currentStepsToday++;
                lastStepTime = now;
                isStepUp = false;
                vibrateOnStep();
            }
        }

        saveData();
        updateUI();
    }

    private void vibrateOnStep() {
        long now = System.currentTimeMillis();
        if (now - lastVibrateTime > 1000) {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(30);
                lastVibrateTime = now;
            }
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void updateUI() {
        stepsText.setText(String.valueOf(currentStepsToday));
        streakText.setText("Streak: " + streak + " days");

        int progress = 0;
        if(dailyGoal > 0) {
            progress = Math.min(100, (currentStepsToday * 100) / dailyGoal);
        }
        progressBar.setProgress(progress);

        if (firebaseUser != null && leaderboardRef != null) {
            // Note: Firebase usually handles its own Auth user, but if you want to force
            // the username you passed, you can change this.
            // However, Firebase Auth UID is usually safer for leaderboards.
            // I'll keep this as is, assuming Firebase Auth is correct.
            String name = currentUsername; // Used the local username for consistency
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

        // --- KONFETTI V2 FIXED LOGIC ---

        // 1. Create the Emitter configuration
        EmitterConfig emitterConfig = new Emitter(100L, TimeUnit.MILLISECONDS).max(100);

        // 2. Create the Party
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .spread(360)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(10f, 30f) // Adjusted speed for V2
                        .position(new Position.Relative(0.5, 0.3))
                        .build()
        );

        achievementText.setText("GOAL SMASHED!");
        achievementText.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> achievementText.setVisibility(View.GONE), 6000);
    }

    private void showBonus() {
        bonusText.setText("CHALLENGE WIN +200 BONUS!");
        bonusText.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> bonusText.setVisibility(View.GONE), 4000);
    }

    private void startDailyChallenge() {
        challengeText.setText("Challenge starts in 5s...");
        new CountDownTimer(5000, 1000) {
            public void onTick(long m) {
                challengeText.setText("Starting in " + (m/1000) + "...");
            }
            public void onFinish() {
                // Keep screen on
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                isChallengeActive = true;
                challengeStartSteps = currentStepsToday;
                challengeText.setText("GO! 100 steps in 60s");

                // Start the main game timer
                challengeTimer = new CountDownTimer(60000, 1000) {
                    public void onTick(long m) {
                        int elapsed = currentStepsToday - challengeStartSteps;
                        challengeText.setText("Challenge: " + elapsed + "/100");
                    }
                    public void onFinish() {
                        // Clear screen flag
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        int completed = currentStepsToday - challengeStartSteps;
                        if (completed >= 100) {
                            currentStepsToday += 200;
                            initialSavedSteps += 200;
                            showBonus();
                            triggerVictory();
                        } else {
                            challengeText.setText("Try again tomorrow!");
                        }
                        isChallengeActive = false;
                        saveData();
                        updateUI();
                    }
                }.start();
            }
        }.start();
    }

    private void setupRealWeeklyChart() {
        // --- FIX 5: Load chart data using user-specific key ---
        String historyKey = "weeklyHistory_" + currentUsername;

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        String historyJson = prefs.getString(historyKey, "{}");

        try {
            JSONObject json = new JSONObject(historyJson);
            // Get last 7 days including today
            for (int i = 6; i >= 0; i--) {
                Calendar tempCal = (Calendar) cal.clone();
                tempCal.add(Calendar.DAY_OF_YEAR, -i);
                String dateKey = sdf.format(tempCal.getTime());

                // Get steps or 0 if missing
                int steps = json.optInt(dateKey, 0);

                // If today, use current live steps
                if (i == 0) steps = Math.max(steps, currentStepsToday);

                entries.add(new BarEntry(6 - i, steps));
                labels.add(dayFormat.format(tempCal.getTime()));
            }
        } catch (Exception e) {
            // Error handling - show empty data
            e.printStackTrace();
        }

        if (entries.isEmpty()) {
            entries.add(new BarEntry(0, 0));
            labels.add("Today");
        }

        BarDataSet dataSet = new BarDataSet(entries, "Your Steps");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        weeklyChart.setData(barData);

        XAxis xAxis = weeklyChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        weeklyChart.getDescription().setEnabled(false);
        weeklyChart.getAxisRight().setEnabled(false);
        weeklyChart.animateY(1200);
        weeklyChart.invalidate();
    }

    private void showLeaderboard() {
        if (leaderboardRef == null) {
            Toast.makeText(this, "Log in to see leaderboard", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch top 10 players
        leaderboardRef.orderByChild("steps").limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> displayList = new ArrayList<>();
                        int rank = 1;

                        // Firebase returns ascending order, so we reverse it
                        ArrayList<DataSnapshot> children = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            children.add(child);
                        }
                        Collections.reverse(children);

                        for (DataSnapshot child : children) {
                            LeaderboardEntry e = child.getValue(LeaderboardEntry.class);
                            if (e != null) {
                                displayList.add(rank + ". " + e.name + " : " + e.steps);
                                rank++;
                            }
                        }

                        if (displayList.isEmpty()) displayList.add("Be the first to join!");

                        // Show dialog
                        new AlertDialog.Builder(StepCounterGameActivity.this)
                                .setTitle("Global Leaderboard")
                                .setItems(displayList.toArray(new String[0]), null)
                                .setPositiveButton("Close", null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StepCounterGameActivity.this, "Failed to load", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAchievements() {
        new AlertDialog.Builder(this)
                .setTitle("Your Badges")
                .setItems(new String[]{
                        "First 5K" + (currentStepsToday >= 5000 ? " ✅" : " 🔒"),
                        "Streak: " + streak + " days"
                }, null)
                .setPositiveButton("Cool!", null)
                .show();
    }

    // Static class for Firebase mapping
    public static class LeaderboardEntry {
        public String name;
        public int steps;
        public long timestamp;

        // Empty constructor required for Firebase
        public LeaderboardEntry() {}

        public LeaderboardEntry(String name, int steps, long timestamp) {
            this.name = name;
            this.steps = steps;
            this.timestamp = timestamp;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only register if we have permission
        boolean hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;

        if (hasPermission && sensorManager != null && stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop Timer
        if (challengeTimer != null) {
            challengeTimer.cancel();
            isChallengeActive = false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // Unregister Sensor
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        saveData();
    }

    @Override protected void onDestroy() {
        if (victorySound != null) victorySound.release();
        super.onDestroy();
    }
}