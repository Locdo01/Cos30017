package com.example.gymnasticsapp

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class GymActivity : AppCompatActivity() {

    companion object {
        const val TAG = "GymApp"
        const val KEY_SCORE = "score"
        const val KEY_ELEMENT = "element"
        const val KEY_DEDUCTION = "deduction"
        const val KEY_COMPLETED = "completed"

        const val TOTAL_ELEMENTS = 10
        const val MAX_SCORE = 20

        const val COLOR_BASIC = 0xFF2878D0.toInt()
        const val COLOR_INTERMEDIATE = 0xFF2E9561.toInt()
        const val COLOR_ADVANCED = 0xFFEF6A3A.toInt()
    }

    // Game state
    var score = 0
    var currentElement = 1
    var deductionTaken = false
    var routineCompleted = false

    // Views from activity_gym.xml
    lateinit var scoreText: TextView
    lateinit var zoneText: TextView
    lateinit var elementText: TextView
    lateinit var progressCount: TextView
    lateinit var statusText: TextView
    lateinit var finalScoreText: TextView

    lateinit var progressBar: ProgressBar
    lateinit var performBtn: Button
    lateinit var deductionBtn: Button
    lateinit var resetBtn: Button
    lateinit var homeBtn: Button
    lateinit var settingsBtn: Button
    lateinit var scoreCard: LinearLayout
    lateinit var completeLayout: LinearLayout

    // Sound effects
    lateinit var soundPool: SoundPool
    var clickSound = 0
    var performSound = 0
    var deductSound = 0
    var resetSound = 0
    var completeSound = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gym)

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

        bindViews()
        loadSoundEffects()
        restoreGameState(savedInstanceState)
        setButtonListeners()
        updateUI()
        updateBgMusic()

        Log.d(TAG, "Game screen created")
    }

    fun bindViews() {
        scoreText = findViewById(R.id.scoreText)
        zoneText = findViewById(R.id.zoneText)
        elementText = findViewById(R.id.elementText)
        progressCount = findViewById(R.id.progressCount)
        statusText = findViewById(R.id.statusText)
        finalScoreText = findViewById(R.id.finalScoreText)

        progressBar = findViewById(R.id.progressBar)
        performBtn = findViewById(R.id.btnPerform)
        deductionBtn = findViewById(R.id.btnDeduction)
        resetBtn = findViewById(R.id.btnReset)
        homeBtn = findViewById(R.id.homeBtn)
        settingsBtn = findViewById(R.id.settingsBtn)
        scoreCard = findViewById(R.id.scoreCard)
        completeLayout = findViewById(R.id.completeLayout)

        // Use SoundPool for app effects; avoid the system click sound as well.
        listOf(performBtn, deductionBtn, resetBtn, homeBtn, settingsBtn)
            .forEach { it.isSoundEffectsEnabled = false }
    }

    fun restoreGameState(state: Bundle?) {
        if (state == null) return

        score = state.getInt(KEY_SCORE, 0).coerceIn(0, MAX_SCORE)
        currentElement = state.getInt(KEY_ELEMENT, 1).coerceIn(1, TOTAL_ELEMENTS)
        deductionTaken = state.getBoolean(KEY_DEDUCTION, false)
        routineCompleted = state.getBoolean(KEY_COMPLETED, false)

        Log.d(TAG, "State restored: score=$score, element=$currentElement")
    }

    fun setButtonListeners() {
        performBtn.setOnClickListener { perform() }
        deductionBtn.setOnClickListener { deduct() }
        resetBtn.setOnClickListener { resetRoutine() }
        homeBtn.setOnClickListener { goHome() }
        settingsBtn.setOnClickListener { openSettings() }
    }

    // Game rules
    fun perform() {
        if (routineCompleted || deductionTaken) {
            Log.w(TAG, "Perform rejected: routine is locked")
            return
        }

        val element = currentElement
        val points = getPointsForElement(element)
        val oldScore = score

        playEffect(performSound)
        score = (score + points).coerceAtMost(MAX_SCORE)

        if (element == TOTAL_ELEMENTS) {
            routineCompleted = true
            currentElement = TOTAL_ELEMENTS
            playEffect(completeSound)
        } else {
            currentElement++
        }

        Log.d(TAG, "Element $element performed: +$points, score $oldScore -> $score")
        updateUI()
        if (!routineCompleted) animateScore()
    }

    fun deduct() {
        if (currentElement == 1 || routineCompleted || deductionTaken) {
            Log.w(TAG, "Deduction rejected: not available")
            return
        }

        val oldScore = score
        playEffect(deductSound)
        score = (score - 2).coerceAtLeast(0)
        deductionTaken = true

        Log.d(TAG, "Deduction applied: score $oldScore -> $score; routine locked")
        updateUI()
        animateScore()
    }

    fun resetRoutine() {
        playEffect(resetSound)

        score = 0
        currentElement = 1
        deductionTaken = false
        routineCompleted = false

        Log.d(TAG, "Routine reset")
        updateUI()
    }

    fun getPointsForElement(element: Int): Int = when (element) {
        in 1..3 -> 1
        in 4..7 -> 2
        else -> 3
    }

    fun getZone(element: Int): String = when (element) {
        in 1..3 -> getString(R.string.basic_label)
        in 4..7 -> getString(R.string.intermediate_label)
        else -> getString(R.string.advanced_label)
    }

    fun getZoneColor(element: Int): Int = when (element) {
        in 1..3 -> COLOR_BASIC
        in 4..7 -> COLOR_INTERMEDIATE
        else -> COLOR_ADVANCED
    }

    // Update every dynamic view from the current game state.
    fun updateUI() {
        val displayedElement = if (routineCompleted) {
            TOTAL_ELEMENTS
        } else {
            currentElement.coerceIn(1, TOTAL_ELEMENTS)
        }

        // Recalculate this on every update. Do not cache it as a class-level val.
        val completed = if (routineCompleted) {
            TOTAL_ELEMENTS
        } else {
            (currentElement - 1).coerceIn(0, TOTAL_ELEMENTS)
        }

        val zoneColor = getZoneColor(displayedElement)

        // Score and difficulty zone
        scoreText.text = score.toString()
        scoreText.setTextColor(zoneColor)
        zoneText.text = getZone(displayedElement)
        zoneText.backgroundTintList = ColorStateList.valueOf(zoneColor)

        // Number of elements completed: starts at 0/10 and advances after Perform.
        elementText.text = getString(
            R.string.element_position,
            completed,
            TOTAL_ELEMENTS
        )

        progressCount.text = getString(
            R.string.progress_count,
            completed,
            TOTAL_ELEMENTS
        )
        progressBar.max = TOTAL_ELEMENTS
        progressBar.progress = completed
        progressBar.progressTintList = ColorStateList.valueOf(zoneColor)

        // Show points awarded by the next Perform action.
        performBtn.text = getString(
            R.string.btn_perform_points,
            getPointsForElement(displayedElement)
        )

        // Enable actions only when allowed by the rules.
        performBtn.isEnabled = !routineCompleted && !deductionTaken
        deductionBtn.isEnabled = completed > 0 && !routineCompleted && !deductionTaken
        resetBtn.isEnabled = true

        performBtn.alpha = if (performBtn.isEnabled) 1f else 0.45f
        deductionBtn.alpha = if (deductionBtn.isEnabled) 1f else 0.45f
        resetBtn.alpha = 1f

        statusText.visibility = if (deductionTaken && !routineCompleted) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Show completion message when the 10th element is performed.
        scoreCard.visibility = if (routineCompleted) View.GONE else View.VISIBLE
        completeLayout.visibility = if (routineCompleted) View.VISIBLE else View.GONE
        if (routineCompleted) {
            finalScoreText.text = getString(R.string.final_score_value, score, MAX_SCORE)
        }

        Log.d(
            TAG,
            "UI updated: score=$score, completed=$completed/$TOTAL_ELEMENTS, " +
                    "deduction=$deductionTaken, complete=$routineCompleted"
        )
    }

    fun animateScore() {
        scoreText.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(120)
            .withEndAction {
                scoreText.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .start()
            }
            .start()
    }

    // Sound effects
    fun loadSoundEffects() {
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()

        clickSound = soundPool.load(this, R.raw.music_click, 1)
        performSound = soundPool.load(this, R.raw.music_perform, 1)
        deductSound = soundPool.load(this, R.raw.music_deduct, 1)
        resetSound = soundPool.load(this, R.raw.music_reset, 1)
        completeSound = soundPool.load(this, R.raw.music_complete, 1)
    }

    fun playEffect(soundId: Int) {
        if (::soundPool.isInitialized && SettingsActivity.soundEffect && soundId != 0) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun updateBgMusic() {
        val player = StartActivity.bgPlayer ?: return

        if (SettingsActivity.backgroundMusic) {
            if (!player.isPlaying) player.start()
        } else if (player.isPlaying) {
            player.pause()
        }
    }

    // Navigation
    fun goHome() {
        Log.d(TAG, "Home clicked")
        playEffect(clickSound)
        finish()
    }

    fun openSettings() {
        Log.d(TAG, "Settings clicked")
        playEffect(clickSound)
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    // Locale support
    fun applyLocale(context: Context, language: String): Context {
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(applyLocale(newBase, SettingsActivity.languageCode))
    }

    override fun onResume() {
        super.onResume()

        val currentLanguage = resources.configuration.locales[0].language
        if (currentLanguage != SettingsActivity.languageCode) {
            Log.d(TAG, "Refreshing Game screen language: ${SettingsActivity.languageCode}")
            recreate()
            return
        }

        updateBgMusic()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_SCORE, score)
        outState.putInt(KEY_ELEMENT, currentElement)
        outState.putBoolean(KEY_DEDUCTION, deductionTaken)
        outState.putBoolean(KEY_COMPLETED, routineCompleted)
        super.onSaveInstanceState(outState)
        Log.d(TAG, "Game state saved")
    }

    override fun onDestroy() {
        if (::soundPool.isInitialized) soundPool.release()
        super.onDestroy()
    }
}
