# 🌿 Sentiment Analysis & Mental Health Assessment App

**AI-powered Android application for analyzing user sentiment and assessing mental health using Azure's NLP services.**

---

## 📱 Overview

This Android app provides a modern AI-driven approach to sentiment analysis and mental health assessment. It uses **Microsoft Azure's Sentiment Analysis API** along with a **custom-trained Azure text classification model** to detect emotional tone in user-submitted text, including **sarcasm** and **emojis**. The app offers:

- 🔐 User and Admin login/signup system
- 🧠 Sentiment classification: Positive, Neutral, Negative
- 🎯 Further classification of negative sentiment into **Stress** or **Depression**
- 💬 Personalized feedback based on sentiment
- 📊 Admin dashboard with user sentiment history
- ☁️ Firebase Realtime Database for storage and user management

---

## 🧠 Key Features

- **Two-Tier Sentiment Analysis**: First detects overall sentiment, then classifies negative ones into stress or depression.
- **Emoji & Sarcasm Support**: Handles real-world text input with emojis and sarcasm.
- **Admin Dashboard**: Displays sentiment history and progression for each user.
- **Custom Feedback**:
  - Positive → "Doing good. Have a wonderful day ahead!"
  - Neutral → "Doing better, continue your therapy."
  - Negative → "Consult a psychologist at the earliest."

---

## ☁️ Technologies Used

| Technology            | Purpose                                      |
|-----------------------|----------------------------------------------|
| Android Studio (Java) | App development                              |
| XML & Material Design | UI design                                    |
| Firebase              | Authentication and Realtime Database         |
| Microsoft Azure       | Sentiment Analysis & Custom Classification   |
| Glide                 | Image loading and caching                    |

---
