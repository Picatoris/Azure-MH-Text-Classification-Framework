package com.example.sentimentanalysis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private static final int VOICE_INPUT_REQUEST = 1;
    private static final int FILE_PICKER_REQUEST = 2;

    private ChatAdapter chatAdapter;
    private RecyclerView recyclerView;
    private EditText userInput;
    private ProgressBar loadingIndicator;
    private TextToSpeech tts;
    private String lastBotResponse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.chatRecyclerView);
        userInput = findViewById(R.id.inputEditText);
        ImageButton sendButton = findViewById(R.id.sendButton);
        ImageButton micButton = findViewById(R.id.micButton);
        ImageView backButton = findViewById(R.id.btn_back);
        ImageView soundButton = findViewById(R.id.btn_sound);
        loadingIndicator = findViewById(R.id.loadingSpinner);

        chatAdapter = new ChatAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> {
            String message = userInput.getText().toString().trim();
            if (!message.isEmpty()) {
                addUserMessage(message);
                userInput.setText("");
                addTypingIndicator();
                fetchGeminiResponse(message);
            }
        });

        micButton.setOnClickListener(v -> startVoiceInput());

        backButton.setOnClickListener(v -> finish());

        soundButton.setOnClickListener(v -> {
            if (!lastBotResponse.isEmpty()) {
                speakText(lastBotResponse);
            }
        });

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    // ---------------- USER MESSAGE ----------------
    private void addUserMessage(String message) {
        ChatMessage userMsg = new ChatMessage(message, ChatMessage.SENDER_USER, System.currentTimeMillis());
        chatAdapter.addMessage(userMsg);
        scrollToBottom();
    }

    // ---------------- TYPING INDICATOR ----------------
    private void addTypingIndicator() {
        ChatMessage typingMsg = new ChatMessage("Gemini is thinking...", ChatMessage.SENDER_BOT_TYPING, System.currentTimeMillis());
        chatAdapter.addMessage(typingMsg);
        scrollToBottom();
    }

    private void removeTypingIndicator() {
        int index = chatAdapter.getTypingMessageIndex();
        if (index != -1) {
            chatAdapter.removeMessage(index);
        }
    }

    // ---------------- SCROLL TO BOTTOM ----------------
    private void scrollToBottom() {
        recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }

    // ---------------- FETCH GEMINI RESPONSE ----------------
    private void fetchGeminiResponse(String prompt) {
        loadingIndicator.setVisibility(View.VISIBLE);

        GeminiHelper.generateGeminiResponse(prompt, response -> runOnUiThread(() -> {
            loadingIndicator.setVisibility(View.GONE);
            lastBotResponse = response;

            // Remove typing indicator before adding real message
            removeTypingIndicator();

            ChatMessage botMsg = new ChatMessage(response, ChatMessage.SENDER_BOT, System.currentTimeMillis());
            chatAdapter.addMessage(botMsg);
            scrollToBottom();
        }));
    }

    // ---------------- VOICE INPUT ----------------
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, VOICE_INPUT_REQUEST);
    }

    // ---------------- FILE PICKER ----------------
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/plain", "application/pdf"});
        startActivityForResult(intent, FILE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_INPUT_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            assert result != null;
            userInput.setText(result.get(0));
        }

        if (requestCode == FILE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                extractTextFromUri(uri);
            }
        }
    }

    // ---------------- FILE EXTRACTION ----------------
    private void extractTextFromUri(Uri uri) {
        try {
            StringBuilder fileText = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getContentResolver().openInputStream(uri))
            );
            String line;
            while ((line = reader.readLine()) != null) {
                fileText.append(line).append("\n");
            }
            reader.close();

            addUserMessage("Uploaded:\n" + fileText.toString());
            addTypingIndicator();
            fetchGeminiResponse(fileText.toString());

        } catch (Exception e) {
            e.printStackTrace();
            chatAdapter.addMessage(new ChatMessage("Failed to read file.", ChatMessage.SENDER_BOT, System.currentTimeMillis()));
        }
    }

    // ---------------- TTS ----------------
    private void speakText(String text) {
        if (tts != null && !tts.isSpeaking()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.shutdown();
        }
        super.onDestroy();
    }
}