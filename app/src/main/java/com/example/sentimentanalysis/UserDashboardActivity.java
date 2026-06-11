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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.util.TypedValueCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;
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
import java.util.concurrent.atomic.AtomicReference;

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

    // Quote System
    private RequestQueue requestQueue;
    private final Handler quoteHandler = new Handler(Looper.getMainLooper());
    private Runnable quoteRunnable;
    private final long QUOTE_INTERVAL = 18000; // 18 seconds

    // Notes System
    private SharedPreferences notesPrefs;
    private JSONObject notesJson = new JSONObject();

    // --- FIX 1: Store the current username globally in this class ---
    private String currentUsername;

    // Fallback quotes
    private final String[] fallbackQuotes = {
            "The best time to start was yesterday. The next best time is now.",
            "It does not matter how slowly you go as long as you do not stop.",
            "Your mind will believe everything you tell it. Tell it: I am strong. I am capable.",
            "Breathe. Let go. And remind yourself that this very moment is the only one you know you have for sure.",
            "Every day may not be good, but there is something good in every day.",
            "You are stronger than you think. Keep going.",
            "The wound is the place where the Light enters you.",
            "Believe you can and you're halfway there.",
            "You may not control all the events that happen to you, but you can decide not to be reduced by them.",
            "Realize deeply that the present moment is all you have.",
            "You are never too old to set another goal or to dream a new dream.",
            "Optimism is the faith that leads to achievement. Nothing can be done without hope and confidence.",
            "Smile, breathe, and go slowly.",
            "It is during our darkest moments that we must focus to see the light.",
            "Life isn't about waiting for the storm to pass, it's about learning to dance in the rain.",
            "What lies behind us and what lies before us are tiny matters compared to what lies within us.",
            "Nature does not hurry, yet everything is accomplished.",
            "When we are no longer able to change a situation, we are challenged to change ourselves.",
            "I took a deep breath and listened to the old brag of my heart. I am, I am, I am.",
            "Happiness is not something ready made. It comes from your own actions.",
            "There is hope, even when your brain tells you there isn't.",
            "Nothing is impossible. The word itself says 'I'm possible!'",
            "You have power over your mind - not outside events. Realize this, and you will find strength.",
            "You are the sky. Everything else – it’s just the weather.",
            "Embrace the glorious mess that you are.",
            "If you're going through hell, keep going.",
            "Courage starts with showing up and letting ourselves be seen.",
            "To love oneself is the beginning of a lifelong romance.",
            "Courage does not always roar. Sometimes courage is the quiet voice at the end of the day saying, 'I will try again tomorrow.'",
            "Peace is the result of retraining your mind to process life as it is, rather than as you think it should be.",
            "The greatest weapon against stress is our ability to choose one thought over another."
    };

    private final String[] fallbackAuthors = {
            "Chinese Proverb",
            "Confucius",
            "Zig Ziglar",
            "Oprah Winfrey",
            "Alice Morse Earle",
            "Joel Osteen",
            "Rumi",
            "Theodore Roosevelt",
            "Maya Angelou",
            "Eckhart Tolle",
            "C.S. Lewis",
            "Helen Keller",
            "Thich Nhat Hanh",
            "Aristotle Onassis",
            "Vivian Greene",
            "Ralph Waldo Emerson",
            "Lao Tzu",
            "Viktor Frankl",
            "Sylvia Plath",
            "Dalai Lama",
            "John Green",
            "Audrey Hepburn",
            "Marcus Aurelius",
            "Pema Chödrön",
            "Elizabeth Gilbert",
            "Winston Churchill",
            "Brené Brown",
            "Oscar Wilde",
            "Mary Anne Radmacher",
            "Wayne Dyer",
            "William James"
    };

    private int dpToPx(float dp) {
        return Math.round(
                TypedValueCompat.dpToPx(dp, getResources().getDisplayMetrics())
        );
    }

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
        Button btnPrevWeek = findViewById(R.id.btnPrevWeek);
        Button btnNextWeek = findViewById(R.id.btnNextWeek);

        CardView cardBreathingQuest = findViewById(R.id.cardBreathingQuest);
        CardView cardStepQuest = findViewById(R.id.cardStepQuest);
        // Preview cards
        CardView cardWaterQuest = findViewById(R.id.cardWaterQuest);
        CardView cardGratitudeGarden = findViewById(R.id.cardGratitudeGarden);
        TextView tvWaterPreview = findViewById(R.id.tvWaterPreview);
        TextView tvGratitudePreview = findViewById(R.id.tvGratitudePreview);

        // --- FIX 2: Capture User Details for Passing ---
        currentUsername = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String regNo = getIntent().getStringExtra("regNo");

        // Safety check if username is null
        if (currentUsername == null) {
            currentUsername = "User";
        }

        // ==================== GREETING & NAV ====================
        String greeting = "Hello, " + currentUsername;
        AtomicReference<String> lang = new AtomicReference<>(LocaleHelper.getSavedLanguage(this));
        MLTranslator.translate(this, greeting, lang.get(), greetingText::setText);

        View header = navigationView.getHeaderView(0);
        TextView navHeaderUsername = header.findViewById(R.id.nav_header_username);
        if (navHeaderUsername != null) {
            String name = currentUsername;
            lang.set(LocaleHelper.getSavedLanguage(this));
            MLTranslator.translate(this, name, lang.get(), navHeaderUsername::setText);
        }

        // ==================== CALENDAR ====================
        currentWeekStart = Calendar.getInstance();
        currentWeekStart.set(Calendar.DAY_OF_WEEK, currentWeekStart.getFirstDayOfWeek());
        displayCurrentWeek();

        // Cards - Updated to use helper that passes username
        cardBreathingQuest.setOnClickListener(v -> openActivity(BreathingGameActivity.class));
        cardStepQuest.setOnClickListener(v -> openActivity(StepCounterGameActivity.class));

        // --- FIX 3: Pass Username to Water and Gratitude ---
        // I changed these to use openActivity() so the username is passed automatically.
        // Make sure WaterQuestActivity and GratitudeGardenActivity are the correct class names.
        cardWaterQuest.setOnClickListener(v -> openActivity(WaterQuestActivity.class));
        cardGratitudeGarden.setOnClickListener(v -> openActivity(GratitudeGardenActivity.class));

        // Week Navigation
        MLTranslator.translate(this, "Previous", lang.get(), btnPrevWeek::setText);
        MLTranslator.translate(this, "Next", lang.get(), btnNextWeek::setText);
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
        // Note: These previews might still show shared data unless you update how SharedPreferences reads here too.
        // But the main goal right now is fixing the GAME progress.
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
                i.putExtra("email", email);
                i.putExtra("regNo", regNo);
                i.putExtra("username", currentUsername); // Added username here too just in case
                startActivity(i);
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                getSharedPreferences("loginPrefs", MODE_PRIVATE).edit().clear().apply();
                lang.set(LocaleHelper.getSavedLanguage(this));

                MLTranslator.translate(this, "Logged out", lang.get(),
                        text -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                );
                startActivity(new Intent(this, UserLoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    // ==================== QUOTE SYSTEM ====================
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
                    try {String quote = response.getString("content");
                        String author = response.getString("author");

                        String lang = LocaleHelper.getSavedLanguage(this);

                        MLTranslator.translate(this, quote, lang, tq ->
                                MLTranslator.translate(this, "- " + author, lang, ta ->
                                        updateQuote(tq, ta)
                                )
                        );
                    }
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
        String lang = LocaleHelper.getSavedLanguage(this);

        MLTranslator.translate(this, quote, lang, tq ->
                MLTranslator.translate(this, "- " + author, lang, ta -> {
                    quoteText.setText("\"" + tq + "\"");
                    quoteAuthor.setText(ta);
                    quoteText.animate().alpha(1f).setDuration(800).start();
                    quoteAuthor.animate().alpha(1f).setDuration(1000).start();
                })
        );
    }

    // ==================== CALENDAR + NOTES ====================
    private void displayCurrentWeek() {
        weekContainer.removeAllViews();

        Calendar tempCal = (Calendar) currentWeekStart.clone();
        Calendar endWeek = (Calendar) tempCal.clone();
        endWeek.add(Calendar.DAY_OF_MONTH, 6);

        String label = "Week of " + weekHeaderFormat.format(tempCal.getTime()) + " – " + weekHeaderFormat.format(endWeek.getTime());
        String lang = LocaleHelper.getSavedLanguage(this);
        MLTranslator.translate(this, label, lang, weekRangeText::setText);
        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            MaterialCardView card = new MaterialCardView(this);

            boolean isToday = isSameDay(tempCal, today);
            String dateKey = fullDateFormat.format(tempCal.getTime());

            card.setRadius(dpToPx(18));
            card.setCardElevation(isToday ? dpToPx(6) : dpToPx(2));
            card.setStrokeWidth(0);
            card.setCardBackgroundColor(
                    ContextCompat.getColor(this,
                            isToday ? R.color.purple_500 : android.R.color.white)
            );

            card.setClickable(true);
            card.setFocusable(true);

            // Inner container
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));
            layout.setPadding(8, 12, 8, 12);

            // Day name (MON, TUE...)
            TextView dayName = new TextView(this);
            dayName.setText(new SimpleDateFormat("EEE", Locale.getDefault())
                    .format(tempCal.getTime()).toUpperCase());
            dayName.setTextSize(11);
            dayName.setTypeface(Typeface.DEFAULT_BOLD);
            dayName.setLetterSpacing(0.08f);
            dayName.setTextColor(isToday ? Color.WHITE : Color.parseColor("#94A3B8"));

            // Date number (22)
            TextView dateNumber = new TextView(this);
            dateNumber.setText(String.valueOf(tempCal.get(Calendar.DAY_OF_MONTH)));
            dateNumber.setTextSize(22);
            dateNumber.setTypeface(Typeface.DEFAULT_BOLD);
            dateNumber.setTextColor(isToday ? Color.WHITE : Color.parseColor("#0F172A"));
            dateNumber.setPadding(0, 4, 0, 0);

            // Assemble
            layout.addView(dayName);
            layout.addView(dateNumber);
            card.addView(layout);

            // Equal width for 7 days
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, dpToPx(96), 1f);
            params.setMargins(6, 0, 6, 0);
            card.setLayoutParams(params);

            // Click → open note
            card.setOnClickListener(v -> showNoteDialog(dateKey));

            weekContainer.addView(card);

            // Move to next day
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void showNoteDialog(String date) {
        // --- NOTE FOR FUTURE FIX: ---
        // Notes are also currently shared across users.
        // To fix notes, you would need to change "date" to "date + "_" + currentUsername" below.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_note, null);
        builder.setView(view);

        TextInputEditText input = view.findViewById(R.id.inputNote);
        TextView title = view.findViewById(R.id.dialogTitle);
        String lang = LocaleHelper.getSavedLanguage(this);
        MLTranslator.translate(this, "Note for " + date, lang, title::setText);

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

    // --- FIX 4: Updated helper to always include username ---
    private void openActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.putExtra("username", currentUsername); // Crucial fix: Passing the ID
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showLanguageDialog() {
        String[] langs = {"English", "हिंदी", "தமிழ்", "తెలుగు", "ಕನ್ನಡ", "മലയാളം", "मराठी", "ગુજરાતી"};
        String[] codes = {"en", "hi", "ta", "te", "kn", "ml", "mr", "gu"};
        new AlertDialog.Builder(this)
                .setTitle("Choose Language")
                .setItems(langs, (d, w) -> {LocaleHelper.setLocale(this, codes[w]);
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent); })
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