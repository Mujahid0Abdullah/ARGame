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
import android.speech.tts.TextToSpeech
import android.widget.ImageButton
import java.util.Locale


class DiscoverGameActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private lateinit var correctSound: MediaPlayer
    private lateinit var wrongSound: MediaPlayer
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
    private var question:  String? = null

    private var targetNumber: Int = 0
    private var numbers = mutableListOf<String?>()
    private var usedNumbers = mutableSetOf<String?>()
    private val history = StringBuilder()
    private lateinit var arFragment: ArFragment
    private lateinit var muteButton: ImageButton
    private var isTtsReady = false
    private var isMuted = false
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
            question = bundle.getString("Question").toString()
            target = bundle.getString("Target").toString()
            targetNumber = bundle.getString("Target Number")!!.toInt()
            shape1 = bundle.getString("Shape1").toString()
            shape2 = bundle.getString("Shape2").toString()
            shape3 = bundle.getString("Shape3").toString()
            shape4 = bundle.getString("Shape4").toString()
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val languageCode = LanguageManager.loadSelectedLanguage(this)

                val locale = when (languageCode) {
                    "tr" -> Locale("tr", "TR")
                    "en" -> Locale.US
                    else -> Locale.US
                }
                val result = tts.setLanguage(locale)
                Log.d("TAG", result.toString()+"lang in stsate "+languageCode)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
                } else {
                    question?.let { speak(it) }
                }
            }
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
        expressionView.setOnClickListener {
            question?.let { speak(it) }
        }

        muteButton.setOnClickListener { toggleMute() }


    }
    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        Log.d("TAG", "giiili "+text)

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
                    this.worldScale = Vector3(1.25f, 1.25f, 1.25f)
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
            Vector3(0.0f, -0.5f, -0.5f), // Left
                    Vector3(0.2f, -0.5f, -1.0f), // Right
        Vector3(-0.5f, -0.5f, -1.0f), // Front left
        Vector3(-0.2f, -0.5f, -0.7f), // Front right
        Vector3(-0.1f, -0.5f, -1.0f), // Far left
        Vector3(0.5f, -0.5f, -0.5f) // Center


        )
        val gameNumbers = listOf(shape1, shape2, shape3, shape4)
        var x=-0.5f;
        for (i in gameNumbers.indices) {
            val shape = gameNumbers[i]
            val modelPath = "models/$shape.glb"
            val position = positions.getOrNull(i) ?: Vector3(x, -0.3f, -0.5f)
            x += 0.1f
            add3DNumberButton(
                modelUri = modelPath,
                position = position,
                number = i
            ) { selectedNumber,noid ->
                onNumberSelected(selectedNumber,noid)
            }
        }


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
        targetView.visibility = View.GONE


        // Reset game state
        currentResult = null
        currentResultNo = 100

        currentOperation = null
        history.clear()
        expressionView.text = question
        question?.let { speak(it) }


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
        if (number == target ) {
            Log.d("TAG", "true")
            playCorrectFeedback()

            Toast.makeText(this, "you selected ${number}", Toast.LENGTH_SHORT).show()
//            expressionView.text = number
            usedNumbers.add(number)
            onGameOver(true) // Target reached

            return
        }

        else  {
            Log.d("TAG", "false "+number+" "+target)
            playWrongFeedback()

            onGameOver(false) // Target reached
        }

    }

    private fun initializeAudio() {
        correctSound = MediaPlayer.create(this, R.raw.correct_sound)
        wrongSound = MediaPlayer.create(this, R.raw.wrong_sound)
    }


    private fun playCorrectFeedback() {
        if (!isMuted) {
            correctSound.start()
        }
    }

    private fun playWrongFeedback() {
        if (!isMuted) {
            wrongSound.start()
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
            unlockNextTarget(FirebaseAuth.getInstance().currentUser!!.uid, targetNumber)
            nextButton.setOnClickListener {
                val intent = Intent(this, DiscoverLvlActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT

                finish()
                startActivity(intent)
            }

            // Unlock next target when current target is reached

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

    private fun unlockNextTarget(userId: String, completedTarget: Int) {
        val nextTarget = completedTarget + 1
        val userProgressRef = FirebaseDatabase.getInstance().reference
            .child("UserProgress")
            .child(userId)
            .child("DiscoverEasyLevel")
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




}
