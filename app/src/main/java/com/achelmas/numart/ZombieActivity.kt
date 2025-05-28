package com.achelmas.numart

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.util.Log

import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.ar.sceneform.ux.TransformationSystem
//import io.github.sceneview.SceneView
//import io.github.sceneview.ar.node.ArModelNode
//import io.github.sceneview.ar.ArSceneView
//import io.github.sceneview.math.Position
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit
import android.view.MotionEvent
import com.google.ar.sceneform.animation.ModelAnimator

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlin.random.Random

class ZombieActivity : AppCompatActivity() {
    private lateinit var startButton: Button

    private lateinit var arFragment: ArFragment
    private var zombieRenderable: ModelRenderable? = null
    private var animator: ModelAnimator? = null
    private var gameStarted = false
    private var score = 0
    private var lives = 3
    private lateinit var targetView: TextView
    private lateinit var expressionView: TextView

    private lateinit var scoreTextView: TextView
    private lateinit var livesTextView: TextView

    private val handler = Handler()
    private val zombies = mutableListOf<Node>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zombie)

        arFragment = supportFragmentManager.findFragmentById(R.id.arSceneViewId) as ArFragment
        scoreTextView = findViewById(R.id.gameActivity_expressionView)
        livesTextView = findViewById(R.id.gameActivity_targetView)


        loadZombieModel()
        updateUI()
        startButton = findViewById(R.id.startButton)


        startButton.setOnClickListener {
            if (!gameStarted) {
                gameStarted = true
                startButton.visibility = View.GONE
                Toast.makeText(this, "Game Started!", Toast.LENGTH_SHORT).show()

                // Ekran ortasına ilk zombi yerleştir
                val frame = arFragment.arSceneView.arFrame
                val hitResult = frame?.hitTest(frame.screenCenter().x, frame.screenCenter().y)?.firstOrNull()
                if (hitResult != null) {
                    spawnZombie(hitResult)
                }

                startZombieSpawner()
            }
        }



        // Plane'e tıklayınca ilk zombi üretimi başlasın
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _: MotionEvent ->
            if (zombieRenderable == null) return@setOnTapArPlaneListener
            Log.d("ZombieActivity", "arFragment.setOnTapArPlaneListener: Plane tapped, spawning zombie")

            spawnZombie(hitResult)
            startZombieSpawner()
        }
    }

    private fun loadZombieModel() {
        Log.d("ZombieActivity", "loadZombieModel: Loading model...")

        ModelRenderable.builder()
            .setSource(this, Uri.parse("models/zombie_commoner.glb"))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable ->
                Log.d("ZombieActivity", "loadZombieModel: Model loaded successfully")

                zombieRenderable = renderable
                // If your renderable has animations, play the first one

            }
            .exceptionally {
                Log.e("ZombieActivity", "loadZombieModel: Model failed to load", it)

                Toast.makeText(this, "Zombie modeli yüklenemedi", Toast.LENGTH_SHORT).show()
                null
            }

    }

    private fun spawnZombie(hitResult: HitResult) {
        Log.d("ZombieActivity", "spawnZombie: Spawning zombie")

        val anchorNode = AnchorNode(hitResult.createAnchor())
        anchorNode.setParent(arFragment.arSceneView.scene)

        val zombieNode = Node()
        zombieNode.setParent(anchorNode)
        zombieNode.renderable = zombieRenderable
        zombieNode.worldScale = Vector3(0.005f, 0.005f, 0.005f)


        // Rastgele konumla yerleştir
        val randomX = Random.nextFloat() * 2f - 1f
        val randomZ = Random.nextFloat() * -2f - 1f
        zombieNode.localPosition = Vector3(randomX, 0f, randomZ)

        // Tıklayınca yok et
        zombieNode.setOnTapListener { _, _ ->
            arFragment.arSceneView.scene.removeChild(anchorNode)
            zombies.remove(zombieNode)
            score += 10
            updateUI()
        }

        zombies.add(zombieNode)
        moveZombie(zombieNode)
    }
    // Call this function after spawning a zombie
    private fun scheduleZombieMovement(zombie: Node) {
        handler.post(object : Runnable {
            override fun run() {
                if (!zombies.contains(zombie)) return
                moveZombie(zombie)
                handler.postDelayed(this, 3000) // Run every 3 seconds
            }
        })
    }

    private fun moveZombie(zombie: Node) {
        Log.d("ZombieActivity", "moveZombie: Moving zombie")

        handler.post(object : Runnable {
            override fun run() {
                if (!zombies.contains(zombie)) return

                val pos = zombie.localPosition
                val direction = Vector3(0f, 0f, 0.02f)
                zombie.localPosition = Vector3.add(pos, direction)

                if (zombie.localPosition.z >= 0f) {
                    zombies.remove(zombie)
                    arFragment.arSceneView.scene.removeChild(zombie.parent as Node?)
                    lives--
                    updateUI()
                    if (lives <= 0) {
                        Toast.makeText(this@ZombieActivity, "Game Over!", Toast.LENGTH_LONG).show()
                        return
                    }
                } else {
                    handler.postDelayed(this, 50)
                }
            }
        })
    }

    private fun startZombieSpawner() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (lives > 0) {
                    val frame = arFragment.arSceneView.arFrame
                    val cameraPose = frame?.camera?.displayOrientedPose
                    if (cameraPose != null) {
                        val hitResult = frame.hitTest(frame.screenCenter().x, frame.screenCenter().y).firstOrNull()
                        if (hitResult != null) {
                            spawnZombie(hitResult)
                        }
                    }
                    handler.postDelayed(this, 4000)
                }
            }
        }, 4000)
    }

    private fun updateUI() {
        Log.d("ZombieActivity", "lupdateUI: Updating UI")

        scoreTextView.text = "Score: $score"
        livesTextView.text = "Lives: $lives"
    }

    // Ekranın ortasını bulmak için yardımcı fonksiyon
    private fun com.google.ar.core.Frame.screenCenter(): android.graphics.PointF {
        val vw = arFragment.arSceneView.width / 2f
        val vh = arFragment.arSceneView.height / 2f
        return android.graphics.PointF(vw, vh)
    }
}
