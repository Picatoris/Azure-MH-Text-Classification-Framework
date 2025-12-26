package com.example.sentimentanalysis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class UserDashboardActivity extends BaseActivity {

    // ==================== UI VIEWS ====================
    private DrawerLayout drawerLayout;
    private LinearLayout weekContainer;
    private TextView weekRangeText;
    private TextView quoteText, quoteAuthor;
    private Calendar currentWeekStart;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE\ndd", Locale.getDefault());
    private final SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat weekHeaderFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
    private HorizontalScrollView weekScrollView;

    // Quote System
    private RequestQueue requestQueue;
    private final Handler quoteHandler = new Handler(Looper.getMainLooper());
    private Runnable quoteRunnable;
    private final long QUOTE_INTERVAL = 18000; // 18 seconds

    // Notes System
    private SharedPreferences notesPrefs;
    private JSONObject notesJson = new JSONObject();

    // Fallback quotes
    private final String[] fallbackQuotes = {
            "The best time to start was yesterday. The next best time is now.",
            "Small steps every day lead to big results.",
            "Your mind will believe everything you tell it. Tell it: I am strong. I am capable. I am enough.",
            "Breathe. Let go. And remind yourself that this very moment is the only one you know you have for sure.",
            "Every day may not be good, but there is something good in every day.",
            "You are stronger than you think. Keep going."
    };
    private final String[] fallbackAuthors = {"Unknown", "Unknown", "Unknown", "Oprah Winfrey", "Alice Morse Earle", "Unknown"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize
        requestQueue = Volley.newRequestQueue(this);
        notesPrefs = getSharedPreferences("DailyNotes", MODE_PRIVATE);
        loadNotesFromStorage();

        // Schedule water reminders
        WaterReminderScheduler.scheduleWaterReminders(this);

        // ==================== FIND VIEWS ====================
        TextView greetingText = findViewById(R.id.greetingText);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ImageView btnLanguage = findViewById(R.id.btnLanguage);
        ImageView sideNavButton = findViewById(R.id.sideNavButton);

        quoteText = findViewById(R.id.quoteText);
        quoteAuthor = findViewById(R.id.quoteAuthor);

        weekContainer = findViewById(R.id.weekContainer);
        weekRangeText = findViewById(R.id.weekRangeText);
        TextView selectedDateEvents = findViewById(R.id.selectedDateEvents);
        weekScrollView = findViewById(R.id.weekScrollView);
        Button btnPrevWeek = findViewById(R.id.btnPrevWeek);
        Button btnNextWeek = findViewById(R.id.btnNextWeek);

        CardView cardBreathingQuest = findViewById(R.id.cardBreathingQuest);
        Button btnBreathingQuest = findViewById(R.id.btnBreathingQuest);
        CardView cardStepQuest = findViewById(R.id.cardStepQuest);
        Button btnStepQuest = findViewById(R.id.btnStepQuest);

        // Preview cards
        CardView cardWaterQuest = findViewById(R.id.cardWaterQuest);
        CardView cardGratitudeGarden = findViewById(R.id.cardGratitudeGarden);
        TextView tvWaterPreview = findViewById(R.id.tvWaterPreview);
        TextView tvGratitudePreview = findViewById(R.id.tvGratitudePreview);

        String username = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String regNo = getIntent().getStringExtra("regNo");

        // ==================== GREETING & NAV ====================
        LocaleHelper.translate("Hello, " + (username != null ? username : "User"),
                greetingText::setText);

        View header = navigationView.getHeaderView(0);
        TextView navHeaderUsername = header.findViewById(R.id.nav_header_username);
        if (navHeaderUsername != null) {
            LocaleHelper.translate(username != null ? username : "User",
                    navHeaderUsername::setText);
        }

        // ==================== CALENDAR ====================
        currentWeekStart = Calendar.getInstance();
        currentWeekStart.set(Calendar.DAY_OF_WEEK, currentWeekStart.getFirstDayOfWeek());
        displayCurrentWeek();

        // Cards
        cardBreathingQuest.setOnClickListener(v -> openActivity(BreathingGameActivity.class));
        btnBreathingQuest.setOnClickListener(v -> openActivity(BreathingGameActivity.class));
        cardStepQuest.setOnClickListener(v -> openActivity(StepCounterGameActivity.class));
        btnStepQuest.setOnClickListener(v -> openActivity(StepCounterGameActivity.class));

        // Week Navigation
        LocaleHelper.translate("Previous", btnPrevWeek::setText);
        LocaleHelper.translate("Next", btnNextWeek::setText);
        btnPrevWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
            displayCurrentWeek();
        });
        btnNextWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
            displayCurrentWeek();
        });

        // UI
        sideNavButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btnLanguage.setOnClickListener(v -> showLanguageDialog());

        // ==================== WATER & GRATITUDE PREVIEWS ====================
        SharedPreferences prefs = getSharedPreferences("wellness", MODE_PRIVATE);
        int waterCount = prefs.getInt("water_today", 0);
        int gratitudeCount = prefs.getInt("gratitude_total", 0);

        // Daily reset
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String lastDay = prefs.getString("last_day", "");
        if (!today.equals(lastDay)) {
            waterCount = 0;
            prefs.edit().putInt("water_today", 0).putString("last_day", today).apply();
        }

        if (tvWaterPreview != null) {
            tvWaterPreview.setText(waterCount + " / 8");
        }
        if (tvGratitudePreview != null) {
            tvGratitudePreview.setText(gratitudeCount + " flowers");
        }

        // Open full activities
        cardWaterQuest.setOnClickListener(v -> startActivity(new Intent(this, WaterQuestActivity.class)));
        cardGratitudeGarden.setOnClickListener(v -> startActivity(new Intent(this, GratitudeGardenActivity.class)));

        // ==================== QUOTE & OTHER FEATURES ====================
        startQuoteRotation();

        // ==================== NAVIGATION ====================
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_guide) startActivity(new Intent(this, GuideActivity.class));
            else if (id == R.id.nav_chat) startActivity(new Intent(this, ChatActivity.class));
            else if (id == R.id.nav_helpline) startActivity(new Intent(this, HelplineActivity.class));
            else if (id == R.id.nav_book) startActivity(new Intent(this, BookConsultationActivity.class));
            else if (id == R.id.nav_mood_check) {
                Intent i = new Intent(this, SentimentAnalysisActivity.class);
                i.putExtra("email", email); i.putExtra("regNo", regNo); startActivity(i);
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                getSharedPreferences("loginPrefs", MODE_PRIVATE).edit().clear().apply();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, UserLoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    // ==================== QUOTE SYSTEM (FIXED) ====================
    private void startQuoteRotation() {
        quoteRunnable = new Runnable() {
            @Override
            public void run() {
                fetchQuote();
                quoteHandler.postDelayed(this, QUOTE_INTERVAL);
            }
        };
        quoteHandler.post(quoteRunnable);
    }

    private void fetchQuote() {
        String url = "https://api.quotable.io/random?maxLength=120";
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try { updateQuote(response.getString("content"), response.getString("author")); }
                    catch (JSONException e) { showRandomFallbackQuote(); }
                },
                error -> showRandomFallbackQuote()
        );
        requestQueue.add(req);
    }

    private void showRandomFallbackQuote() {
        Random r = new Random();
        int i = r.nextInt(fallbackQuotes.length);
        updateQuote(fallbackQuotes[i], fallbackAuthors[i]);
    }

    private void updateQuote(String quote, String author) {
        quoteText.setAlpha(0f);
        quoteAuthor.setAlpha(0f);
        LocaleHelper.translate(quote, tq -> LocaleHelper.translate("- " + author, ta -> {
            quoteText.setText("\"" + tq + "\"");
            quoteAuthor.setText(ta);
            quoteText.animate().alpha(1f).setDuration(800).start();
            quoteAuthor.animate().alpha(1f).setDuration(1000).start();
        }));
    }

    // ==================== CALENDAR + NOTES ====================
    private void displayCurrentWeek() {
        weekContainer.removeAllViews();
        Calendar tempCal = (Calendar) currentWeekStart.clone();
        Calendar endWeek = (Calendar) tempCal.clone();
        endWeek.add(Calendar.DAY_OF_MONTH, 6);

        String label = "Week of " + weekHeaderFormat.format(tempCal.getTime()) + " - " + weekHeaderFormat.format(endWeek.getTime());
        LocaleHelper.translate(label, t -> weekRangeText.setText(t));

        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            boolean isToday = isSameDay(tempCal, today);
            String dateKey = fullDateFormat.format(tempCal.getTime());

            LinearLayout dayLayout = new LinearLayout(this);
            dayLayout.setOrientation(LinearLayout.VERTICAL);
            dayLayout.setGravity(Gravity.CENTER);

            TextView dayView = new TextView(this);
            dayView.setText(dateFormat.format(tempCal.getTime()));
            dayView.setGravity(Gravity.CENTER);
            dayView.setTextSize(15);
            dayView.setTypeface(null, Typeface.BOLD);
            dayView.setTextColor(isToday ? Color.WHITE : Color.parseColor("#1E293B"));
            dayView.setPadding(42, 42, 42, 42);
            dayView.setBackgroundResource(isToday ? R.drawable.day_card_selected : R.drawable.day_card_normal);
            dayView.setElevation(isToday ? 20 : 10);
            dayView.setTag(dateKey);

            View dot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(14, 14);
            dotParams.topMargin = 12;
            dot.setLayoutParams(dotParams);
            dot.setBackgroundResource(R.drawable.ic_note_dot);
            dot.setVisibility(notesJson.has(dateKey) && !notesJson.optString(dateKey, "").isEmpty() ? View.VISIBLE : View.INVISIBLE);

            dayLayout.addView(dayView);
            dayLayout.addView(dot);
            dayLayout.setOnClickListener(v -> showNoteDialog(dateKey));
            dayLayout.setClickable(true);
            dayLayout.setForeground(ContextCompat.getDrawable(this, android.R.drawable.dialog_holo_light_frame));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(24, 0, 24, 0);
            dayLayout.setLayoutParams(params);

            weekContainer.addView(dayLayout);
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        weekScrollView.post(() -> {
            int idx = today.get(Calendar.DAY_OF_WEEK) - currentWeekStart.getFirstDayOfWeek();
            if (idx >= 0 && idx < 7) {
                View v = weekContainer.getChildAt(idx);
                if (v != null) {
                    int x = v.getLeft() - (weekScrollView.getWidth() / 2) + (v.getWidth() / 2);
                    weekScrollView.smoothScrollTo(x, 0);
                }
            }
        });
    }

    private void showNoteDialog(String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_note, null);
        builder.setView(view);

        TextInputEditText input = view.findViewById(R.id.inputNote);
        TextView title = view.findViewById(R.id.dialogTitle);
        LocaleHelper.translate("Note for " + date, title::setText);

        String existing = notesJson.optString(date, "");
        if (!existing.isEmpty()) input.setText(existing);

        builder.setPositiveButton("Save", (d, w) -> {
            String note = Objects.requireNonNull(input.getText()).toString().trim();
            try {
                if (note.isEmpty()) notesJson.remove(date);
                else notesJson.put(date, note);
                saveNotesToStorage();
                displayCurrentWeek();
            } catch (JSONException e) { e.printStackTrace(); }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveNotesToStorage() {
        notesPrefs.edit().putString("notes", notesJson.toString()).apply();
    }

    private void loadNotesFromStorage() {
        String json = notesPrefs.getString("notes", "{}");
        try { notesJson = new JSONObject(json); }
        catch (JSONException e) { notesJson = new JSONObject(); }
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    // ==================== UTILS ====================
    private void openActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showLanguageDialog() {
        String[] langs = {"English", "हिंदी", "தமிழ்", "తెలుగు", "ಕನ್ನಡ", "മലയാളം", "मराठी", "ગુજરાતી"};
        String[] codes = {"en", "hi", "ta", "te", "kn", "ml", "mr", "gu"};
        new AlertDialog.Builder(this)
                .setTitle("Choose Language")
                .setItems(langs, (d, w) -> { LocaleHelper.setLocale(this, codes[w]); recreate(); })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (quoteHandler != null && quoteRunnable != null) {
            quoteHandler.removeCallbacks(quoteRunnable);
        }
    }
}