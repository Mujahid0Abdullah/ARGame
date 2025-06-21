package com.achelmas.numart

import android.util.Log
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.ImageButton
import java.util.Locale

/**
 * DiscoverGameActivity handles the AR-based discover game logic,
 * including 3D number rendering, user interaction, feedback, and progress tracking.
 */
class DiscoverGameActivity : AppCompatActivity() {

    // Audio and feedback
    private lateinit var tts: TextToSpeech
    private lateinit var correctSound: MediaPlayer
    private lateinit var wrongSound: MediaPlayer

    // UI elements
    private lateinit var refreshButton: CardView
    private lateinit var expressionView: TextView
    private lateinit var targetView: TextView
    private lateinit var nextBtnLinearLayout: LinearLayout
    private lateinit var refreshBtnLinearLayout: LinearLayout
    private lateinit var nextButton: CardView
    private lateinit var refreshButtonWithText: CardView
    private lateinit var muteButton: ImageButton
    private lateinit var konfettiView: KonfettiView

    // Game state variables
    private var currentResult: String? = null
    private var currentResultNo: Int = 100
    private var currentOperation: String? = null
    private var target: String? = null
    private var question: String? = null
    private var targetNumber: Int = 0
    private var numbers = mutableListOf<String?>()
    private var usedNumbers = mutableSetOf<String?>()
    private val history = StringBuilder()
    private lateinit var arFragment: ArFragment
    private var isTtsReady = false
    private var isMuted = false
    private var isGameOver = false

    // 3D model names for numbers
    private var shape1: String? = null
    private var shape2: String? = null
    private var shape3: String? = null
    private var shape4: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set language based on user preference
        LanguageManager.loadLocale(this)
        setContentView(R.layout.activity_match_game)

        // Initialize UI components
        refreshButton = findViewById(R.id.gameActivity_refreshButton)
        expressionView = findViewById(R.id.gameActivity_expressionView)
        targetView = findViewById(R.id.gameActivity_targetView)
        konfettiView = findViewById(R.id.konfetti_view)
        nextBtnLinearLayout = findViewById(R.id.gameActivity_nextBtn_linearLayout)
        nextButton = findViewById(R.id.gameActivity_nextButton)
        refreshBtnLinearLayout = findViewById(R.id.gameActivity_refreshBtn_linearLayout)
        refreshButtonWithText = findViewById(R.id.gameActivity_refreshButton_withText)
        muteButton = findViewById(R.id.muteButton)

        // Retrieve game data from intent extras
        val bundle: Bundle = intent.extras!!
        if (bundle != null) {
            question = bundle.getString("Question").toString()
            target = bundle.getString("Target").toString()
            targetNumber = bundle.getString("Target Number")!!.toInt()
            shape1 = bundle.getString("Shape1").toString()
            shape2 = bundle.getString("Shape2").toString()
            shape3 = bundle.getString("Shape3").toString()
            shape4 = bundle.getString("Shape4").toString()
        }

        // Initialize TextToSpeech for question reading
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val languageCode = LanguageManager.loadSelectedLanguage(this)
                val locale = when (languageCode) {
                    "tr" -> Locale("tr", "TR")
                    "en" -> Locale.US
                    else -> Locale.US
                }
                val result = tts.setLanguage(locale)
                Log.d("TAG", result.toString() + "lang in state " + languageCode)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
                } else {
                    question?.let { speak(it) }
                }
            }
        }

        arFragment = supportFragmentManager.findFragmentById(R.id.arSceneViewId) as ArFragment

        initializeGame()
        initializeAudio()

        // Button listeners for refresh, question repeat, and mute
        refreshButton.setOnClickListener {
            finish()
            startActivity(intent)
        }
        expressionView.setOnClickListener {
            question?.let { speak(it) }
        }
        muteButton.setOnClickListener { toggleMute() }
    }

    /** Speaks the given text using TTS. */
    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        Log.d("TAG", "speak: $text")
    }

    /**
     * Adds a 3D number button to the AR scene.
     * @param modelUri Path to the 3D model file.
     * @param position Position in AR space.
     * @param number Number identifier.
     * @param onClick Callback for tap events.
     */
    private fun add3DNumberButton(
        modelUri: String,
        position: Vector3,
        number: Int,
        onClick: (String, Int) -> Unit
    ) {
        ModelRenderable.builder()
            .setSource(this, Uri.parse(modelUri))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable ->
                val node = Node().apply {
                    this.renderable = renderable
                    this.worldPosition = position
                    this.worldScale = Vector3(3.0f, 3.0f, 3.0f)
                }
                arFragment.arSceneView.scene.addChild(node)
                val fileName = modelUri.substringAfterLast("/").substringBefore(".")
                node.setOnTapListener { _, _ -> onClick(fileName, number) }
            }
            .exceptionally { throwable ->
                throwable.printStackTrace()
                null
            }
    }

    /** Adds all number buttons to the AR scene at predefined positions. */
    private fun addNumberButtons() {
        val positions = listOf(
            Vector3(0.2f, -0.5f, -0.5f),
            Vector3(0.2f, -0.5f, -1.0f),
            Vector3(-0.5f, -0.5f, -1.0f),
            Vector3(-0.3f, -0.5f, -0.7f),
            Vector3(-0.1f, -0.5f, -1.0f),
            Vector3(0.5f, -0.5f, -0.5f)
        )
        val gameNumbers = listOf(shape1, shape2, shape3, shape4)
        var x = -0.5f
        for (i in gameNumbers.indices) {
            val shape = gameNumbers[i]
            val modelPath = "models/$shape.glb"
            val position = positions.getOrNull(i) ?: Vector3(x, -0.3f, -0.5f)
            x += 0.1f
            add3DNumberButton(
                modelUri = modelPath,
                position = position,
                number = i
            ) { selectedNumber, noid ->
                onNumberSelected(selectedNumber, noid)
            }
        }
    }

    /** Initializes the game state and UI for a new round. */
    private fun initializeGame() {
        addNumberButtons()
        numbers.clear()
        usedNumbers.clear()
        numbers.addAll(listOf(shape1, shape2, shape3, shape4))
        targetView.text = "$target"
        targetView.setTextColor(Color.BLACK)
        targetView.visibility = View.GONE
        currentResult = null
        currentResultNo = 100
        currentOperation = null
        history.clear()
        expressionView.text = question
        question?.let { speak(it) }
        resetScoreView()
    }

    /** Resets the score view's position and scale with animation. */
    private fun resetScoreView() {
        val scoreView = findViewById<RelativeLayout>(R.id.gameActivity_scoreView)
        val moveDown = ObjectAnimator.ofFloat(scoreView, "translationY", scoreView.translationY, 0f)
        moveDown.duration = 500
        val scaleX = ObjectAnimator.ofFloat(scoreView, "scaleX", scoreView.scaleX, 1f)
        val scaleY = ObjectAnimator.ofFloat(scoreView, "scaleY", scoreView.scaleY, 1f)
        scaleX.duration = 500
        scaleY.duration = 500
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(moveDown, scaleX, scaleY)
        animatorSet.start()
    }

    /**
     * Handles logic when a number is selected in the AR scene.
     * Updates state, checks for correct/incorrect match, and triggers feedback.
     */
    private fun onNumberSelected(number: String, noid: Int) {
        if (isGameOver) return
        if (number == target) {
            Log.d("TAG", "true")
            playCorrectFeedback()
            Toast.makeText(this, "you selected $number", Toast.LENGTH_SHORT).show()
            usedNumbers.add(number)
            onGameOver(true)
            return
        } else {
            Log.d("TAG", "false $number $target")
            playWrongFeedback()
            onGameOver(false)
        }
    }

    /** Initializes audio feedback. */
    private fun initializeAudio() {
        correctSound = MediaPlayer.create(this, R.raw.correct_sound)
        wrongSound = MediaPlayer.create(this, R.raw.wrong_sound)
    }

    /** Plays correct answer feedback (sound). */
    private fun playCorrectFeedback() {
        if (!isMuted) {
            correctSound.start()
        }
    }

    /** Plays wrong answer feedback (sound). */
    private fun playWrongFeedback() {
        if (!isMuted) {
            wrongSound.start()
        }
    }

    /** Plays a tap sound effect for UI feedback. */
    private fun playTapSound() {
        if (!isMuted) {
            MediaPlayer.create(this, R.raw.tap_sound).apply {
                start()
                setOnCompletionListener { release() }
            }
        }
    }

    /** Toggles mute state and updates the mute button icon. */
    private fun toggleMute() {
        isMuted = !isMuted
        muteButton.setImageResource(
            if (isMuted) R.drawable.ic_volume_off
            else android.R.drawable.ic_lock_silent_mode_off
        )
        if (!isMuted) playTapSound()
    }

    /**
     * Handles game over state, shows feedback, and unlocks next target if successful.
     * @param isSuccess True if the player matched correctly.
     */
    private fun onGameOver(isSuccess: Boolean) {
        isGameOver = true
        refreshButton.visibility = View.GONE
        if (isSuccess) {
            nextBtnLinearLayout.visibility = View.VISIBLE
            targetView.setTextColor(ContextCompat.getColor(this, R.color.primaryColor))
            Toast.makeText(this, resources.getString(R.string.target_reached), Toast.LENGTH_LONG).show()
            animateScoreView { startKonfetti() }
            unlockNextTarget(FirebaseAuth.getInstance().currentUser!!.uid, targetNumber)
            nextButton.setOnClickListener {
                val intent = Intent(this, DiscoverLvlActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                finish()
                startActivity(intent)
            }
        } else {
            targetView.setTextColor(Color.RED)
            Toast.makeText(this, resources.getString(R.string.target_not_reached), Toast.LENGTH_SHORT).show()
            val scoreView = findViewById<RelativeLayout>(R.id.gameActivity_scoreView)
            val screenHeight = resources.displayMetrics.heightPixels
            val targetY = screenHeight / 2 - (scoreView.height / 2)
            val moveUp = ObjectAnimator.ofFloat(scoreView, "translationY", scoreView.translationY, -targetY.toFloat())
            moveUp.duration = 1000
            val scaleX = ObjectAnimator.ofFloat(scoreView, "scaleX", 1f, 2f)
            val scaleY = ObjectAnimator.ofFloat(scoreView, "scaleY", 1f, 2f)
            scaleX.duration = 1000
            scaleY.duration = 1000
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(moveUp, scaleX, scaleY)
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            animatorSet.start()
            refreshBtnLinearLayout.visibility = View.VISIBLE
            refreshButtonWithText.setOnClickListener {
                finish()
                startActivity(intent)
            }
        }
    }

    /**
     * Unlocks the next target for the user in Firebase after a successful match.
     * @param userId The user's Firebase UID.
     * @param completedTarget The number of the completed target.
     */
    private fun unlockNextTarget(userId: String, completedTarget: Int) {
        val nextTarget = completedTarget + 1
        val userProgressRef = FirebaseDatabase.getInstance().reference
            .child("UserProgress")
            .child(userId)
            .child("DiscoverEasyLevel")
        Log.d("TAG", userProgressRef.toString())
        userProgressRef.child(nextTarget.toString()).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists() && snapshot.value == true) {
                Toast.makeText(this, resources.getString(R.string.next_target_already_unlocked), Toast.LENGTH_SHORT).show()
            } else {
                userProgressRef.child(nextTarget.toString()).setValue(true)
                    .addOnSuccessListener {
                        Toast.makeText(this, resources.getString(R.string.next_target_unlocked), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, resources.getString(R.string.next_target_unlock_failed), Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    /**
     * Animates the score view to the center and scales it up, then triggers a callback.
     * @param onAnimationEnd Callback to run after animation completes.
     */
    private fun animateScoreView(onAnimationEnd: () -> Unit) {
        val scoreView = findViewById<RelativeLayout>(R.id.gameActivity_scoreView)
        val screenHeight = resources.displayMetrics.heightPixels
        val targetY = screenHeight / 2 - (scoreView.height / 2)
        val moveUp = ObjectAnimator.ofFloat(scoreView, "translationY", scoreView.translationY, -targetY.toFloat())
        moveUp.duration = 1000
        val scaleX = ObjectAnimator.ofFloat(scoreView, "scaleX", 1f, 2f)
        val scaleY = ObjectAnimator.ofFloat(scoreView, "scaleY", 1f, 2f)
        scaleX.duration = 1000
        scaleY.duration = 1000
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(moveUp, scaleX, scaleY)
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) { onAnimationEnd() }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animatorSet.start()
    }

    /** Starts the konfetti animation for celebration. */
    private fun startKonfetti() {
        konfettiView.visibility = View.VISIBLE
        val repeatCount = 3
        val intervalMillis = 1000L
        var currentRepeat = 0
        val handler = android.os.Handler(mainLooper)
        val animationRunnable = object : Runnable {
            override fun run() {
                if (currentRepeat < repeatCount) {
                    konfettiView.start(
                        Party(
                            speed = 0f,
                            maxSpeed = 30f,
                            damping = 0.9f,
                            spread = 360,
                            colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW),
                            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                            position = nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.3)
                        )
                    )
                    currentRepeat++
                    handler.postDelayed(this, intervalMillis)
                }
            }
        }
        handler.post(animationRunnable)
    }

    /** Releases resources on activity destroy. */
    override fun onDestroy() {
        super.onDestroy()
        try { correctSound.release() } catch (_: Exception) {}
        try { wrongSound.release() } catch (_: Exception) {}
        try { tts.shutdown() } catch (_: Exception) {}
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}