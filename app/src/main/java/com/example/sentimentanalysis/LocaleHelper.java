package com.example.sentimentanalysis;

import android.content.Context;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import java.util.Locale;

public class LocaleHelper {

    private static Translator translator;
    private static String currentLang = "en";

    public static void setLocale(Context context, String langCode) {
        currentLang = langCode;
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(langCode)
                .build();
        translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Language ready (offline)", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Using online mode", Toast.LENGTH_SHORT).show();
                });

        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).recreate();
        }
    }

    public static void translate(String englishText, TranslateCallback callback) {
        if (translator == null || currentLang.equals("en")) {
            callback.onResult(englishText);
            return;
        }
        translator.translate(englishText)
                .addOnSuccessListener(callback::onResult)
                .addOnFailureListener(e -> callback.onResult(englishText));
    }

    public interface TranslateCallback {
        void onResult(String translatedText);
    }

    public static String getCurrentLang() {
        return currentLang;
    }
}