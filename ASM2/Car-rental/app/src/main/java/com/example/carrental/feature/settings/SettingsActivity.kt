package com.example.carrental.feature.settings

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.carrental.common.BottomNavigationHelper
import com.example.carrental.common.BottomTab
import com.example.carrental.common.BaseCarRentalActivity
import com.example.carrental.data.repository.AppSettingsRepository
import com.example.carrental.feature.cars.CarActivity
import com.example.carrental.feature.cars.FavoriteActivity
import com.example.carrental.feature.rentals.RentalsActivity
import com.example.carrental.MainActivity
import com.example.carrental.R

// QUICK FIND: settings screen, sound switch, language, light and dark theme.
// Loads saved preferences and sends user changes to AppSettingsRepository.


class SettingsActivity : BaseCarRentalActivity() {

    private var updatingChoices = false
    private val settingsHandler = Handler(Looper.getMainLooper())
    private var pendingLanguageChange: Runnable? = null

    // Initializes the screen and connects its controls.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        showSavedPreferences()
        setupSoundSwitch()
        setupLanguageChoices()
        setupThemeSwitch()
        setupBottomNavigation()
    }

    // Loads saved preferences into the settings controls.
    private fun showSavedPreferences() {
        updatingChoices = true
        findViewById<SwitchCompat>(R.id.switchSound).isChecked =
            AppSettingsRepository.isSoundEnabled(this)

        val languageChoice = if (
            AppSettingsRepository.getLanguage(this) == AppSettingsRepository.LANGUAGE_VIETNAMESE
        ) R.id.rbVietnamese else R.id.rbEnglish
        findViewById<RadioGroup>(R.id.radioLanguage).check(languageChoice)

        findViewById<SwitchCompat>(R.id.switchDarkMode).isChecked =
            AppSettingsRepository.isDarkMode(this)
        updatingChoices = false
    }

    // Saves changes to the sound preference.
    private fun setupSoundSwitch() {
        findViewById<SwitchCompat>(R.id.switchSound).setOnCheckedChangeListener { _, enabled ->
            AppSettingsRepository.setSoundEnabled(this, enabled)
        }
    }

    // Saves and applies the selected app language.
    private fun setupLanguageChoices() {
        val languageGroup = findViewById<RadioGroup>(R.id.radioLanguage)
        languageGroup.setOnCheckedChangeListener { group, checkedId ->
                if (updatingChoices) return@setOnCheckedChangeListener
                if (checkedId != R.id.rbVietnamese && checkedId != R.id.rbEnglish) {
                    return@setOnCheckedChangeListener
                }

                pendingLanguageChange?.let(settingsHandler::removeCallbacks)
                val applyLanguage = Runnable {
                    if (isFinishing || isDestroyed) return@Runnable

                    // Read the final checked item after RadioGroup finishes the tap.
                    val selectedId = group.checkedRadioButtonId
                    val language = when (selectedId) {
                        R.id.rbVietnamese -> AppSettingsRepository.LANGUAGE_VIETNAMESE
                        R.id.rbEnglish -> AppSettingsRepository.LANGUAGE_ENGLISH
                        else -> return@Runnable
                    }
                    if (language != AppSettingsRepository.getLanguage(this)) {
                        AppSettingsRepository.setLanguage(this, language)
                        recreateWithoutTransition()
                    }
                }
                pendingLanguageChange = applyLanguage
                settingsHandler.postDelayed(applyLanguage, 120)
            }
    }

    // Saves the theme switch and reloads this screen without a window transition.
    private fun setupThemeSwitch() {
        findViewById<SwitchCompat>(R.id.switchDarkMode)
            .setOnCheckedChangeListener { _, enabled ->
                if (!updatingChoices && enabled != AppSettingsRepository.isDarkMode(this)) {
                    AppSettingsRepository.setDarkMode(this, enabled)
                    recreateWithoutTransition()
                }
            }
    }

    // Removes pending callbacks when this settings screen is closed.
    override fun onDestroy() {
        pendingLanguageChange?.let(settingsHandler::removeCallbacks)
        super.onDestroy()
    }

    // Connects the bottom navigation items for this screen.
    private fun setupBottomNavigation() {
        BottomNavigationHelper.setup(
            activity = this,
            selectedTab = BottomTab.SETTINGS,
            onHomeClick = { openPage(MainActivity::class.java) },
            onCarsClick = { openPage(CarActivity::class.java) },
            onFavoriteClick = { openPage(FavoriteActivity::class.java) },
            onRentalsClick = { openPage(RentalsActivity::class.java) },
            onSettingsClick = { /* Settings is already open. */ }
        )
    }

    // Opens the requested screen.
    private fun openPage(destination: Class<out AppCompatActivity>) {
        startActivity(Intent(this, destination))
        finish()
    }
}
