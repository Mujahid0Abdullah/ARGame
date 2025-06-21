package com.achelmas.numart

import android.util.Log
import com.google.ar.sceneform.collision.Box
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Intent
import android.graphics.Color
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
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import android.net.Uri
import com.google.ar.core.TrackingState

/**
 * GameActivity handles the AR-based math game logic,
 * including 3D number/operation rendering, user interaction, feedback, and progress tracking.
 */
class GameActivity : AppCompatActivity() {

    // UI elements
    private lateinit var refreshButton: CardView
    private lateinit var expressionView: TextView
    private lateinit var targetView: TextView
    private lateinit var nextBtnLinearLayout: LinearLayout
    private lateinit var refreshBtnLinearLayout: LinearLayout
    private lateinit var nextButton: CardView
    private lateinit var refreshButtonWithText: CardView
    private lateinit var transformationSystem: TransformationSystem

    // Game state variables
    private var currentResult: Int? = null
    private var currentOperation: String? = null
    private var target: Int = 0
    private var targetNumber: Int = 0
    private var numbers = mutableListOf<Int>()
    private var usedNumbers = mutableSetOf<Int>()
    private val history = StringBuilder()

    private lateinit var konfettiView: KonfettiView
    private lateinit var arFragment: ArFragment

    // Numbers for this round
    private var number1: Int = 0
    private var number2: Int = 0
    private var number3: Int = 0
    private var number4: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set language based on user preference
        LanguageManager.loadLocale(this)
        setContentView(R.layout.activity_game)

        // Initialize UI components
        refreshButton = findViewById(R.id.gameActivity_refreshButton)
        expressionView = findViewById(R.id.gameActivity_expressionView)
        targetView = findViewById(R.id.gameActivity_targetView)
        konfettiView = findViewById(R.id.konfetti_view)
        nextBtnLinearLayout = findViewById(R.id.gameActivity_nextBtn_linearLayout)
        nextButton = findViewById(R.id.gameActivity_nextButton)
        refreshBtnLinearLayout = findViewById(R.id.gameActivity_refreshBtn_linearLayout)
        refreshButtonWithText = findViewById(R.id.gameActivity_refreshButton_withText)

        // Retrieve game data from intent extras
        val bundle: Bundle = intent.extras!!
        if (bundle != null) {
            target = bundle.getString("Target")!!.toInt()
            targetNumber = bundle.getString("Target Number")!!.toInt()
            number1 = bundle.getString("Number1")!!.toInt()
            number2 = bundle.getString("Number2")!!.toInt()
            number3 = bundle.getString("Number3")!!.toInt()
            number4 = bundle.getString("Number4")!!.toInt()
        }

        arFragment = supportFragmentManager.findFragmentById(R.id.arSceneViewId) as ArFragment

        initializeGame()

        // Refresh button restarts the activity
        refreshButton.setOnClickListener {
            finish()
            startActivity(intent)
        }
    }

    /**
     * Adds a 3D operation button to the AR scene.
     * @param modelUri Path to the 3D model file.
     * @param position Position in AR space.
     * @param operation Operation symbol.
     * @param onClick Callback for tap events.
     */
    private fun add3DOperationButton(
        modelUri: String,
        position: Vector3,
        operation: String,
        onClick: (String) -> Unit
    ) {
        ModelRenderable.builder()
            .setSource(this, Uri.parse(modelUri))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable ->
                val node = Node().apply {
                    this.renderable = renderable
                    this.worldPosition = position
                    this.worldScale = Vector3(0.7f, 0.7f, 0.7f)
                }
                arFragment.arSceneView.scene.addChild(node)
                node.setOnTapListener { _, _ -> onClick(operation) }
            }
            .exceptionally {
                it.printStackTrace()
                null
            }
    }

    /** Adds all operation buttons to the AR scene at predefined positions. */
    private fun addOperationButtons() {
        val operations = listOf("+", "-", "×", "÷")
        val modelFiles = listOf(
            "models/plus.glb",
            "models/minus.glb",
            "models/multiply.glb",
            "models/divide.glb"
        )
        val positions = listOf(
            Vector3(-0.5f, -0.3f, -0.2f),
            Vector3(-0.2f, -0.3f, -0.2f),
            Vector3(0.1f, -0.3f, -0.2f),
            Vector3(0.3f, -0.3f, -0.2f)
        )
        for (i in operations.indices) {
            add3DOperationButton(
                modelUri = modelFiles[i],
                position = positions[i],
                operation = operations[i]
            ) { selectedOperation ->
                onOperationSelected(selectedOperation)
            }
        }
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
        onClick: (Int) -> Unit
    ) {
        ModelRenderable.builder()
            .setSource(this, Uri.parse(modelUri))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable ->
                val node = Node().apply {
                    this.renderable = renderable
                    this.worldPosition = position
                    this.worldScale = Vector3(1.0025f, 1.0025f, 1.0025f)
                }
                arFragment.arSceneView.scene.addChild(node)
                node.setOnTapListener { _, _ -> onClick(number) }
            }
            .exceptionally { throwable ->
                throwable.printStackTrace()
                null
            }
    }

    /** Adds all number buttons to the AR scene at predefined positions. */
    private fun addNumberButtons() {
        val positions = listOf(
            Vector3(-0.5f, 0.0f, -0.5f),
            Vector3(0.5f, 0.2f, -0.6f),
            Vector3(-0.5f, -0.2f, -0.8f),
            Vector3(0.5f, -0.3f, -0.7f)
        )
        val gameNumbers = listOf(number1, number2, number3, number4)
        for (i in gameNumbers.indices) {
            val number = gameNumbers[i]
            val modelPath = "models/number$number.glb"
            val position = positions.getOrNull(i) ?: Vector3(0f, 0f, -0.5f)
            add3DNumberButton(
                modelUri = modelPath,
                position = position,
                number = number
            ) { selectedNumber ->
                onNumberSelected(selectedNumber)
            }
        }
    }

    /** Initializes the game state and UI for a new round. */
    private fun initializeGame() {
        addNumberButtons()
        numbers.clear()
        usedNumbers.clear()
        numbers.addAll(listOf(number1, number2, number3, number4))
        targetView.text = "$target"
        targetView.setTextColor(Color.BLACK)
        currentResult = null
        currentOperation = null
        history.clear()
        expressionView.text = resources.getString(R.string.start_by_choosing_number)
        resetScoreView()
        addNumberButtons()
        addOperationButtons()
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
    private fun onNumberSelected(number: Int) {
        if (usedNumbers.contains(number)) {
            Toast.makeText(this, resources.getString(R.string.number_already_used), Toast.LENGTH_SHORT).show()
            return
        }
        if (currentResult == null) {
            currentResult = number
            expressionView.text = "$number"
        } else if (currentOperation != null) {
            val result = calculateResult(currentResult!!, number, currentOperation!!)
            if (result != null) {
                history.append("$currentResult $currentOperation $number = $result\n")
                expressionView.text = history.toString()
                currentResult = result
                currentOperation = null
                checkTarget(result)
            } else {
                Toast.makeText(this, resources.getString(R.string.invalid_operation), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, resources.getString(R.string.select_operation), Toast.LENGTH_SHORT).show()
            return
        }
        usedNumbers.add(number)
        if (usedNumbers.size == numbers.size && currentResult != target) {
            onGameOver(false)
        }
    }

    /**
     * Handles logic when an operation is selected in the AR scene.
     */
    private fun onOperationSelected(operation: String) {
        if (currentResult != null && currentOperation == null) {
            currentOperation = operation
            expressionView.text = "${history}${currentResult} $operation"
        } else if (currentOperation != null) {
            Toast.makeText(this, resources.getString(R.string.operation_already_selected), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, resources.getString(R.string.select_number_first), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Calculates the result of an operation between two numbers.
     * @return The result or null if invalid.
     */
    private fun calculateResult(firstNumber: Int, secondNumber: Int, operation: String): Int? {
        return when (operation) {
            "+" -> firstNumber + secondNumber
            "-" -> firstNumber - secondNumber
            "×" -> firstNumber * secondNumber
            "÷" -> if (secondNumber != 0) firstNumber / secondNumber else null
            else -> null
        }
    }

    /**
     * Checks if the result matches or exceeds the target and triggers game over if needed.
     */
    private fun checkTarget(result: Int) {
        when {
            result == target -> {
                onGameOver(true)
            }
            result > target -> {
                Toast.makeText(this, resources.getString(R.string.target_exceeded), Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Handles game over state, shows feedback, and unlocks next target if successful.
     * @param isSuccess True if the player matched correctly.
     */
    private fun onGameOver(isSuccess: Boolean) {
        refreshButton.visibility = View.GONE
        if (isSuccess) {
            nextBtnLinearLayout.visibility = View.VISIBLE
            targetView.setTextColor(ContextCompat.getColor(this, R.color.primaryColor))
            Toast.makeText(this, resources.getString(R.string.target_reached), Toast.LENGTH_LONG).show()
            animateScoreView { startKonfetti() }
            nextButton.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                finish()
                startActivity(intent)
            }
            unlockNextTarget(FirebaseAuth.getInstance().currentUser!!.uid, targetNumber)
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
            .child("A1EasyLevel")
        Log.d("TAG", userProgressRef.toString())
        userProgressRef.child(nextTarget.toString()).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists() && snapshot.value == true) {
                Toast.makeText(this, resources.getString(R.string.next_target_already_unlocked), Toast.LENGTH_SHORT).show()
            } else {
                userProgressRef.child(nextTarget.toString()).setValue(true)
                    .addOnSuccessListener {
                        // Next target unlocked successfully
                    }
                    .addOnFailureListener {
                        // Handle failure
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
        // Release resources here if you add any (e.g., MediaPlayer, Handler)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Optionally release resources here if needed
    }
}