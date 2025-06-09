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

/**
 * Main activity for the AR Zombie game.
 * Handles AR setup, zombie spawning, movement, scoring, and game state.
 */
class ZombieActivity : AppCompatActivity() {
    // UI elements
    private lateinit var refreshButton: CardView
    private lateinit var arFragment: ArFragment
    private lateinit var startButton: Button
    private lateinit var scoreTextView: TextView
    private lateinit var livesTextView: TextView

    // Sound effects for game events
    private lateinit var gunPlayer: MediaPlayer
    private lateinit var deathPlayer: MediaPlayer
    private lateinit var loseHeartPlayer: MediaPlayer
    private lateinit var zombieGrowlPlayer: MediaPlayer

    // Game state variables
    private var zombieRenderable: ModelRenderable? = null // 3D zombie model
    private var score = 0                                 // Player's score
    private var lives = 3                                 // Player's lives
    private var isGameStarted = false                     // Game state flag
    private var hitlast: HitResult? = null                // Last AR hit result

    // Zombie management
    private val growledZombies = mutableSetOf<Node>()     // Zombies that have growled
    private val handler = Handler()                       // For scheduling tasks
    private val zombies = mutableListOf<Node>()           // Active zombies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zombie)

        // Initialize sound effects
        gunPlayer = MediaPlayer.create(this, R.raw.gun_shot_ak)
        deathPlayer = MediaPlayer.create(this, R.raw.zombie_death_smashed)
        loseHeartPlayer = MediaPlayer.create(this, R.raw.lose_heart)
        zombieGrowlPlayer = MediaPlayer.create(this, R.raw.zombie_death)

        // Find UI elements
        refreshButton = findViewById(R.id.gameActivity_refreshButton)
        arFragment = supportFragmentManager.findFragmentById(R.id.arSceneViewId) as ArFragment
        scoreTextView = findViewById(R.id.gameActivity_expressionView)
        livesTextView = findViewById(R.id.gameActivity_targetView)
        startButton = findViewById(R.id.startButton)

        // Refresh button restarts the activity
        refreshButton.setOnClickListener {
            finish()
            startActivity(intent)
        }

        // Load the 3D zombie model
        loadZombieModel()
        updateUI()

        // Start button begins the game and spawns zombies
        startButton.setOnClickListener {
            isGameStarted = true
            startButton.visibility = View.GONE
            startZombieSpawner()
            Toast.makeText(this, "Game Started!", Toast.LENGTH_SHORT).show()
        }

        // Play gun sound when user taps on AR plane (for feedback)
        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _ ->
            if (!isGameStarted) return@setOnTapArPlaneListener
            playGunSound()
        }
    }

    /** Plays the zombie growl sound effect. */
    private fun playZombieGrowl() {
        zombieGrowlPlayer.seekTo(0)
        zombieGrowlPlayer.start()
    }

    /** Plays the lose heart sound effect. */
    private fun playLoseHeartSound() {
        loseHeartPlayer.seekTo(0)
        loseHeartPlayer.start()
    }

    /** Plays the gun shot sound effect. */
    private fun playGunSound() {
        gunPlayer.seekTo(0)
        gunPlayer.start()
    }

    /** Plays the zombie death sound effect. */
    private fun playDeathSound() {
        deathPlayer.seekTo(0)
        deathPlayer.start()
    }

    /**
     * Loads the 3D zombie model from assets.
     * Sets zombieRenderable when loaded.
     */
    private fun loadZombieModel() {
        ModelRenderable.builder()
            .setSource(this, Uri.parse("models/zombie.glb"))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable ->
                Log.d("ZombieActivity", "loadZombieModel: Model loaded successfully")
                zombieRenderable = renderable
            }
            .exceptionally {
                Toast.makeText(this, "Failed to load zombie model", Toast.LENGTH_SHORT).show()
                null
            }
    }

    /**
     * Spawns a zombie at a random position around the player.
     * Limits the number of zombies to 5 at a time.
     */
    private fun spawnZombie(hitResult: HitResult) {
        Log.d("ZombieActivity", "load ${zombies.size} ZombieModel: ${hitResult.createAnchor()}")

        // Limit max zombies
        if (zombies.size >= 5) return

        // Create anchor node at hit position
        val anchorNode = AnchorNode(hitResult.createAnchor())
        anchorNode.setParent(arFragment.arSceneView.scene)

        // Randomize spawn position (angle and distance)
        val randomX: Float
        val pickFirstRange = Random.nextBoolean()
        randomX = if (pickFirstRange) -3.0f + Random.nextFloat() else 2.0f + Random.nextFloat()
        val centerAngle = Math.PI / 2
        val halfAngleRange = Math.toRadians(75.0)
        val angle = centerAngle + (Random.nextFloat() * 2 - 1) * halfAngleRange
        val distance = 3f + Random.nextFloat() * 2f
        var x = (distance * Math.cos(angle)).toFloat()
        var z = -(distance * Math.sin(angle)).toFloat()
        x = x.coerceIn(-3f, 3f)
        z = z.coerceIn(-4f, 0f)

        // Create zombie node and set its properties
        val zombieNode = Node().apply {
            setParent(anchorNode)
            worldScale = Vector3(0.5f, 0.5f, 0.5f)
            renderable = zombieRenderable
            localPosition = Vector3(x, 0f, z)
        }

        // Handle tap on zombie: play sounds, remove zombie, update score
        zombieNode.setOnTapListener { _, _ ->
            playGunSound()
            playDeathSound()
            arFragment.arSceneView.scene.removeChild(anchorNode)
            zombies.remove(zombieNode)
            score += 10
            updateUI()
        }

        // Add zombie to scene and start its movement
        arFragment.arSceneView.scene.addChild(anchorNode)
        zombies.add(zombieNode)
        moveZombie(zombieNode)
        zombieNode.localRotation = com.google.ar.sceneform.math.Quaternion.axisAngle(Vector3(0f, 1f, 0f), 180f)

        // Play zombie animation if available
        zombieNode.renderableInstance?.let { instance ->
            if (instance.animationCount > 0) {
                val animator = instance.animate(2)
                animator.start()
            }
        }
    }

    /**
     * Moves the zombie towards the player.
     * Handles collision, growl, and life deduction.
     */
    private fun moveZombie(zombie: Node) {
        handler.post(object : Runnable {
            override fun run() {
                if (!zombies.contains(zombie)) return

                // Get player's position from camera pose
                val camPose = arFragment.arSceneView.arFrame?.camera?.pose ?: return
                val playerPos = Vector3(camPose.tx(), camPose.ty(), camPose.tz())
                val zombiePos = zombie.worldPosition

                // Calculate direction and speed (scales with score)
                val direction = Vector3.subtract(playerPos, zombiePos).normalized()
                val speed = 0.01f + (score / 2000f).coerceAtMost(0.06f)

                // Rotate zombie to face player
                val lookDirection = Vector3.subtract(playerPos, zombie.worldPosition)
                zombie.worldRotation = com.google.ar.sceneform.math.Quaternion.lookRotation(lookDirection, Vector3.up())

                // Move zombie towards player
                zombie.worldPosition = Vector3.add(zombiePos, direction.scaled(speed))

                // Play growl if close enough and not already growled
                val distance = Vector3.subtract(playerPos, zombie.worldPosition).length()
                if (distance < 0.8f && !growledZombies.contains(zombie)) {
                    playZombieGrowl()
                    growledZombies.add(zombie)
                }

                // If zombie reaches player, remove it and deduct a life
                if (distance < 0.4f) {
                    zombies.remove(zombie)
                    arFragment.arSceneView.scene.removeChild(zombie.parent as? Node)
                    lives--
                    playLoseHeartSound()
                    updateUI()
                    if (lives <= 0) {
                        removeAllZombies()
                        Toast.makeText(this@ZombieActivity, "Game Over!", Toast.LENGTH_LONG).show()
                        handler.removeCallbacksAndMessages(null)
                        return
                    }
                } else {
                    handler.postDelayed(this, 50)
                }
            }
        })
    }

    /** Removes all zombies from the scene and clears lists. */
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

    /**
     * Periodically spawns zombies while the game is active.
     * Uses a fixed delay (can be adjusted for difficulty).
     */
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
                        if (hit != null) { hitlast = hit }
                        Log.d("ZombieActivity", "Hit result: $hit")
                        Log.d("ZombieActivity", "Hit result: $hitlast")
                        if (hitlast != null) {
                            spawnZombie(hitlast!!)
                        }
                    }
                    handler.postDelayed(this, 4000)
                }
            }
        }, 4000)
    }

    /** Updates the score and lives UI. */
    private fun updateUI() {
        scoreTextView.text = "Score: $score"
        livesTextView.text = "❤️".repeat(lives)
    }

    /**
     * Cleans up resources when activity is destroyed.
     * Releases all MediaPlayers and stops handlers.
     */
    override fun onDestroy() {
        super.onDestroy()
        gunPlayer.release()
        deathPlayer.release()
        loseHeartPlayer.release()
        zombieGrowlPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}