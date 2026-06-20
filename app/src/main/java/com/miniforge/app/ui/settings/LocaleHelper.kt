package com.miniforge.app.ui.settings

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "miniforge_settings"
    private const val KEY_LANGUAGE = "selected_language"

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Default to system language if supported, else English
        val systemLang = Locale.getDefault().language
        val supportedLangs = setOf("en", "es", "fr", "he", "de", "it", "pt", "ru")
        val defaultLang = if (systemLang in supportedLangs) systemLang else "en"
        return prefs.getString(KEY_LANGUAGE, defaultLang) ?: defaultLang
    }

    fun saveLanguage(context: Context, lang: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun applyLocale(context: Context): Context {
        val lang = getSavedLanguage(context)
        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        // Ensure proper text direction (LTR vs RTL) for languages like Hebrew
        config.setLayoutDirection(locale)
        
        return context.createConfigurationContext(config)
    }
}
