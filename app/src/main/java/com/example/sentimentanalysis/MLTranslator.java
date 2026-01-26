package com.example.sentimentanalysis;

import android.content.Context;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.nl.translate.Translation;

import java.util.HashMap;
import java.util.Map;

public class MLTranslator {

    private static final Map<String, Translator> translatorCache = new HashMap<>();

    public interface TranslationCallback {
        void onTranslated(String text);
    }

    public static void translate(
            Context context,
            String text,
            String targetLang,
            TranslationCallback callback
    ) {
        if (text == null || text.trim().isEmpty() || "en".equals(targetLang)) {
            callback.onTranslated(text);
            return;
        }

        Translator translator = getTranslator(targetLang);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused ->
                        translator.translate(text)
                                .addOnSuccessListener(callback::onTranslated)
                                .addOnFailureListener(e -> callback.onTranslated(text))
                )
                .addOnFailureListener(e -> callback.onTranslated(text));
    }

    private static Translator getTranslator(String targetLang) {
        Translator cached = translatorCache.get(targetLang);
        if (cached != null) {
            return cached;
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(getMlKitLanguage(targetLang))
                .build();

        Translator translator = Translation.getClient(options);
        translatorCache.put(targetLang, translator);
        return translator;
    }

    private static String getMlKitLanguage(String code) {
        switch (code) {
            case "hi": return TranslateLanguage.HINDI;
            case "ta": return TranslateLanguage.TAMIL;
            case "te": return TranslateLanguage.TELUGU;
            case "kn": return TranslateLanguage.KANNADA;
            case "mr": return TranslateLanguage.MARATHI;
            case "gu": return TranslateLanguage.GUJARATI;

            case "ml": return TranslateLanguage.fromLanguageTag("ml");
            case "bn": return TranslateLanguage.fromLanguageTag("bn");
            case "pa": return TranslateLanguage.fromLanguageTag("pa");
            case "ur": return TranslateLanguage.fromLanguageTag("ur");

            default: return TranslateLanguage.ENGLISH;
        }
    }
}