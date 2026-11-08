package com.example.carrental.data.repository

import android.content.Context
import java.util.Locale

// QUICK FIND: sound, language, and appearance preferences.
// Stores simple app settings; BaseCarRentalActivity applies locale and theme per screen.
object AppSettingsRepository {

    private const val PREFS_NAME = "app_settings"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_DARK_MODE = "dark_mode"

    // Returns the app settings preferences.
    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Reads whether sound effects are enabled.
    fun isSoundEnabled(context: Context): Boolean =
        preferences(context).getBoolean(KEY_SOUND_ENABLED, false)

    // Saves the sound-effects preference.
    fun setSoundEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    // Reads the selected app language, defaulting to the device language.
    fun getLanguage(context: Context): String = preferences(context).getString(
        KEY_LANGUAGE,
        if (Locale.getDefault().language == LANGUAGE_VIETNAMESE) {
            LANGUAGE_VIETNAMESE
        } else {
            LANGUAGE_ENGLISH
        }
    ) ?: LANGUAGE_ENGLISH

    // Saves the language; the current screen is recreated to load translated resources.
    fun setLanguage(context: Context, language: String) {
        preferences(context).edit().putString(KEY_LANGUAGE, language).apply()
    }

    // Reads whether the app dark theme is enabled.
    fun isDarkMode(context: Context): Boolean =
        preferences(context).getBoolean(KEY_DARK_MODE, false)

    // Saves the selected appearance; each Activity applies it before inflating its layout.
    fun setDarkMode(context: Context, enabled: Boolean) {
        preferences(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_VIETNAMESE = "vi"
}
