package com.example.sentimentanalysis;

import android.content.Context;
import android.util.Log;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.nl.translate.Translation;

import java.util.HashMap;
import java.util.Map;

public class MLTranslator {

    private static final String TAG = "MLTranslator";
    private static final Map<String, Translator> cache = new HashMap<>();

    public interface TranslationCallback {
        void onTranslated(String text);
    }

    public static void translate(
            Context context,
            String text,
            String targetLang,
            TranslationCallback callback
    ) {
        if (callback == null) return;

        if (text == null || text.trim().isEmpty()) {
            callback.onTranslated(text);
            return;
        }

        if (targetLang == null || targetLang.equals("en")) {
            callback.onTranslated(text);
            return;
        }

        String mlKitLang = getMlKitLanguage(targetLang);
        if (mlKitLang == null) {
            Log.w(TAG, "Language not supported by ML Kit: " + targetLang);
            callback.onTranslated(text); // fallback
            return;
        }

        Translator translator = getTranslator(mlKitLang);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused ->
                        translator.translate(text)
                                .addOnSuccessListener(callback::onTranslated)
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Translation failed", e);
                                    callback.onTranslated(text);
                                })
                )
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Model download failed", e);
                    callback.onTranslated(text);
                });
    }

    private static Translator getTranslator(String targetLang) {
        if (cache.containsKey(targetLang)) {
            return cache.get(targetLang);
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(targetLang)
                .build();

        Translator translator = Translation.getClient(options);
        cache.put(targetLang, translator);
        return translator;
    }

    /**
     * Returns ML Kit language code or null if unsupported
     */
    private static String getMlKitLanguage(String code) {
        switch (code) {
            case "hi": return TranslateLanguage.HINDI;
            case "ta": return TranslateLanguage.TAMIL;
            case "te": return TranslateLanguage.TELUGU;
            case "kn": return TranslateLanguage.KANNADA;
            case "mr": return TranslateLanguage.MARATHI;
            case "gu": return TranslateLanguage.GUJARATI;

            // ❌ Malayalam NOT supported by ML Kit
            case "ml": return null;

            default: return null;
        }
    }
}