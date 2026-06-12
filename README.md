# Azure-Powered Hybrid Sentiment Analysis for Mental Health Assessment

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.XXXXXXX.svg)](https://doi.org/ DOI: 10.5281/zenodo.20652066)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Overview

This repository contains the source code, synthetic dataset, Azure Language Studio configuration, and preprocessing pipeline for the two-stage cloud-based framework described in:

> **A Cloud-Based Two-Layer Text Classification Framework for Mental Health Screening with Sarcasm and Emoji-Aware Sentiment Analysis**
>
> Thamizh Mani R, Vikram Palimar, Shashank Singh, Dazzle A J, R. Sujithra Kanmani, Mamatha Shivanandha Pai, Shwetha T S, Nirmal Krishnan M, Ramana Vinjamuri, Lata Satyen
>
> *Scientific Reports* (under review)

The framework combines **Azure Sentiment Analysis** (Layer 1) and **Azure Custom Text Classification** (Layer 2) to classify user-generated text into five mental health-related categories: Anxiety, Depression, PTSD, Social Anxiety Disorder, and Suicidal Ideation and Behaviour.

## Repository Structure

```
├── README.md
├── LICENSE
├── CITATION.cff
│
├── android-app/
│   ├── MainActivity.java              # Core app logic and UI handling
│   ├── SentimentAnalysisHelper.java    # Azure Sentiment Analysis API integration
│   ├── CustomClassificationHelper.java # Azure Custom Text Classification API integration
│   ├── FirebaseHelper.java             # Firebase Auth and Realtime Database operations
│   ├── FeedbackGenerator.java          # Category-specific feedback generation
│   ├── AdminDashboardActivity.java     # Admin monitoring and analytics interface
│   ├── res/
│   │   ├── layout/                     # XML layout files
│   │   └── values/                     # Strings, colors, themes
│   └── build.gradle                    # Dependencies (Azure SDK, OkHttp, Firebase)
│
├── azure-config/
│   ├── language-studio-setup.md        # Step-by-step Azure Language Studio configuration
│   ├── project-settings.json           # Category labels, split ratios, project parameters
│   ├── endpoint-template.env           # API endpoint and key template (credentials redacted)
│   └── sentiment-analysis-config.md    # Azure Sentiment Analysis service setup
│
├── data/
│   ├── training-dataset/
│   │   └── synthetic_training_data.json    # 400 labeled synthetic samples (80 per category)
│   ├── testing-dataset/
│   │   └── synthetic_testing_data.json     # 100 labeled synthetic samples (20 per category)
│   ├── clinical-validation/
│   │   └── clinical_validation_data.json   # 125 samples reviewed by clinical psychologists
│   └── data-dictionary.md                  # Category definitions and labeling guidelines
│
├── preprocessing/
│   ├── text_preprocessing.py           # Normalization, lowercasing, punctuation, token standardization
│   └── requirements.txt                # Python dependencies
│
├── evaluation/
│   ├── confusion_matrix_primary.png    # Confusion matrix from primary test dataset
│   ├── confusion_matrix_clinical.png   # Confusion matrix from clinical validation dataset
│   └── evaluation_metrics.md           # Detailed performance metrics and class-level results
│
├── figures/
│   ├── Figure_1_Architecture.png       # System architecture overview
│   └── Figure_2_Workflow.png           # Sentiment analysis and classification workflow
│
└── docs/
    ├── PATH_checklist.pdf              # Completed PATH protocol checklist
    └── annotation_guidelines.md        # Guidelines used by clinical reviewers
```

## System Architecture

The framework operates as a two-stage classification pipeline deployed within an Android application:

**Stage 1 — Sentiment Analysis:** User-generated text is submitted to Azure Sentiment Analysis, which classifies input as positive, neutral, or negative. Positive and neutral inputs are stored with corresponding feedback. Negative inputs proceed to Stage 2.

**Stage 2 — Mental Health Classification:** Text classified as negative is forwarded to Azure Custom Text Classification (trained via Azure Language Studio) for categorization into one of five mental health-related classes.

**Backend:** Firebase Authentication handles user management. Firebase Realtime Database stores submissions, classifications, and feedback records with real-time synchronization.

## Requirements

### Android Application
- Android Studio (Arctic Fox or later)
- Java 11+
- Minimum SDK: API 24 (Android 7.0)
- Dependencies listed in `android-app/build.gradle`:
  - `com.azure:azure-ai-textanalytics`
  - `com.squareup.okhttp3:okhttp`
  - `com.google.firebase:firebase-auth`
  - `com.google.firebase:firebase-database`

### Azure Services
- Microsoft Azure subscription
- Azure Language Resource (S tier recommended for production)
- Azure Language Studio access for Custom Text Classification

### Preprocessing
- Python 3.8+
- Dependencies: `pip install -r preprocessing/requirements.txt`

## Reproducing Results

### 1. Azure Language Studio Setup

Follow the instructions in `azure-config/language-studio-setup.md`:

1. Create an Azure Language Resource in the Azure Portal.
2. Navigate to [Language Studio](https://language.cognitive.azure.com/).
3. Create a new **Custom Text Classification** project (single-label).
4. Import the training dataset from `data/training-dataset/synthetic_training_data.json`.
5. Configure five category labels: `Anxiety`, `Depression`, `PTSD`, `Social_Anxiety_Disorder`, `Suicidal_Ideation_and_Behaviour`.
6. Set training/testing split to 80/20.
7. Train the model and deploy to an endpoint.
8. Record the endpoint URL and API key in `azure-config/endpoint-template.env`.

### 2. Running the Preprocessing Pipeline

```bash
cd preprocessing
pip install -r requirements.txt
python text_preprocessing.py --input ../data/training-dataset/synthetic_training_data.json --output preprocessed_output.json
```

### 3. Building the Android Application

1. Open the `android-app/` directory in Android Studio.
2. Copy `azure-config/endpoint-template.env` values into your local `gradle.properties` or `local.properties`.
3. Configure Firebase by adding your `google-services.json` to the app module.
4. Build and run on an emulator or physical device (API 24+).

### 4. Evaluating Classification Performance

The trained Azure Custom Text Classification model achieved the following on the held-out test dataset:

| Category | Precision (%) | Recall (%) | F1 Score |
|---|---|---|---|
| Anxiety | 95.24 | 100.00 | 0.976 |
| Depression | 100.00 | 100.00 | 1.000 |
| PTSD | 100.00 | 95.00 | 0.974 |
| Social Anxiety Disorder | 90.48 | 100.00 | 0.950 |
| Suicidal Ideation and Behaviour | 100.00 | 90.00 | 0.947 |
| **Overall** | **96.97** | **96.97** | **0.9697** |

Additional validation using 125 clinically reviewed samples is documented in `evaluation/evaluation_metrics.md`.

## Important Notes

- **Azure Managed Services:** Azure Sentiment Analysis and Azure Custom Text Classification are fully managed cloud services. Azure Language Studio performs feature extraction, model training, optimization, and evaluation internally. Internal model architecture, weights, and hyperparameters are not exposed to end users. Consequently, no custom model training code exists beyond the platform configuration provided in this repository.
- **Synthetic Dataset:** All training and evaluation data are synthetically generated. No real user posts, clinical records, or personally identifiable information were collected or used.
- **Not a Diagnostic Tool:** This framework is intended for text classification and screening support. It is not a clinical diagnostic system, medical device, or substitute for professional healthcare services.

## Citation

If you use this code or dataset, please cite:

```bibtex
@article{mani2026cloudbased,
  title={A Cloud-Based Two-Layer Text Classification Framework for Mental Health Screening with Sarcasm and Emoji-Aware Sentiment Analysis},
  author={Mani R, Thamizh and Palimar, Vikram and Singh, Shashank and A J, Dazzle and Kanmani, R. Sujithra and Pai, Mamatha Shivanandha and T S, Shwetha and Krishnan M, Nirmal and Vinjamuri, Ramana and Satyen, Lata},
  journal={Scientific Reports},
  year={2026},
  publisher={Nature Publishing Group},
  doi={10.5281/zenodo.XXXXXXX}
}
```

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

## Contact

For questions or requests for additional materials, contact:

- **Thamizh Mani R** — thamizh.mani@learner.manipal.edu
- **Dazzle A J** — dazzlejolly@gmail.com
- **Shashank Singh** — shashanksingh631@gmail.com
.
