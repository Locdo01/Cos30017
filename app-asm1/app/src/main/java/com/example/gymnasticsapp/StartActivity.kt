package com.example.gymnasticsapp

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class StartActivity : AppCompatActivity() {

    companion object {
        const val TAG = "GymApp"
        var bgPlayer: MediaPlayer? = null
    }

    lateinit var soundPool: SoundPool
    var clickSound = 0
    var appliedLanguage = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appliedLanguage = SettingsActivity.languageCode
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)

        // Keep the content clear of the system bars.
        val root = findViewById<View>(R.id.main)
        val initialLeft = root.paddingLeft
        val initialTop = root.paddingTop
        val initialRight = root.paddingRight
        val initialBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                initialLeft + bars.left,
                initialTop + bars.top,
                initialRight + bars.right,
                initialBottom + bars.bottom
            )
            insets
        }

        // Create the background music player only once.
        if (bgPlayer == null) {
            bgPlayer = MediaPlayer.create(this, R.raw.music_background)
            bgPlayer?.isLooping = true
            bgPlayer?.setVolume(0.65f, 0.65f)
        }

        // Load the button click sound.
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .build()
        clickSound = soundPool.load(this, R.raw.music_click, 1)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnSettings = findViewById<Button>(R.id.btnSettings)

        btnStart.setOnClickListener {
            Log.d(TAG, "Start clicked")
            playClickSound()
            startActivity(Intent(this, GymActivity::class.java))
        }

        btnSettings.setOnClickListener {
            Log.d(TAG, "Settings clicked")
            playClickSound()
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        updateBackgroundMusic()
    }

    fun playClickSound() {
        if (SettingsActivity.soundEffect && clickSound != 0) {
            soundPool.play(clickSound, 1f, 1f, 1, 0, 1f)
        }
    }

    fun updateBackgroundMusic() {
        val player = bgPlayer ?: return
        if (SettingsActivity.backgroundMusic && !player.isPlaying) {
            player.start()
        } else if (!SettingsActivity.backgroundMusic && player.isPlaying) {
            player.pause()
        }
    }

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            applyLocale(newBase, SettingsActivity.languageCode)
        )
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundMusic()
        if (appliedLanguage != SettingsActivity.languageCode) {
            Log.d(
                TAG,
                "Language changed to ${SettingsActivity.languageCode}"
            )
            recreate()
        }
    }

    override fun onDestroy() {
        if (::soundPool.isInitialized) {
            soundPool.release()
        }
        // Keep bgPlayer alive while navigating between Start, Game and Settings.
        super.onDestroy()
    }
}
