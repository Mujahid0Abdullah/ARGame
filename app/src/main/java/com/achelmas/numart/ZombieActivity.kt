package com.achelmas.numart
import android.util.Log
import android.view.View
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
import kotlin.random.Random
import android.media.MediaPlayer
import android.view.MotionEvent
import androidx.cardview.widget.CardView

class ZombieActivity : AppCompatActivity() {
    private lateinit var refreshButton: CardView

    private lateinit var arFragment: ArFragment
    private lateinit var startButton: Button
    private lateinit var scoreTextView: TextView
    private lateinit var livesTextView: TextView
    private lateinit var gunPlayer: MediaPlayer
    private lateinit var deathPlayer: MediaPlayer
    private var zombieRenderable: ModelRenderable? = null
    private var score = 0
    private var lives = 3
    private var isGameStarted = false
    private var hitlast: HitResult? = null
    private lateinit var loseHeartPlayer: MediaPlayer
    private lateinit var zombieGrowlPlayer: MediaPlayer
    private val growledZombies = mutableSetOf<Node>()
    private val handler = Handler()
    private val zombies = mutableListOf<Node>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zombie)
        gunPlayer = MediaPlayer.create(this, R.raw.gun_shot_ak)
        deathPlayer = MediaPlayer.create(this, R.raw.zombie_death_smashed)
        loseHeartPlayer = MediaPlayer.create(this, R.raw.lose_heart)
        zombieGrowlPlayer = MediaPlayer.create(this, R.raw.zombie_death)
        refreshButton = findViewById(R.id.gameActivity_refreshButton)

        arFragment = supportFragmentManager.findFragmentById(R.id.arSceneViewId) as ArFragment
        scoreTextView = findViewById(R.id.gameActivity_expressionView)
        livesTextView = findViewById(R.id.gameActivity_targetView)
        startButton = findViewById(R.id.startButton)
        refreshButton.setOnClickListener {
            finish()
            startActivity(intent)
        }
        loadZombieModel()
        updateUI()

        startButton.setOnClickListener {
            isGameStarted = true
            startButton.visibility = View.GONE
            startZombieSpawner() // ✅ sadece burada çağır

            Toast.makeText(this, "Game Started!", Toast.LENGTH_SHORT).show()
        }
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _ ->
            if ( !isGameStarted) return@setOnTapArPlaneListener

            playGunSound() // 🔫 Kullanıcının dokunduğu anda ses çal
        }
    }
    private fun playZombieGrowl() {
        zombieGrowlPlayer.seekTo(0)
        zombieGrowlPlayer.start()
    }
    private fun playLoseHeartSound() {
        loseHeartPlayer.seekTo(0)
        loseHeartPlayer.start()
    }
    private fun playGunSound() {
        gunPlayer.seekTo(0)
        gunPlayer.start()
    }
    private fun playDeathSound() {
        deathPlayer.seekTo(0)
        deathPlayer.start()
    }
    private fun loadZombieModel() {
        ModelRenderable.builder()
            .setSource(this, Uri.parse("models/zombie.glb"))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable ->
                Log.d("ZombieActivity", "loadZombieModel: Model loaded successfully")

                zombieRenderable = renderable
                // If your renderable has animations, play the first one

            }
            .exceptionally {
                Toast.makeText(this, "Failed to load zombie model", Toast.LENGTH_SHORT).show()
                null
            }
    }

    private fun spawnZombie(hitResult: HitResult) {
        Log.d("ZombieActivity", "load ${zombies.size} ZombieModel: ${hitResult.createAnchor()}")


        // ✅ Maksimum zombi sınırı
        if (zombies.size >= 5) return
        val anchorNode = AnchorNode(hitResult.createAnchor())
        anchorNode.setParent(arFragment.arSceneView.scene)
        val randomX: Float
        val pickFirstRange = Random.nextBoolean()
        if (pickFirstRange) {
            // Generates a random float in the range [-3.0, -2.0)
            // Random.nextFloat() produces [0.0, 1.0)
            // So, -3.0f + [0.0, 1.0) gives [-3.0, -2.0)
            randomX = -3.0f + Random.nextFloat()
        } else {
            // Generates a random float in the range [2.0, 3.0)
            // So, 2.0f + [0.0, 1.0) gives [2.0, 3.0)
            randomX = 2.0f + Random.nextFloat()
        }

        val angle = Random.nextFloat() * Math.PI // [0, π] for front half
        val distance = 4f + Random.nextFloat() * 1f // [2, 4]
        var x = (distance * Math.cos(angle)).toFloat()
        var z = -(distance * Math.sin(angle)).toFloat()

        x = x.coerceIn(-3f, 3f)
        z = z.coerceIn(-4f, 0f)
        val zombieNode = Node().apply {
            setParent(anchorNode)
            worldScale = Vector3(0.5f, 0.5f, 0.5f)
            renderable = zombieRenderable

            localPosition = Vector3(
                x,
                0f,
                z
            )


        }


        zombieNode.setOnTapListener { _, _ ->
            playGunSound()
            playDeathSound()
            arFragment.arSceneView.scene.removeChild(anchorNode)
            zombies.remove(zombieNode)
            score += 10
            updateUI()
        }

        arFragment.arSceneView.scene.addChild(anchorNode)
        zombies.add(zombieNode)
        moveZombie(zombieNode)
        zombieNode.localRotation = com.google.ar.sceneform.math.Quaternion.axisAngle(Vector3(0f, 1f, 0f), 180f)
        zombieNode.renderableInstance?.let { instance ->
            if (instance.animationCount > 0) {
                val animator = instance.animate(2)
                animator.start()
            }
        }
    }

    private fun moveZombie(zombie: Node) {
        handler.post(object : Runnable {
            override fun run() {
                if (!zombies.contains(zombie)) return

                val camPose = arFragment.arSceneView.arFrame?.camera?.pose ?: return
                val playerPos = Vector3(camPose.tx(), camPose.ty(), camPose.tz())

                val zombiePos = zombie.worldPosition
                val direction = Vector3.subtract(playerPos, zombiePos).normalized()
                val speed = 0.01f + (score / 2000f).coerceAtMost(0.06f)
                val lookDirection = Vector3.subtract(playerPos, zombie.worldPosition)
                zombie.worldRotation = com.google.ar.sceneform.math.Quaternion.lookRotation(lookDirection, Vector3.up())
                zombie.worldPosition = Vector3.add(zombiePos, direction.scaled(speed))

                val distance = Vector3.subtract(playerPos, zombie.worldPosition).length()
                if (distance < 0.8f && !growledZombies.contains(zombie)) {
                    playZombieGrowl()
                    growledZombies.add(zombie)
                }
                if (distance < 0.4f) {
                    // Player touched
                    zombies.remove(zombie)
                    arFragment.arSceneView.scene.removeChild(zombie.parent as? Node)
                    lives--
                    playLoseHeartSound() // <-- Play sound here

                    updateUI()
                    if (lives <= 0) {
                        removeAllZombies()
                        Toast.makeText(this@ZombieActivity, "Game Over!", Toast.LENGTH_LONG).show()
                        handler.removeCallbacksAndMessages(null) // ✅ tüm işlemleri durdur

                        return
                    }
                } else {
                    handler.postDelayed(this, 50)
                }
            }
        })
    }
    private fun removeAllZombies() {
        Log.d("ZombieActivity", "ZombieModel: deleting all zombies")

        for (zombie in zombies) {
            val parentNode = zombie.parent as? Node
            if (parentNode != null) {
                arFragment.arSceneView.scene.removeChild(parentNode)
            } else {
                arFragment.arSceneView.scene.removeChild(zombie)
            }
        }
        zombies.clear()
        growledZombies.clear()
    }

    private fun startZombieSpawner() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (lives > 0 && isGameStarted) {
                    val frame = arFragment.arSceneView.arFrame
                    val cameraPose = frame?.camera?.pose
                    if (cameraPose != null) {
                        val hitResults = frame.hitTest(
                            arFragment.arSceneView.width / 2f,
                            arFragment.arSceneView.height / 2f
                        )
                        val hit = hitResults.firstOrNull()
                        if (hit != null) { hitlast = hit}

                        Log.d("ZombieActivity", "Hit result: $hit")
                        Log.d("ZombieActivity", "Hit result: $hitlast")
                        if (hitlast != null) {
                            spawnZombie(hitlast!!)
                        }                    }
                    handler.postDelayed(this, 4000)
                }
            }
        }, 4000)
    }

    private fun updateUI() {
        scoreTextView.text = "Score: $score"
        livesTextView.text = "❤️".repeat(lives)    }
    override fun onDestroy() {
        super.onDestroy()
        gunPlayer.release()
        deathPlayer.release()
        loseHeartPlayer.release()
        zombieGrowlPlayer.release()
        handler.removeCallbacksAndMessages(null) // cleanup

    }
}
