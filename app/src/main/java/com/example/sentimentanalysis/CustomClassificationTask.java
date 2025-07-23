package com.example.sentimentanalysis;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CustomClassificationTask {
    private final Context context;
    private final String subscriptionKey = "1dksluHlMYX1PTQvbU4xxj3lF3d52ZkodI6LhL2l6fF4ZyYA1cjgJQQJ99BBACYeBjFXJ3w3AAAaACOGXUUD";
    private static final String BASE_URL = "https://sentidazzle123.cognitiveservices.azure.com/language/analyze-text/jobs?api-version=2022-10-01-preview";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public CustomClassificationTask(Context context) {
        this.context = context;
    }

    public void execute(String userSentence) {
        executorService.execute(() -> {
            String classificationResult = doInBackground(userSentence);
            handler.post(() -> onPostExecute(classificationResult));
        });
    }

    private String doInBackground(String userSentence) {
        String requestBody = "{\"tasks\":[{\"kind\":\"CustomMultiLabelClassification\",\"parameters\":{\"projectName\":\"senti123\",\"deploymentName\":\"senti1\"}}],\"displayName\":\"CustomTextPortal_CustomMultiLabelClassification\",\"analysisInput\":{\"documents\":[{\"id\":\"document_CustomMultiLabelClassification\",\"text\":\"" + userSentence + "\",\"language\":\"en\"}]}}";

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, requestBody);

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(body)
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try {
            Response response = client.newCall(request).execute();
            int responseCode = response.code();
            Log.e("Response Code", String.valueOf(responseCode));

            if (responseCode == 202) {
                String operationLocation = response.header("operation-location");

                while (true) {
                    assert operationLocation != null;
                    Request resultRequest = new Request.Builder()
                            .url(operationLocation)
                            .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                            .build();

                    Response resultResponse = client.newCall(resultRequest).execute();
                    if (resultResponse.isSuccessful() && resultResponse.body() != null) {
                        ResponseBody responseBody = resultResponse.body();
                        String resultString = responseBody.string();
                        Log.e("ResultResponseBody", resultString);
                        JSONObject resultObject = new JSONObject(resultString);

                        String status = resultObject.getString("status");
                        if ("succeeded".equalsIgnoreCase(status)) {
                            JSONArray documents = resultObject.getJSONObject("tasks")
                                    .getJSONArray("items")
                                    .getJSONObject(0)
                                    .getJSONObject("results")
                                    .getJSONArray("documents");
                            JSONObject classificationResult = documents.getJSONObject(0)
                                    .getJSONArray("class")
                                    .getJSONObject(0);

                            return classificationResult.getString("category");
                        } else {
                            Log.e("ResultResponseBody", "Task status: " + status + ". Retrying...");
                            Thread.sleep(1000);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onPostExecute(String classificationResult) {
        if (classificationResult != null) {
            ((SentimentAnalysisActivity) context).updateClassificationResult(classificationResult);
        }
    }
}