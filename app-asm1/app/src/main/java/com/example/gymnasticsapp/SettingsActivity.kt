package com.example.gymnasticsapp

import android.content.Context
import android.content.res.Configuration
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    companion object {
        var backgroundMusic = true
        var soundEffect = true
        var languageCode = "en"
        const val TAG = "GymApp"
    }

    var soundPool: SoundPool? = null
    var clickSound = 0
    var clickStream = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // Keep content clear of system bars while preserving XML padding.
        val root = findViewById<View>(R.id.main)
        val left = root.paddingLeft
        val top = root.paddingTop
        val right = root.paddingRight
        val bottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                left + bars.left,
                top + bars.top,
                right + bars.right,
                bottom + bars.bottom
            )
            insets
        }

        // Connect XML views.
        val musicSwitch = findViewById<Switch>(R.id.switchBgMusic)
        val effectSwitch = findViewById<Switch>(R.id.switchSoundEffect)
        val languageText = findViewById<TextView>(R.id.languageText)
        val languageGroup = findViewById<RadioGroup>(R.id.languageRadioGroup)
        val englishRadio = findViewById<RadioButton>(R.id.radioEnglish)
        val vietnameseRadio = findViewById<RadioButton>(R.id.radioVietnamese)
        val backButton = findViewById<Button>(R.id.btnBack)

        // Only our custom SoundPool controls click audio. Android's system click
        // sounds are disabled so the Sound Effect setting controls every app click.
        listOf(musicSwitch, effectSwitch, englishRadio, vietnameseRadio, backButton)
            .forEach { it.isSoundEffectsEnabled = false }

        languageText.text = getString(R.string.language_label)

        // Load the click sound.
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .build()
        clickSound = soundPool?.load(this, R.raw.music_click, 1) ?: 0

        // Show the saved-in-memory choices before adding listeners.
        musicSwitch.isChecked = backgroundMusic
        effectSwitch.isChecked = soundEffect

        if (languageCode == "vi") {
            vietnameseRadio.isChecked = true
        } else {
            englishRadio.isChecked = true
        }

        updateBackgroundMusic()

        // Background music on/off.
        musicSwitch.setOnCheckedChangeListener { _, isChecked ->
            backgroundMusic = isChecked
            updateBackgroundMusic()
            playClickSound()
            Log.d(TAG, "Background music: $backgroundMusic")
        }

        // Sound effects on/off.
        effectSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Update the setting first. Turning effects OFF must not play a click.
            soundEffect = isChecked
            if (soundEffect) {
                playClickSound()
            } else {
                // Stop a click sound that might still be playing in Settings.
                if (clickStream != 0) soundPool?.stop(clickStream)
                clickStream = 0
            }
            Log.d(TAG, "Sound effects: $soundEffect")
        }

        // English and Vietnamese selection.
        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            val newLanguage = when (checkedId) {
                R.id.radioEnglish -> "en"
                R.id.radioVietnamese -> "vi"
                else -> languageCode
            }

            if (newLanguage != languageCode) {
                playClickSound()
                languageCode = newLanguage
                Log.d(TAG, "Language changed to $languageCode")
                recreate()
            }
        }

        // Return to the previous screen.
        backButton.setOnClickListener {
            playClickSound()
            Log.d(TAG, "Back clicked")
            finish()
        }
    }

    fun updateBackgroundMusic() {
        val player = StartActivity.bgPlayer ?: return

        if (backgroundMusic && !player.isPlaying) {
            player.start()
        } else if (!backgroundMusic && player.isPlaying) {
            player.pause()
        }
    }

    fun playClickSound() {
        if (soundEffect && clickSound != 0) {
            clickStream = soundPool?.play(clickSound, 1f, 1f, 1, 0, 1f) ?: 0
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundMusic()
    }

    override fun onDestroy() {
        soundPool?.release()
        soundPool = null
        super.onDestroy()
    }
}
