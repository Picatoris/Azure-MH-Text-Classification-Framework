package com.example.sentimentanalysis;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserDashboardActivity extends AppCompatActivity {

    private EditText etUserSentence;
    private TextView tvSentimentResult, classification, suggestion, progressText;
    private ProgressBar progressBar;
    private Button btnAnalyzeSentiment, btnConsultTherapy;
    private DatabaseReference databaseReference, newEntryRef;
    private String regNo;

    private TextAnalyticsService textAnalyticsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize UI components
        etUserSentence = findViewById(R.id.etUserSentence);
        tvSentimentResult = findViewById(R.id.tvSentimentResult);
        classification = findViewById(R.id.classification);
        suggestion = findViewById(R.id.suggestion);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        btnAnalyzeSentiment = findViewById(R.id.btnAnalyzeSentiment);
        btnConsultTherapy = findViewById(R.id.btnConsultTherapy);

        // Retrieve data from Intent
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        regNo = intent.getStringExtra("regNo");

        // Initialize Firebase Database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        assert email != null;
        databaseReference = firebaseDatabase.getReference("sentiments")
                .child(email.substring(0, email.indexOf('@')));

        // Initialize Azure Text Analytics Service
        String languageKey = "1dksluHlMYX1PTQvbU4xxj3lF3d52ZkodI6LhL2l6fF4ZyYA1cjgJQQJ99BBACYeBjFXJ3w3AAAaACOGXUUD";
        String languageEndpoint = "https://sentidazzle123.cognitiveservices.azure.com/";
        textAnalyticsService = new TextAnalyticsService(languageKey, languageEndpoint);

        // Button Click Listeners
        btnAnalyzeSentiment.setOnClickListener(view -> analyzeSentiment());
        btnConsultTherapy.setOnClickListener(view -> openTherapyActivity());
    }

    private void analyzeSentiment() {
        String userSentence = etUserSentence.getText().toString().trim();

        if (userSentence.isEmpty()) {
            Toast.makeText(this, "Please enter a sentence", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform sentiment analysis
        String sentimentResult = textAnalyticsService.analyzeSentiment(userSentence);
        String sentiment = extractSentiment(sentimentResult);

        // Display sentiment result
        tvSentimentResult.setText(sentimentResult);

        // Get current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentDateTime = dateFormat.format(Calendar.getInstance().getTime());

        // Create a new Firebase entry
        newEntryRef = databaseReference.push();
        newEntryRef.child("result").setValue(sentiment);
        newEntryRef.child("sentence").setValue(userSentence);
        newEntryRef.child("sentimentResult").setValue(sentimentResult);
        newEntryRef.child("dateTime").setValue(currentDateTime);
        newEntryRef.child("regNo").setValue(regNo);

        // Handle sentiment classification
        handleSentimentClassification(sentiment, userSentence);
    }

    private String extractSentiment(String sentimentResult) {
        try {
            return sentimentResult.substring(sentimentResult.indexOf(':') + 2, sentimentResult.indexOf(',')).trim();
        } catch (Exception e) {
            return "Unknown"; // In case of parsing errors
        }
    }

    private void handleSentimentClassification(String sentiment, String userSentence) {
        if (sentiment.equalsIgnoreCase("negative")) {
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            new CustomClassificationTask(this).execute(userSentence);
        } else {
            classification.setVisibility(View.GONE);
            suggestion.setVisibility(View.VISIBLE);
            String suggestionText = (sentiment.equalsIgnoreCase("neutral"))
                    ? "Suggestion: Doing better. Continue your therapy. 😊"
                    : "Suggestion: Doing good. Have a wonderful day ahead! 🎉";

            suggestion.setText(suggestionText);
            newEntryRef.child("SadClassification").setValue("Null");
            newEntryRef.child("suggestion").setValue(suggestionText);

            Toast.makeText(this, "Data Stored Successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateClassificationResult(String classificationResult) {
        classification.setText("Classification result: " + classificationResult);
        classification.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);

        String sentiment = extractSentiment(tvSentimentResult.getText().toString());
        String suggestionText = generateSuggestion(sentiment, classificationResult);

        suggestion.setText(suggestionText);
        suggestion.setVisibility(View.VISIBLE);

        if (newEntryRef != null) {
            newEntryRef.child("SadClassification").setValue(classificationResult);
            newEntryRef.child("suggestion").setValue(suggestionText);
            Toast.makeText(this, "Data Stored Successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateSuggestion(String sentiment, String classificationResult) {
        if (sentiment.equalsIgnoreCase("negative") &&
                (classificationResult.equalsIgnoreCase("Anxiety") ||
                        classificationResult.equalsIgnoreCase("Depression") ||
                        classificationResult.equalsIgnoreCase("ADHD") ||
                        classificationResult.equalsIgnoreCase("PTSD") ||
                        classificationResult.equalsIgnoreCase("Suicidal Ideation and Behavior") ||
                        classificationResult.equalsIgnoreCase("Social Anxiety and Disorder"))) {
            return "Suggestion: Need Therapy! Consult a psychologist at the earliest. 😟";
        } else {
            return sentiment.equalsIgnoreCase("neutral") ?
                    "Suggestion: Doing better. Continue your therapy. 😊" :
                    "Suggestion: Doing good. Have a wonderful day ahead! 🎉";
        }
    }

    public void openTherapyActivity() {
        Intent intent = new Intent(this, TherapyActivity.class);
        startActivity(intent);
    }
}