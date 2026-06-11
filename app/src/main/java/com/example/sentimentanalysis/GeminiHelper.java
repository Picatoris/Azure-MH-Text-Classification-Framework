package com.example.sentimentanalysis;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GeminiHelper {

    // ⚠️ SECURITY WARNING: Never commit your real API key to GitHub!
    // Ideally, read this from BuildConfig.API_KEY in a real app.
    private static final String API_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    private static final String MODEL_URL =
            "https://XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" + API_KEY;
    private static final int MAX_INPUT_LENGTH = 4000;

    // 🆕 CONSTANTS FOR RETRY LOGIC
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_BACKOFF_MS = 2000; // Start waiting 2 seconds

    public static void generateGeminiResponse(String userInput, GeminiCallback callback) {

        new Thread(() -> {
            HttpURLConnection conn = null;
            int attempt = 0;
            boolean success = false;
            int currentBackoff = INITIAL_BACKOFF_MS;

            // 🔄 RETRY LOOP
            while (attempt < MAX_RETRIES && !success) {
                try {
                    // Trim and limit input
                    String prompt = userInput == null ? "" : userInput.trim();
                    if (prompt.length() > MAX_INPUT_LENGTH) {
                        prompt = prompt.substring(0, MAX_INPUT_LENGTH);
                    }

                    URL url = new URL(MODEL_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    // JSON Construction
                    String fullPrompt = "Always reply strictly in the same language as the user's input. " +
                            "Do not translate. Do not mix languages.\n\n" + prompt;

                    JSONObject textPart = new JSONObject().put("text", fullPrompt);
                    JSONObject userMessage = new JSONObject()
                            .put("role", "user")
                            .put("parts", new JSONArray().put(textPart));

                    JSONObject payload = new JSONObject()
                            .put("contents", new JSONArray().put(userMessage));

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                    }

                    int responseCode = conn.getResponseCode();

                    // 🛑 CHECK FOR 429 ERROR (QUOTA EXCEEDED)
                    if (responseCode == 429) {
                        attempt++;
                        if (attempt < MAX_RETRIES) {
                            // Log it (optional)
                            System.out.println("Quota hit (429). Retrying in " + currentBackoff + "ms...");
                            Thread.sleep(currentBackoff);
                            currentBackoff *= 2; // Double the wait time (2s -> 4s -> 8s)
                            if (conn != null) conn.disconnect(); // Clean up before retry
                            continue; // Restart the loop
                        }
                    }

                    // Handle Response (Success or other errors)
                    InputStream stream = responseCode >= 400
                            ? conn.getErrorStream()
                            : conn.getInputStream();

                    if (stream == null) {
                        callback.onResponse("Error: Null response stream from API.");
                        return;
                    }

                    Scanner scanner = new Scanner(stream, "UTF-8");
                    StringBuilder responseBuilder = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        responseBuilder.append(scanner.nextLine());
                    }
                    scanner.close();

                    String responseText = responseBuilder.toString();
                    JSONObject json = new JSONObject(responseText);

                    if (responseCode >= 400) {
                        // Pass the actual error message from Google so you can debug
                        String errorMsg = json.has("error") ? json.getJSONObject("error").getString("message") : "Unknown error";
                        callback.onResponse("API Error (" + responseCode + "): " + errorMsg);
                    } else if (json.has("candidates") && json.getJSONArray("candidates").length() > 0) {
                        String reply = json.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        callback.onResponse(reply);
                        success = true; // Exit loop
                    } else {
                        callback.onResponse("No content returned.");
                    }

                } catch (Exception e) {
                    // Only retry on network exceptions if you want, but usually just 429
                    e.printStackTrace();
                    callback.onResponse("Exception: " + e.getMessage());
                    break; // Don't retry on crashes, only API errors
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }
        }).start();
    }

    public interface GeminiCallback {
        void onResponse(String response);
    }
}
