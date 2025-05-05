package com.achelmas.numart

import android.util.Log

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

class MatchGameActivity : AppCompatActivity() {


    private lateinit var refreshButton: CardView
    private lateinit var expressionView: TextView
    private lateinit var targetView: TextView
    private lateinit var nextBtnLinearLayout: LinearLayout
    private lateinit var refreshBtnLinearLayout: LinearLayout
    private lateinit var nextButton: CardView
    private lateinit var refreshButtonWithText: CardView

    private var currentResult: String? = null
    private var currentOperation: String? = null
    private var target:  String? = null
    private var targetNumber: Int = 0
    private var numbers = mutableListOf<Int>()
    private var usedNumbers = mutableSetOf<Int>()
    private val history = StringBuilder()
    private lateinit var arFragment: ArFragment

    // private lateinit var sceneView: ArSceneView
    private lateinit var konfettiView: KonfettiView

    // Numbers
    private var number1: Int = 0
    private var number2: Int = 0
    private var number3: Int = 0
    private var number4: Int = 0

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


        var bundle: Bundle = intent.extras!!
        if(bundle != null) {
            target = bundle.getString("Target").toString()
            targetNumber = bundle.getString("Target Number")!!.toInt()
            number1 = bundle.getString("Number1")!!.toInt()
            number2 = bundle.getString("Number2")!!.toInt()
            number3 = bundle.getString("Number3")!!.toInt()
            number4 = bundle.getString("Number4")!!.toInt()
        }

        //sceneView = findViewById(R.id.arSceneViewId)
        arFragment = supportFragmentManager.findFragmentById(R.id.arSceneViewId) as ArFragment

        initializeGame()

        // Operation buttons

        refreshButton.setOnClickListener {
            finish()
            startActivity(intent)
        }

    }

    // ----------------------------------------------------------------------------------------------

    private fun add3DNumberButton(
        modelUri: String, // 3D model URI (e.g., "models/number24.sfb")
        position: Vector3, // // Model's position
        number: Int, // Number it represents
        onClick: (String) -> Unit // Tıklanınca yapılacak işlem
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
                    this.worldScale = Vector3(0.0025f, 0.0025f, 0.0025f)
                }

                arFragment.arSceneView.scene.addChild(node)

                // Set a tap listener to handle user interaction with the 3D model
                node.setOnTapListener { _, _ -> onClick(modelUri.split(".").first()) }

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
            Vector3(-1.0f, 0.5f, -1.5f), // Sol üst
            Vector3(1.0f, 0.5f, -1.5f),  // Sağ üst
            Vector3(-0.5f, -0.5f, -1.0f), // Sol alt
            Vector3(1.0f, -0.5f, -2.0f) ,  // Sağ alt
            Vector3(-3.0f, -0.5f, -2.0f) ,  // Sağ alt
            Vector3(0.5f, 0.5f, -2.0f) ,  // Sağ alt

        )

        // Her bir sayıyı ve pozisyonunu ekleyelim
        val numbersAndModels = listOf(
            Triple("models/number7.glb", positions[0], 5),   // Üstte
            Triple("models/Eiffel.glb", positions[1], 2),   // Altta
            Triple("models/Eiffel.glb", positions[2], 2), // Solda

            Triple("models/pyramid.glb", positions[3], 1) , // Sağda
            Triple("models/numberp.glb", positions[4], 45)  ,// Sağda
            Triple("models/Ayasofya.glb", positions[5], 23)  ,// Sağda

        )

        for ((model, position, number) in numbersAndModels) {
            add3DNumberButton(
                modelUri = model,
                position = position,
                number = number
            ) { selectedNumber ->
                onNumberSelected(selectedNumber)
            }
        }
    }


    //GAME START---------------------------------------------------------------
    private fun initializeGame() {

        addNumberButtons()


        // Predefined numbers
        numbers.clear()
        usedNumbers.clear()
        numbers.addAll(listOf(number1, number2, number3, number4))

        targetView.text = "$target"
        targetView.setTextColor(Color.BLACK)

        // Reset game state
        currentResult = null
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
    private fun onNumberSelected(number: String) {
        if (currentResult == null ) {
            currentResult= number
            Toast.makeText(this, "you selected ${number.split("/")[1]}", Toast.LENGTH_SHORT).show()

            return
        }
        else if(currentResult == number){
            onGameOver(true) // Target reached

        }else if (currentResult != number) {
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
                val intent = Intent(this, MainActivity::class.java)
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
