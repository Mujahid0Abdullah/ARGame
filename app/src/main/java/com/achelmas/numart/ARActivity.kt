package com.achelmas.numart

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position

class ARActivity : AppCompatActivity() {

    private lateinit var arSceneView: ArSceneView
    private lateinit var sceneView: ArSceneView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)
        arSceneView = findViewById(R.id.arSceneVie)
        val modelUri = Uri.parse("models/puzzle_numbers.glb")
        val numberNode = ArModelNode().apply {
            loadModelGlbAsync( glbFileLocation = modelUri.toString()) {
                scale = Position(0.005f, 0.005f, 0.005f) // Modelin boyutunu ayarla
                this.position =   Position(-1.0f, 0.5f, -1.5f) // Modelin sahnedeki pozisyonunu ayarla
            }

        }
        arSceneView.addChild(numberNode)

    }
}
