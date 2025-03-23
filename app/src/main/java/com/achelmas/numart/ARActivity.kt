package com.achelmas.numart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position

class PuzzleGameActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar) // Ensure you create this XML file

        sceneView = findViewById(R.id.arSceneView)

        loadPuzzleModel()
    }

    private fun loadPuzzleModel() {
        val puzzleNode = ArModelNode().apply {
            loadModelGlbAsync("models/number1.glb") {
                scale = Position(1f, 1f, 1f) // Adjust size
                this.position = Position(-1.0f, 0.5f, -1.5f) // Adjust position
            }
        }
        sceneView.addChild(puzzleNode)
    }
}
