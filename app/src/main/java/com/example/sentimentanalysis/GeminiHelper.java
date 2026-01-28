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

    // 🔑 Put your API key here
    private static final String API_KEY = "AIzaSyC_SvW_sevKCdf62GM5EZWwbRj20w78t7c";

    // ✅ Stable Android REST model
    private static final String MODEL_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + API_KEY;


    // Max text length to prevent API rejection
    private static final int MAX_INPUT_LENGTH = 4000;

    public static void generateGeminiResponse(String userInput, GeminiCallback callback) {

        new Thread(() -> {
            HttpURLConnection conn = null;
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

                // Multilingual instructions embedded directly in prompt
                String fullPrompt = "Always reply strictly in the same language as the user's input. " +
                        "Do not translate. Do not mix languages.\n\n" +
                        prompt;

                JSONObject textPart = new JSONObject().put("text", fullPrompt);
                JSONArray parts = new JSONArray().put(textPart);

                JSONObject userMessage = new JSONObject()
                        .put("role", "user")
                        .put("parts", parts);

                JSONArray contents = new JSONArray().put(userMessage);

                JSONObject payload = new JSONObject().put("contents", contents);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                }

                InputStream stream = conn.getResponseCode() >= 400
                        ? conn.getErrorStream()
                        : conn.getInputStream();

                Scanner scanner = new Scanner(stream, "UTF-8");
                StringBuilder responseBuilder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    responseBuilder.append(scanner.nextLine());
                }

                String responseText = responseBuilder.toString();
                JSONObject json = new JSONObject(responseText);

                // ✅ SAFELY handle missing candidates
                if (json.has("candidates") && json.getJSONArray("candidates").length() > 0) {
                    String reply = json
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    callback.onResponse(reply);
                } else if (json.has("error")) {
                    callback.onResponse("Gemini API error: " + json.getJSONObject("error").toString());
                } else {
                    callback.onResponse("Gemini returned no candidates. Try again with shorter text.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                callback.onResponse("Gemini error: " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    // Callback interface
    public interface GeminiCallback {
        void onResponse(String response);
    }
}
