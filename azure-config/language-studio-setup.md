# Azure Language Studio Configuration Guide

This document provides step-by-step instructions for replicating the Azure Custom Text Classification model used in this study.

## Prerequisites

- An active Microsoft Azure subscription
- Access to Azure Language Studio (https://language.cognitive.azure.com/)
- An Azure Language Resource (S tier recommended; F0 free tier has rate limits)

## Step 1: Create an Azure Language Resource

1. Log in to the Azure Portal (https://portal.azure.com).
2. Navigate to **Create a resource** > **AI + Machine Learning** > **Language Service**.
3. Select the following configuration:
   - **Resource group:** Create new or use existing
   - **Region:** Select the region nearest to your deployment (e.g., East US, Central India)
   - **Pricing tier:** S (Standard) for production; F0 (Free) for testing with rate limits
4. Record the **Endpoint URL** and **API Key** from the Keys and Endpoint section.

## Step 2: Create a Custom Text Classification Project

1. Open Azure Language Studio (https://language.cognitive.azure.com/).
2. Select **Custom text classification** under the Classify text section.
3. Create a new project with the following settings:
   - **Project name:** `MentalHealthClassification`
   - **Project type:** Single-label classification
   - **Language:** English
   - **Description:** Classification of mental health-related text into five categories

## Step 3: Import Training Data

1. Upload the training dataset from `data/training-dataset/synthetic_training_data.json`.
2. The dataset contains 500 labeled synthetic text samples distributed as follows:
   - **Anxiety:** 100 samples
   - **Depression:** 100 samples
   - **PTSD:** 100 samples
   - **Social Anxiety Disorder:** 100 samples
   - **Suicidal Ideation and Behaviour:** 100 samples

## Step 4: Configure Category Labels

Define the following five classification labels exactly as shown:

| Label | Description |
|-------|-------------|
| `Anxiety` | Text expressing generalized anxiety, worry, panic, or nervousness |
| `Depression` | Text expressing persistent sadness, hopelessness, loss of interest, or low mood |
| `PTSD` | Text expressing trauma-related distress, flashbacks, hypervigilance, or avoidance |
| `Social_Anxiety_Disorder` | Text expressing fear of social situations, social withdrawal, or social evaluation anxiety |
| `Suicidal_Ideation_and_Behaviour` | Text expressing thoughts of self-harm, suicidal ideation, or desire to end life |

## Step 5: Train the Model

1. Set the **training/testing split** to **80% training / 20% testing**.
2. Start model training via the **Train model** tab.
3. Training typically completes within 15-30 minutes depending on resource tier.
4. Review the evaluation metrics in the **View model details** tab after training completes.

## Step 6: Deploy the Model

1. Navigate to the **Deploy model** tab.
2. Create a new deployment:
   - **Deployment name:** `mh-classifier-v1`
3. Record the deployment name for API integration.

## Step 7: Test the Endpoint

Use the following cURL command to verify the endpoint (replace placeholders):

```bash
curl -X POST "{ENDPOINT_URL}/language/:analyze-text?api-version=2022-05-01" \
  -H "Ocp-Apim-Subscription-Key: {API_KEY}" \
  -H "Content-Type: application/json" \
  -d '{
    "kind": "CustomSingleLabelClassification",
    "parameters": {
      "projectName": "MentalHealthClassification",
      "deploymentName": "mh-classifier-v1"
    },
    "analysisInput": {
      "documents": [
        {
          "id": "1",
          "language": "en",
          "text": "I feel like nobody would even notice if I disappeared tomorrow."
        }
      ]
    }
  }'
```

## Azure Sentiment Analysis

The first-stage sentiment analysis uses the pre-built Azure Sentiment Analysis service (no custom training required):

- **Service:** Azure AI Language — Sentiment Analysis
- **API version:** 2022-05-01 or later
- **Output:** Positive, Neutral, or Negative with confidence scores
- **Integration:** Via Azure SDK (`com.azure:azure-ai-textanalytics`) or REST API

No custom configuration is needed. The service is invoked directly through the SDK as shown in `android-app/SentimentAnalysisHelper.java`.

## Expected Results

Following this configuration with the provided dataset, the model should achieve performance comparable to the results reported in the manuscript:

- **Overall Precision:** 96.97%
- **Overall Recall:** 96.97%
- **Overall F1-score:** 96.97%

Minor variations may occur due to Azure's internal model updates and stochastic training processes.
