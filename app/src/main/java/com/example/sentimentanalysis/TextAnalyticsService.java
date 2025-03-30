package com.example.sentimentanalysis;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AnalyzeSentimentOptions;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;

public class TextAnalyticsService {

    private final TextAnalyticsClient textAnalyticsClient;

    public TextAnalyticsService(String key, String endpoint) {
        this.textAnalyticsClient = new TextAnalyticsClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildClient();
    }

    public String analyzeSentiment(String document) {
        AnalyzeSentimentOptions options = new AnalyzeSentimentOptions().setIncludeOpinionMining(true);
        final DocumentSentiment documentSentiment = textAnalyticsClient.analyzeSentiment(document, "en", options);
        SentimentConfidenceScores scores = documentSentiment.getConfidenceScores();

        return String.format("Recognized document sentiment: %s, \npositive score: %f, \nneutral score: %f, \nnegative score: %f",
                documentSentiment.getSentiment(), scores.getPositive(), scores.getNeutral(), scores.getNegative());
    }
}
