package com.achelmas.numart

import android.util.Log

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer

import android.speech.tts.TextToSpeech

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
//import io.github.sceneview.ar.ArSceneView
//import io.github.sceneview.ar.node.ArModelNode
//import io.github.sceneview.math.Position
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import android.net.Uri
import android.widget.ImageButton

class MatchGameActivity : AppCompatActivity() {


    private lateinit var refreshButton: CardView
    private lateinit var expressionView: TextView
    private lateinit var targetView: TextView
    private lateinit var nextBtnLinearLayout: LinearLayout
    private lateinit var refreshBtnLinearLayout: LinearLayout
    private lateinit var nextButton: CardView
    private lateinit var refreshButtonWithText: CardView

    private var currentResult: String? = null
    private var currentResultNo: Int =100

    private var currentOperation: String? = null
    private var target:  String? = null
    private var targetNumber: Int = 0
    private var numbers = mutableListOf<String?>()
    private var usedNumbers = mutableSetOf<String?>()
    private val history = StringBuilder()
    private lateinit var arFragment: ArFragment

    private lateinit var correctSound: MediaPlayer
    private lateinit var wrongSound: MediaPlayer
    private lateinit var tts: TextToSpeech
    private var isTtsReady = false
    private var isMuted = false
    private lateinit var muteButton: ImageButton
    // private lateinit var sceneView: ArSceneView
    private lateinit var konfettiView: KonfettiView

    // Numbers
    private var shape1: String? = null
    private var shape2: String? = null
    private var shape3: String? = null
    private var shape4: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set language
        LanguageManager.loadLocale(this)

        setContentView(R.layout.activity_match_game)

        // Button and TextView initializations

        refreshButton = findViewById(R.id.gameActivity_refreshButton)
        expressionView = findViewById(R.id.gameActivity_expressionView)
        targetView = findViewById(R.id.gameActivity_targetView)
        konfettiView = findViewById(R.id.konfetti_view)
        nextBtnLinearLayout = findViewById(R.id.gameActivity_nextBtn_linearLayout)
        nextButton = findViewById(R.id.gameActivity_nextButton)
        refreshBtnLinearLayout = findViewById(R.id.gameActivity_refreshBtn_linearLayout)
        refreshButtonWithText = findViewById(R.id.gameActivity_refreshButton_withText)
        muteButton = findViewById(R.id.muteButton)

        var bundle: Bundle = intent.extras!!
        if(bundle != null) {
            target = bundle.getString("Target").toString()
            targetNumber = bundle.getString("Target Number")!!.toInt()
            shape1 = bundle.getString("Number1").toString()
            shape2 = bundle.getString("Number2").toString()
            shape3 = bundle.getString("Number3").toString()
            shape4 = bundle.getString("Number4").toString()
        }

        //sceneView = findViewById(R.id.arSceneViewId)
        arFragment = supportFragmentManager.findFragmentById(R.id.arSceneViewId) as ArFragment

        initializeGame()
        initializeAudio()
        // Operation buttons

        refreshButton.setOnClickListener {
            finish()
            startActivity(intent)
        }
        muteButton.setOnClickListener { toggleMute() }

    }


    private fun initializeAudio() {
        correctSound = MediaPlayer.create(this, R.raw.correct_sound)
        wrongSound = MediaPlayer.create(this, R.raw.wrong_sound)
        tts = TextToSpeech(this) { status -> isTtsReady = status == TextToSpeech.SUCCESS }
    }


    private fun playCorrectFeedback() {
        if (!isMuted) {
            correctSound.start()
            if (isTtsReady) tts.speak("Correct! Great job!", TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun playWrongFeedback() {
        if (!isMuted) {
            wrongSound.start()
            if (isTtsReady) tts.speak("Try again!", TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun playTapSound() {
        if (!isMuted) {
            MediaPlayer.create(this, R.raw.tap_sound).apply {
                start()
                setOnCompletionListener { release() }
            }
        }
    }

    private fun toggleMute() {
        isMuted = !isMuted
        muteButton.setImageResource(
            if (isMuted) R.drawable.ic_volume_off
            else android.R.drawable.ic_lock_silent_mode_off
        )
        if (!isMuted) playTapSound()
    }

    // ----------------------------------------------------------------------------------------------

    private fun add3DNumberButton(
        modelUri: String, // 3D model URI (e.g., "models/number24.sfb")
        position: Vector3, // // Model's position
        number: Int, // Number it represents
        onClick: (String,Int) -> Unit // Tıklanınca yapılacak işlem
    ) {
        ModelRenderable.builder()
            .setSource(this, Uri.parse(modelUri)) // GLB file in assets
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable ->
                val node = Node().apply {
                    this.renderable = renderable
                    this.worldPosition = position // Position of the model in AR space
                    // Adjust the scale of the 3D model
                    this.worldScale = Vector3(0.35f, 0.35f, 0.35f)
                }

                arFragment.arSceneView.scene.addChild(node)
                val fileName = modelUri.substringAfterLast("/").substringBefore(".")

                // Set a tap listener to handle user interaction with the 3D model
                node.setOnTapListener { _, _ -> onClick(fileName,number) }

            }
            .exceptionally { throwable ->
                // Handle any errors that occur when loading the model
                throwable.printStackTrace()
                null
            }
    }


    private fun addNumberButtons() {
        // Modellerin farklı pozisyonlara yerleştirilmesi (x, y, z ekseninde farklılık)
        val positions = listOf(
            Vector3(-0.5f, -0.5f, -0.9f),  // Row 1, Col 1
            Vector3(1f, -0.5f, -0.9f),   // Row 1, Col 2
            Vector3(0.5f, -0.5f, -0.9f),   // Row 1, Col 3

            Vector3(0f, 0.5f, -1.0f),  // Row 2, Col 1



        )
        val gameNumbers = listOf(shape1, shape2, shape3, shape4)
        var x=-0.5f;
        for (i in gameNumbers.indices) {
            val shape = gameNumbers[i]
            val modelPath = "models/$shape.glb"
            val position = positions.getOrNull(i) ?: Vector3(x, 0f, 0f)
            x += 0.1f
            add3DNumberButton(
                modelUri = modelPath,
                position = position,
                number = i
            ) { selectedNumber,noid ->
                onNumberSelected(selectedNumber,noid)
            }
        }

/*
        // Her bir sayıyı ve pozisyonunu ekleyelim
        val numbersAndModels = listOf(
            Triple("models/jesus.glb", positions[1], 2),   // Altta
            Triple("models/Moai Statue.glb", positions[2], 2), // Solda

            Triple("models/pyramid.glb", positions[3], 1) , // Sağda
            Triple("models/eiffel_tower.glb", positions[4], 45)  ,// Sağda
            Triple("models/Ayasofya.glb", positions[5], 23)  ,// Sağda
            Triple("models/Great Wall China.glb", positions[0], 5),   // Üstte
            Triple("models/Coliseo romano.glb", positions[1], 2),   // Altta
            Triple("models/Burj Khalifa.glb", positions[2], 2), // Solda

            Triple("models/BigBen.glb", positions[3], 1) , // Sağda
            Triple("models/Al Aqsa.glb", positions[4], 45)  ,// Sağda
            Triple("models/jesus.glb", positions[5], 23)  ,// Sağda

        )*/
/*
        for ((model, position, number) in numbersAndModels) {
            add3DNumberButton(
                modelUri = model,
                position = position,
                number = number
            ) { selectedNumber ->
                onNumberSelected(selectedNumber)
            }
        }
        */
    }


    //GAME START---------------------------------------------------------------
    private fun initializeGame() {

        addNumberButtons()


        // Predefined numbers
        numbers.clear()
        usedNumbers.clear()
        numbers.addAll(listOf(shape1, shape2, shape3, shape4))

        targetView.text = "$target"
        targetView.setTextColor(Color.BLACK)

        // Reset game state
        currentResult = null
        currentResultNo = 100

        currentOperation = null
        history.clear()
        expressionView.text = resources.getString(R.string.start_by_choosing_number2)

        resetScoreView() // Animasyonu geri döndür


    }



    private fun resetScoreView() {
        val scoreView = findViewById<RelativeLayout>(R.id.gameActivity_scoreView)

        // Eski pozisyona geri dön (y ekseni hareketi)
        val moveDown = ObjectAnimator.ofFloat(scoreView, "translationY", scoreView.translationY, 0f)
        moveDown.duration = 500 // 0.5 saniyede geri dön

        // Ölçeklendirme sıfırlama
        val scaleX = ObjectAnimator.ofFloat(scoreView, "scaleX", scoreView.scaleX, 1f)
        val scaleY = ObjectAnimator.ofFloat(scoreView, "scaleY", scoreView.scaleY, 1f)
        scaleX.duration = 500
        scaleY.duration = 500

        // Animasyonları birleştir ve çalıştır
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(moveDown, scaleX, scaleY)
        animatorSet.start()
    }

    // Number and operation selection===================================
    private fun onNumberSelected(number: String, noid: Int) {
        playTapSound()

        if (currentResult == null ) {
            currentResult= number
            currentResultNo= noid
            Toast.makeText(this, "you selected ${number}", Toast.LENGTH_SHORT).show()
            expressionView.text = number
            usedNumbers.add(number)
            return
        }
        else if(noid == currentResultNo  ){
            Toast.makeText(this, "select another shape", Toast.LENGTH_SHORT).show()
             }

        else if(currentResult == number && noid != currentResultNo  ){
            playCorrectFeedback()

            onGameOver(true) // Target reached

        }else if (currentResult != number) {
            playWrongFeedback()

            onGameOver(false) // Target reached
        }
        /*
        if (usedNumbers.contains(number)) {
            Toast.makeText(this, resources.getString(R.string.number_already_used), Toast.LENGTH_SHORT).show()
            return
        }

        if (currentResult == null) {
            // First number selected
            currentResult = number
            expressionView.text = "$number"
        } else if (currentOperation != null) {
            // Perform operation
            val result = calculateResult(currentResult!!, number, currentOperation!!)
            if (result != null) {
                // Show operation and result
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

        // Check if all numbers are used without reaching the target
        if (usedNumbers.size == numbers.size && currentResult != target) {
            onGameOver(false)
        }*/
    }



    //GAME OVER -----------------------------------------------
    private fun onGameOver(isSuccess: Boolean) {
        // Visibility of buttons

        refreshButton.visibility = View.GONE

        if (isSuccess) {
            nextBtnLinearLayout.visibility = View.VISIBLE

            targetView.setTextColor(ContextCompat.getColor(this , R.color.primaryColor))
            //            targetView.text = "TEBRİKLER! Hedefe ulaştınız! 🎉"
            Toast.makeText(this, resources.getString(R.string.target_reached), Toast.LENGTH_LONG).show()


            // Skor animasyonu başlat
            animateScoreView {
                // Konfetti animasyonu başlat
                startKonfetti()
            }

            nextButton.setOnClickListener {
                val intent = Intent(this, MatchMainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
                finish()
            }

            // Unlock next target when current target is reached
            unlockNextTarget(FirebaseAuth.getInstance().currentUser!!.uid, targetNumber)
        }
        else {
            targetView.setTextColor(Color.RED)
            //            targetView.text = "Hedefe ulaşılamadı! 😞"
            Toast.makeText(this, resources.getString(R.string.target_not_reached), Toast.LENGTH_SHORT).show()

            val scoreView = findViewById<RelativeLayout>(R.id.gameActivity_scoreView)

            // Ekranın merkezine taşımak için Y ekseni animasyonu
            val screenHeight = resources.displayMetrics.heightPixels
            val targetY = screenHeight / 2 - (scoreView.height / 2) // Ortaya yerleşim

            val moveUp = ObjectAnimator.ofFloat(scoreView, "translationY", scoreView.translationY, -targetY.toFloat())
            moveUp.duration = 1000 // Hareket 1 saniye sürecek

            // Büyüme (ölçek) animasyonu
            val scaleX = ObjectAnimator.ofFloat(scoreView, "scaleX", 1f, 2f)
            val scaleY = ObjectAnimator.ofFloat(scoreView, "scaleY", 1f, 2f)
            scaleX.duration = 1000
            scaleY.duration = 1000

            // Animasyonlar tamamlanınca bir işlem başlatmak için Listener
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

    override fun onDestroy() {
        super.onDestroy()
        correctSound.release()
        wrongSound.release()
        tts.shutdown()
    }


    private fun unlockNextTarget(userId: String, completedTarget: Int) {
        val nextTarget = completedTarget + 1
        val userProgressRef = FirebaseDatabase.getInstance().reference
            .child("UserProgress")
            .child(userId)
            .child("MatchEasyLevel")
        Log.d("TAG", userProgressRef.toString())
        // Check if the next target is already unlocked
        userProgressRef.child(nextTarget.toString()).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists() && snapshot.value == true) {
                Toast.makeText(this, resources.getString(R.string.next_target_already_unlocked), Toast.LENGTH_SHORT).show()
            } else {
                // Unlock the next target
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


    private fun animateScoreView(onAnimationEnd: () -> Unit) {
        val scoreView = findViewById<RelativeLayout>(R.id.gameActivity_scoreView)

        // Ekranın merkezine taşımak için Y ekseni animasyonu
        val screenHeight = resources.displayMetrics.heightPixels
        val targetY = screenHeight / 2 - (scoreView.height / 2) // Ortaya yerleşim

        val moveUp = ObjectAnimator.ofFloat(scoreView, "translationY", scoreView.translationY, -targetY.toFloat())
        moveUp.duration = 1000 // Hareket 1 saniye sürecek

        // Büyüme (ölçek) animasyonu
        val scaleX = ObjectAnimator.ofFloat(scoreView, "scaleX", 1f, 2f)
        val scaleY = ObjectAnimator.ofFloat(scoreView, "scaleY", 1f, 2f)
        scaleX.duration = 1000
        scaleY.duration = 1000

        // Animasyonlar tamamlanınca bir işlem başlatmak için Listener
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(moveUp, scaleX, scaleY)
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd() // Animasyon tamamlandığında konfetti başlat
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animatorSet.start()
    }

    private fun startKonfetti() {
        konfettiView.visibility = View.VISIBLE

        // Repeat the animation 3 times
        val repeatCount = 3
        val intervalMillis = 1000L // Delay between animations in milliseconds
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

        // Start the first animation
        handler.post(animationRunnable)
    }
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

    private fun calculateResult(firstNumber: Int, secondNumber: Int, operation: String): Int? {
        return when (operation) {
            "+" -> firstNumber + secondNumber
            "-" -> firstNumber - secondNumber
            "×" -> firstNumber * secondNumber
            "÷" -> if (secondNumber != 0) firstNumber / secondNumber else null
            else -> null
        }
    }
    /*
        private fun checkTarget(result: Int) {
            when {
                result == target -> {
                    onGameOver(true) // Target reached
                }
                result > target -> {
                    Toast.makeText(this, resources.getString(R.string.target_exceeded), Toast.LENGTH_LONG).show()
                }
            }
        }*/


}
