package com.example.sentimentanalysis;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GeminiHelper {

    private static final String API_KEY = "AIzaSyBIg-YhmLztUefLmaVpUqWDieb3rkWIKAg"; // Replace with your real key

    public static void generateGeminiResponse(String prompt, GeminiCallback callback) {
        new Thread(() -> {
            try {
                // Gemini 2.0 Flash endpoint
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-goog-api-key", API_KEY);
                conn.setDoOutput(true);

                // Payload based on Gemini 2.0 format
                JSONObject contentPart = new JSONObject().put("text", prompt);
                JSONArray partsArray = new JSONArray().put(contentPart);
                JSONObject contentObject = new JSONObject().put("parts", partsArray);
                JSONArray contentsArray = new JSONArray().put(contentObject);
                JSONObject payload = new JSONObject().put("contents", contentsArray);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes());
                    os.flush();
                }

                // Read response
                Scanner in = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (in.hasNext()) {
                    response.append(in.nextLine());
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                String reply = jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                callback.onResponse(reply);

            } catch (Exception e) {
                callback.onResponse("Sorry, something went wrong.");
                e.printStackTrace();
            }
        }).start();
    }

    public interface GeminiCallback {
        void onResponse(String response);
    }
}