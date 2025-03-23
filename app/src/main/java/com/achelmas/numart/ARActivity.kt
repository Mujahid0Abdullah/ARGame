package com.achelmas.numart

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position

class PuzzleGameActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar) // Ensure XML file exists

        sceneView = findViewById(R.id.arSceneView)



        loadPuzzleModel()
    }

    private fun loadPuzzleModel() {
        val puzzleNode = ArModelNode().apply {
            loadModelGlbAsync("models/number1.glb") { success ->
                    Log.d("PuzzleGame", "Model successfully loaded.")
                    scale = Position(0.5f, 0.5f, 0.5f) // Küçük başlat
                    position = Position(0f, 0f, -1.5f) // Daha belirgin bir konum

            }
        }
        sceneView.addChild(puzzleNode)


    }
}
