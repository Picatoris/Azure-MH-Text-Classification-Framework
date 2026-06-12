# Azure-Powered Hybrid Sentiment Analysis for Mental Health Assessment

[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.XXXXXXX.svg)](https://doi.org/10.5281/zenodo.XXXXXXX)
![License: Academic Inspection](https://img.shields.io/badge/License-Academic%20Inspection-red.svg)
![Rights: All Rights Reserved](https://img.shields.io/badge/Rights-All%20Rights%20Reserved-red.svg)

> **⚠️ INTELLECTUAL PROPERTY NOTICE:** This repository is protected under registered Copyright and Patent agreements filed with the Government of India. The code and associated materials are provided solely for academic inspection, peer review, and reproducibility verification. Commercial use, modification, distribution, and private use are strictly prohibited. See [LICENSE](LICENSE) for full terms.

## Overview

This repository contains the source code, synthetic dataset, Azure Language Studio configuration, and clinical validation materials for the two-stage cloud-based framework described in:

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
├── .gitignore
├── .github/workflows/
│   └── gradle.yml                          # CI build workflow
│
├── app/                                    # Complete Android application (Java / XML)
│   ├── build.gradle.kts                    # App-level dependencies (Azure SDK, Firebase, OkHttp)
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/sentimentanalysis/
│       │   ├── SentimentAnalysisActivity.java      # Core UI — user text input and result display
│       │   ├── TextAnalyticsService.java            # Azure Sentiment Analysis API integration
│       │   ├── CustomClassificationTask.java        # Azure Custom Text Classification API call
│       │   ├── GeminiHelper.java                    # Gemini API integration for feedback generation
│       │   ├── ChatActivity.java                    # AI-assisted conversational interface
│       │   ├── ChatAdapter.java                     # RecyclerView adapter for chat messages
│       │   ├── ChatMessage.java                     # Chat message data model
│       │   ├── MLTranslator.java                    # ML Kit on-device translation module
│       │   ├── AdminDashboardActivity.java          # Admin panel — analytics and user monitoring
│       │   ├── AdminLoginActivity.java              # Admin authentication
│       │   ├── AdminSignUpActivity.java             # Admin registration
│       │   ├── AdminActivity.java                   # Admin navigation handler
│       │   ├── UserDashboardActivity.java           # User home screen with navigation
│       │   ├── UserLoginActivity.java               # User authentication via Firebase
│       │   ├── UserSignUpActivity.java              # User registration with Firebase Auth
│       │   ├── UserActivity.java                    # User profile and settings
│       │   ├── ForgotPasswordActivity.java          # Password recovery flow
│       │   ├── BaseActivity.java                    # Shared base class for all activities
│       │   ├── StartActivity.java                   # App entry point and role selection
│       │   ├── StartLoadingActivity.java            # Splash/loading screen
│       │   ├── OnboardingActivity.java              # First-launch walkthrough
│       │   ├── OnboardingAdapter.java               # ViewPager adapter for onboarding
│       │   ├── OnboardingFragment1–3.java           # Onboarding screen fragments
│       │   ├── BookConsultationActivity.java        # Counselor booking interface
│       │   ├── CounselorAdapter.java                # Counselor list adapter
│       │   ├── DoctorAdapter.java                   # Doctor list adapter
│       │   ├── DoctorModel.java                     # Doctor data model
│       │   ├── HelplineActivity.java                # Crisis helpline directory
│       │   ├── HelplineAdapter.java                 # Helpline list adapter
│       │   ├── HelplineModel.java                   # Helpline data model
│       │   ├── TherapyActivity.java                 # Self-help therapy resources
│       │   ├── GuideActivity.java                   # User guide and instructions
│       │   ├── ChartPopupActivity.java              # Analytics chart popup
│       │   ├── BreathingGameActivity.java           # Breathing exercise gamification
│       │   ├── GratitudeGardenActivity.java         # Gratitude journaling gamification
│       │   ├── StepCounterGameActivity.java         # Step counter wellness game
│       │   ├── WaterQuestActivity.java              # Hydration tracking gamification
│       │   ├── WaterReminderReceiver.java           # Broadcast receiver for water reminders
│       │   ├── WaterReminderScheduler.java          # Alarm scheduler for hydration alerts
│       │   ├── DailyReminderReceiver.java           # Daily check-in notification handler
│       │   └── LocaleHelper.java                    # Multi-language support utility
│       └── res/
│           ├── layout/                     # 30+ XML layout files for all screens
│           ├── drawable/                   # 70+ icons, backgrounds, and UI assets
│           ├── menu/                       # Navigation drawer menu
│           ├── anim/                       # Slide transition animations
│           ├── values/                     # Colors, strings, styles, themes
│           ├── mipmap-*/                   # App launcher icons (all densities)
│           └── xml/                        # Backup and data extraction rules
│
├── azure-config/                           # Azure Language Studio configuration
│   ├── language-studio-setup.md            # Step-by-step Azure Language Studio replication guide
│   ├── project-settings.json               # Category labels, training split, deployment config
│   └── endpoint-template.env               # API endpoint and key template (credentials redacted)
│
├── Clinically Annotated Data/              # Clinical validation dataset (125 samples)
│   ├── Anxiety.pdf                         # 25 clinically annotated anxiety samples
│   ├── Depression.pdf                      # 25 clinically annotated depression samples
│   ├── PTSD.pdf                            # 25 clinically annotated PTSD samples
│   ├── Social Anxiety Disorder.pdf         # 25 clinically annotated social anxiety disorder samples
│   └── Suicidal Ideation.pdf              # 25 clinically annotated suicidal ideation samples
│
├── Clincal Validation Certificates/        # PATH protocol validation documents
│   ├── PATH VALIDATION 1.pdf              # Completed PATH checklist — Reviewer 1
│   ├── PATH VALIDATION 2.pdf              # Completed PATH checklist — Reviewer 2
│   └── Reference Paper.pdf                 # PATH protocol reference paper
│
├── Paper Images/                           # All manuscript figures and evaluation visuals
│   ├── Figure_1_Architecture.png           # System architecture diagram (Figure 1)
│   ├── Figure_2_Workflow.png               # Classification workflow (Figure 2)
│   ├── CONFUSION MATRIX.png               # Confusion matrix from primary evaluation
│   ├── Metrics.png                         # Overall performance metrics
│   ├── Metrics_class_wise_performance.png  # Class-level precision, recall, F1
│   ├── *_example_with_emoji.png            # App screenshots with emoji input (5 categories)
│   └── *_example_without_emoji.png         # App screenshots without emoji input (5 categories)
│
├── legal/                                  # Intellectual property documentation
│   ├── patent1.jpg                         # Patent filing confirmation
│   ├── patent2.png                         # Patent application details
│   ├── copyright1_page1.jpg               # Copyright registration (page 1)
│   ├── copyright1_page2.jpg               # Copyright registration (page 2)
│   ├── copyright2_page1.jpg               # Second copyright registration (page 1)
│   └── copyright2_page2.jpg               # Second copyright registration (page 2)
│
├── Data.zip                                # Synthetic training and testing dataset (500 samples)
├── MIND_v1.0_debug.apk                     # Debug build of the MIND Android application
├── Supplementary_01.pdf                    # Supplementary material 1
├── Supplementary_02.pdf                    # Supplementary material 2
│
├── build.gradle.kts                        # Project-level Gradle build file
├── settings.gradle.kts                     # Gradle settings
├── gradle.properties                       # Gradle configuration properties
└── gradle/wrapper/                         # Gradle wrapper for build reproducibility
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
- Dependencies listed in `app/build.gradle.kts`:
  - `com.azure:azure-ai-textanalytics`
  - `com.squareup.okhttp3:okhttp`
  - `com.google.firebase:firebase-auth`
  - `com.google.firebase:firebase-database`
  - Google ML Kit Translation SDK
  - Google Gemini API SDK

### Azure Services
- Microsoft Azure subscription
- Azure Language Resource (S tier recommended for production)
- Azure Language Studio access for Custom Text Classification

## Reproducing Results

### 1. Azure Language Studio Setup

Follow the instructions in `azure-config/language-studio-setup.md`:

1. Create an Azure Language Resource in the Azure Portal.
2. Navigate to [Language Studio](https://language.cognitive.azure.com/).
3. Create a new **Custom Text Classification** project (single-label).
4. Import the training dataset from `Data.zip`.
5. Configure five category labels: `Anxiety`, `Depression`, `PTSD`, `Social_Anxiety_Disorder`, `Suicidal_Ideation_and_Behaviour`.
6. Set training/testing split to 80/20.
7. Train the model and deploy to an endpoint.
8. Record the endpoint URL and API key using the template in `azure-config/endpoint-template.env`.

### 2. Building the Android Application

1. Open the project root directory in Android Studio.
2. Add your own `google-services.json` from the Firebase Console to the `app/` module.
3. Configure Azure endpoint credentials in the appropriate configuration file.
4. Build and run on an emulator or physical device (API 24+).

### 3. Classification Performance

The trained Azure Custom Text Classification model achieved the following on the held-out test dataset (100 samples):

| Category | Precision (%) | Recall (%) | F1 Score |
|---|---|---|---|
| Anxiety | 95.24 | 100.00 | 0.976 |
| Depression | 100.00 | 100.00 | 1.000 |
| PTSD | 100.00 | 95.00 | 0.974 |
| Social Anxiety Disorder | 90.48 | 100.00 | 0.950 |
| Suicidal Ideation and Behaviour | 100.00 | 90.00 | 0.947 |
| **Overall** | **96.97** | **96.97** | **0.9697** |

An independent validation using 125 clinically annotated samples reviewed by qualified clinical psychologists is documented in the `Clinically Annotated Data/` directory and the corresponding PATH protocol checklists are available in `Clincal Validation Certificates/`.

## Important Notes

- **Azure Managed Services:** Azure Sentiment Analysis and Azure Custom Text Classification are fully managed cloud services. Azure Language Studio performs feature extraction, model training, optimization, and evaluation internally. Internal model architecture, weights, and hyperparameters are not exposed to end users. Consequently, no custom model training code exists beyond the platform configuration provided in this repository.

- **Synthetic Dataset:** All training and evaluation data are synthetically generated. No real user posts, clinical records, or personally identifiable information were collected or used at any stage of this study.

- **Not a Diagnostic Tool:** This framework is intended for text classification and screening support within a research context. It is not a clinical diagnostic system, medical device, or substitute for professional healthcare services.

- **Intellectual Property:** The system architecture, classification framework, and associated methods are protected under registered Copyright and Patent agreements filed with the Government of India. See `legal/` for documentation. All rights reserved.

## Citation

If you reference this work, please cite:

```bibtex
@article{mani2026cloudbased,
  title={A Cloud-Based Two-Layer Text Classification Framework for Mental Health
         Screening with Sarcasm and Emoji-Aware Sentiment Analysis},
  author={Mani R, Thamizh and Palimar, Vikram and Singh, Shashank and
          A J, Dazzle and Kanmani, R. Sujithra and Pai, Mamatha Shivanandha and
          T S, Shwetha and Krishnan M, Nirmal and Vinjamuri, Ramana and Satyen, Lata},
  journal={Scientific Reports},
  year={2026},
  publisher={Nature Publishing Group},
  doi={10.5281/zenodo.XXXXXXX}
}
```

## License

This repository is provided under an **Academic Inspection License (All Rights Reserved)**. The code and associated materials are made available solely for the purpose of academic inspection, peer review, and verification of the published research findings. Commercial use, modification, distribution, and private use are strictly prohibited without prior written permission from the copyright holders. See [LICENSE](LICENSE) for full terms.

## Contact

For licensing enquiries, collaboration proposals, or permission requests, contact:

- **Thamizh Mani R** — thamizh.mani@learner.manipal.edu
- **Dazzle A J** — dazzlejolly@gmail.com
- **Shashank Singh** — shashanksingh631@gmail.com

Department of Forensic Medicine and Toxicology, Kasturba Medical College, Manipal Academy of Higher Education, Manipal, Karnataka, India.
