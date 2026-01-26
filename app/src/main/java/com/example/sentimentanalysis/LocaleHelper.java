package com.example.sentimentanalysis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_NAME = "language_pref";
    private static final String KEY_LANGUAGE = "selected_language";

    public static Context setLocale(Context context, String languageCode) {
        saveLanguage(context, languageCode);
        return updateResources(context, languageCode);
    }

    public static Context applySavedLocale(Context context) {
        String lang = getSavedLanguage(context);
        return updateResources(context, lang);
    }

    private static void saveLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    @SuppressLint("NewApi")
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }
}