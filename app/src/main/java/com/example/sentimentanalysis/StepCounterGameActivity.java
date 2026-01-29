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
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class StepCounterGameActivity extends AppCompatActivity implements SensorEventListener {

    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001;
    private static final String TAG = "StepCounter";

    // SharedPreferences
    private static final String PREFS_NAME = "StepQuestPrefs";
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
    private float sensorBaseline = -1f;
    private int currentStepsToday = 0;
    private int streak = 0;
    private final int dailyGoal = 5000;
    private int initialSavedSteps = 0; // Steps loaded from prefs at start

    // Challenge
    private boolean isChallengeActive = false;
    private int challengeStartSteps = 0;
    private final Handler handler = new Handler();

    // Accelerometer Fallback
    private static final float STEP_THRESHOLD = 11.0f; // Adjusted for raw acceleration (Earth gravity is ~9.8)
    private static final long MIN_STEP_INTERVAL_MS = 380;
    private long lastStepTime = 0;
    private float lastY = 0;
    private boolean isStepUp = false;
    // Add this to your variable declarations at the top
    private android.os.CountDownTimer challengeTimer;
    // Firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference leaderboardRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter_game);

        initViews();
        initFirebase();
        loadSavedData();

        // 1. Check Permission immediately
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

        // Safety check for buttons (in case layout ID mismatches)
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
        // Load whatever we had saved
        initialSavedSteps = prefs.getInt(KEY_TODAY_STEPS, 0);
        currentStepsToday = initialSavedSteps;
        streak = prefs.getInt(KEY_STREAK, 0);
        String lastDate = prefs.getString(KEY_LAST_DATE, "");

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Check if it's a new day
        if (!today.equals(lastDate)) {
            // New day logic
            if (currentStepsToday >= dailyGoal) streak++;
            else streak = 0; // Reset streak if missed yesterday

            // Reset steps for the new day
            currentStepsToday = 0;
            initialSavedSteps = 0;

            saveData(); // Save the reset
        }

        sensorBaseline = -1f; // Reset baseline so we can re-calibrate
        updateUI();
    }

    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_TODAY_STEPS, currentStepsToday);
        editor.putInt(KEY_STREAK, streak);
        editor.putString(KEY_LAST_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

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
        if (event.sensor == null) return;

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // Value is TOTAL steps since reboot
            float rawSensorValue = event.values[0];

            if (sensorBaseline == -1f) {
                // First time we hear from sensor in this session, set baseline
                sensorBaseline = rawSensorValue;
            }

            // Steps walked JUST in this session (since app opened)
            int stepsInSession = (int) (rawSensorValue - sensorBaseline);

            // Total = Saved Steps from before + New Steps
            currentStepsToday = initialSavedSteps + stepsInSession;

            Log.d(TAG, "Step Event: " + currentStepsToday);
            vibrateOnStep();
        }
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Fallback logic for old phones
            float y = event.values[1]; // Y-axis
            float z = event.values[2]; // Z-axis

            // Simple magnitude calculation
            double magnitude = Math.sqrt(y*y + z*z);
            long now = System.currentTimeMillis();

            // Detect peak
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
        // Don't vibrate too often (max once per second) to save battery/annoyance
        if (now - lastVibrateTime > 1000) {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(30);
                lastVibrateTime = now;
            }
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void updateUI() {
        stepsText.setText(String.valueOf(currentStepsToday)); // Ensure it's a string
        streakText.setText("Streak: " + streak + " days");

        int progress = 0;
        if(dailyGoal > 0) {
            progress = Math.min(100, (currentStepsToday * 100) / dailyGoal);
        }
        progressBar.setProgress(progress);

        if (firebaseUser != null && leaderboardRef != null) {
            String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Player";
            leaderboardRef.child(firebaseUser.getUid())
                    .setValue(new LeaderboardEntry(name, currentStepsToday, System.currentTimeMillis()));
        }

        if (currentStepsToday >= dailyGoal) {
            triggerVictory();
        }
    }

    private void triggerVictory() {
        // Only trigger if we haven't already celebrated today (optional logic to add)
        // For now, checks sound is not playing
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

        achievementText.setText("GOAL SMASHED!");
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
                // 1. KEEP SCREEN ON (So the phone doesn't sleep during the game)
                getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                isChallengeActive = true;
                challengeStartSteps = currentStepsToday;
                challengeText.setText("GO! 100 steps in 60s");

                // 2. Start the game timer (Save it to the variable 'challengeTimer')
                challengeTimer = new android.os.CountDownTimer(60000, 1000) {
                    public void onTick(long m) {
                        int elapsed = currentStepsToday - challengeStartSteps;
                        challengeText.setText("Challenge: " + elapsed + "/100");
                    }
                    public void onFinish() {
                        // REMOVE SCREEN ON FLAG (Let phone sleep normally again)
                        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        int completed = currentStepsToday - challengeStartSteps;
                        if (completed >= 100) {
                            currentStepsToday += 200;
                            initialSavedSteps += 200;
                            showBonus("CHALLENGE WIN +200 BONUS!");
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
        // [Same Chart Logic as before - removed for brevity as it works fine]
        // You can paste your existing chart code here.
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
            // Mock data if empty
            entries.add(new BarEntry(0, 100));
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
        if (leaderboardRef == null) {
            Toast.makeText(this, "Log in to see leaderboard", Toast.LENGTH_SHORT).show();
            return;
        }
        leaderboardRef.orderByChild("steps").limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> list = new ArrayList<>();
                        int rank = 1;
                        // Reverse needed because Firebase returns ascending
                        ArrayList<DataSnapshot> children = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) children.add(child);

                        for (int i = children.size() - 1; i >= 0; i--) {
                            LeaderboardEntry e = children.get(i).getValue(LeaderboardEntry.class);
                            if (e != null) {
                                list.add(rank + ". " + e.name + " : " + e.steps);
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
                        "First 5K" + (currentStepsToday >= 5000 ? " ✅" : " 🔒"),
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

    @Override
    protected void onResume() {
        super.onResume();
        // SAFEGUARD: Only register if sensorManager is ready and we have permission
        boolean hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;

        if (hasPermission && sensorManager != null && stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 1. Stop the Challenge Timer if the user leaves the app
        if (challengeTimer != null) {
            challengeTimer.cancel();
            isChallengeActive = false;
            // Remove the "Keep Screen On" flag just in case
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // 2. Safe Sensor Unregister (from previous fix)
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