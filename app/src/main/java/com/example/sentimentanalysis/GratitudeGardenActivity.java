package com.example.sentimentanalysis;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GratitudeGardenActivity extends AppCompatActivity {

    private ImageView imgFlower;
    private TextView tvCount;
    private SharedPreferences prefs;
    private int gratitudeCount = 0;

    // --- FIX 1: Add variable to hold the current user ---
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gratitude_garden);

        // --- FIX 2: Retrieve the username passed from UserDashboardActivity ---
        currentUsername = getIntent().getStringExtra("username");
        if (currentUsername == null) {
            currentUsername = "guest";
        }

        // 1. Initialize Views (Matching your XML IDs)
        imgFlower = findViewById(R.id.imgFlower);
        tvCount = findViewById(R.id.tvCount);
        Button btnAdd = findViewById(R.id.btnAddGratitude);
        // Note: tvLastEntry was defined but not used in logic, keeping it safe
        TextView tvLastEntry = findViewById(R.id.tvLastEntry);
        Button btnHistory = findViewById(R.id.btnHistory);

        btnHistory.setOnClickListener(v -> showHistoryDialog());

        // 2. Enable Back Button in Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gratitude Garden");
        }

        // 3. Load Data
        prefs = getSharedPreferences("wellness", MODE_PRIVATE);
        loadGratitudeData();

        // 4. Set Button Listener (Shows Input Dialog)
        btnAdd.setOnClickListener(v -> showAddGratitudeDialog());

        // 5. HIDDEN FEATURE: Long press the flower to see your history!
        imgFlower.setOnLongClickListener(v -> {
            showHistoryDialog();
            return true;
        });
    }

    private void loadGratitudeData() {
        // --- FIX 3: Load data using USER-SPECIFIC key ---
        String key = "gratitude_history_" + currentUsername;

        // We use a Set to store history. Size of set = Number of flowers.
        Set<String> history = prefs.getStringSet(key, new HashSet<>());
        gratitudeCount = history.size();
        updateGardenUI();
    }

    private void showAddGratitudeDialog() {
        // Create the input field programmatically
        final EditText input = new EditText(this);
        input.setHint("e.g., A warm cup of coffee...");

        // Add padding so text isn't stuck to the edge
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Plant a Seed")
                .setMessage("What are you grateful for right now?")
                .setView(input)
                .setPositiveButton("Plant", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) {
                        saveEntry(text);
                    } else {
                        Toast.makeText(this, "Please write something to plant a seed!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveEntry(String text) {
        // --- FIX 4: Save data using USER-SPECIFIC key ---
        String key = "gratitude_history_" + currentUsername;

        // 1. Get existing history (Must create new HashSet to modify safely)
        Set<String> history = new HashSet<>(prefs.getStringSet(key, new HashSet<>()));

        // 2. Create formatted entry with timestamp
        String timestamp = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(new Date());
        String fullEntry = text + "  — " + timestamp;

        // 3. Save to SharedPreferences
        history.add(fullEntry);
        prefs.edit().putStringSet(key, history).apply();

        // 4. Update local count and UI
        gratitudeCount = history.size();
        updateGardenUI();
        animateFlowerGrowth();

        Toast.makeText(this, "Seed planted successfully!", Toast.LENGTH_SHORT).show();
    }

    private void updateGardenUI() {
        tvCount.setText(gratitudeCount + " flowers planted");

        // Reset alpha in case it was dimmed previously
        imgFlower.setAlpha(1.0f);

        // GAMIFICATION LOGIC: Change image based on count
        if (gratitudeCount == 0) {
            // Stage 0: Just a seed waiting to grow
            imgFlower.setImageResource(R.drawable.ic_garden_0_seed);
            // Optional: Dim it slightly to show it's inactive
            imgFlower.setAlpha(0.6f);
        } else if (gratitudeCount < 10) {
            // Stage 1: A small sprout appears (1-9 entries)
            imgFlower.setImageResource(R.drawable.ic_garden_1_sprout);
        } else if (gratitudeCount < 20) {
            // Stage 2: It grows into a bud (10-19 entries)
            imgFlower.setImageResource(R.drawable.ic_garden_2_bud);
        } else {
            // Stage 3: Full Bloom! (20+ entries)
            imgFlower.setImageResource(R.drawable.ic_garden_3_bloom);
        }
    }

    private void animateFlowerGrowth() {
        // Bouncy Animation using OvershootInterpolator
        imgFlower.animate()
                .scaleX(1.3f).scaleY(1.3f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {
                    imgFlower.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(300)
                            .start();
                }).start();
    }

    // Displays the list of saved gratitude notes
    private void showHistoryDialog() {
        // --- FIX 5: Retrieve history using USER-SPECIFIC key ---
        String key = "gratitude_history_" + currentUsername;

        Set<String> historySet = prefs.getStringSet(key, new HashSet<>());

        if (historySet.isEmpty()) {
            Toast.makeText(this, "No memories yet. Plant one!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert Set to List for display
        List<String> historyList = new ArrayList<>(historySet);
        CharSequence[] items = historyList.toArray(new CharSequence[0]);

        new AlertDialog.Builder(this)
                .setTitle("Your Garden Memories")
                .setItems(items, null) // List the items
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}