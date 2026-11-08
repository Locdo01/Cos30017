package com.example.carrental.common

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.carrental.data.repository.AppSettingsRepository
import java.util.Locale

// QUICK FIND: shared language, theme, and quiet screen refresh.
// Applies saved settings before each screen loads its XML layout.
abstract class BaseCarRentalActivity : AppCompatActivity() {

    // Applies the saved locale before Android loads strings for this Activity.
    override fun attachBaseContext(newBase: Context) {
        val language = AppSettingsRepository.getLanguage(newBase)
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)

        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    // Applies the saved light or dark theme before the screen inflates its layout.
    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.localNightMode = if (AppSettingsRepository.isDarkMode(this)) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        super.onCreate(savedInstanceState)
    }

    // Rebuilds this screen without a window transition when a setting changes.
    @Suppress("DEPRECATION")
    fun recreateWithoutTransition() {
        recreate()
        overridePendingTransition(0, 0)
    }
}
