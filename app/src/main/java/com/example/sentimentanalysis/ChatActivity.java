package com.example.sentimentanalysis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

        // Initialize Views
        recyclerView = findViewById(R.id.chatRecyclerView);
        userInput = findViewById(R.id.inputEditText);
        ImageButton sendButton = findViewById(R.id.sendButton);
        ImageButton micButton = findViewById(R.id.micButton);
        ImageView backButton = findViewById(R.id.btn_back);
        ImageView soundButton = findViewById(R.id.btn_sound);

        // Note: We don't strictly need the loading spinner anymore since
        // we use a "Typing..." chat bubble, but I'll leave it as a backup UI element.
        loadingIndicator = findViewById(R.id.loadingSpinner);

        // Setup RecyclerView
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Auto-scroll to bottom when keyboard opens
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);

        // Send Button
        sendButton.setOnClickListener(v -> {
            String message = userInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });

        // Mic Button
        micButton.setOnClickListener(v -> startVoiceInput());

        // Back Button
        backButton.setOnClickListener(v -> finish());

        // TTS (Sound) Button
        soundButton.setOnClickListener(v -> {
            if (!lastBotResponse.isEmpty()) {
                speakText(lastBotResponse);
            } else {
                Toast.makeText(this, "No message to read yet", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "TTS Language not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ---------------- CORE MESSAGING LOGIC ----------------

    private void sendMessage(String message) {
        // 1. Add User Message to UI
        addUserMessage(message);
        userInput.setText("");

        // 2. Show "Typing..." Indicator
        addTypingIndicator();

        // 3. Send to Gemini
        fetchGeminiResponse(message);
    }

    private void addUserMessage(String message) {
        ChatMessage userMsg = new ChatMessage(message, ChatMessage.SENDER_USER, System.currentTimeMillis());
        chatAdapter.addMessage(userMsg);
        scrollToBottom();
    }

    private void addTypingIndicator() {
        // Only add if not already present
        if (!chatAdapter.hasTypingMessage()) {
            ChatMessage typingMsg = new ChatMessage("Gemini is thinking...", ChatMessage.SENDER_BOT_TYPING, System.currentTimeMillis());
            chatAdapter.addMessage(typingMsg);
            scrollToBottom();
        }
    }

    private void removeTypingIndicator() {
        // We let the Adapter handle the logic of finding and removing the specific item
        chatAdapter.removeTypingMessage();
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    // ---------------- GEMINI API CALL ----------------

    private void fetchGeminiResponse(String prompt) {
        // Optional: Show the top spinner (if you want double indication)
        // loadingIndicator.setVisibility(View.VISIBLE);

        GeminiHelper.generateGeminiResponse(prompt, response -> {
            // Must run UI updates on main thread
            runOnUiThread(() -> {
                // loadingIndicator.setVisibility(View.GONE);

                // 1. Remove "Typing..."
                removeTypingIndicator();

                // 2. Clean up the response (Simple Markdown Fix)
                String cleanResponse = response.replace("**", ""); // Removes bold markers for cleaner plain text
                lastBotResponse = cleanResponse;

                // 3. Add Bot Message
                ChatMessage botMsg = new ChatMessage(cleanResponse, ChatMessage.SENDER_BOT, System.currentTimeMillis());
                chatAdapter.addMessage(botMsg);

                // 4. Scroll
                scrollToBottom();
            });
        });
    }

    // ---------------- VOICE INPUT ----------------

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");
        try {
            startActivityForResult(intent, VOICE_INPUT_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------- FILE PICKER (For future use) ----------------

    // Note: If you want to use this, you need a button in your XML layout that calls this method.
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain"); // Restrict to text files for now to be safe
        startActivityForResult(intent, FILE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_INPUT_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                userInput.setText(spokenText);
                // Optional: Auto-send after speaking
                // sendMessage(spokenText);
            }
        }

        if (requestCode == FILE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                extractTextFromUri(uri);
            }
        }
    }

    // ---------------- FILE READING ----------------

    private void extractTextFromUri(Uri uri) {
        new Thread(() -> {
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

                String finalContent = fileText.toString();

                runOnUiThread(() -> {
                    // Send the file content as a message
                    sendMessage("Analyze this file content:\n\n" + finalContent);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Failed to read file", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // ---------------- TEXT TO SPEECH ----------------

    private void speakText(String text) {
        if (tts != null) {
            // Stop any current speech
            tts.stop();
            // Speak new text
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}