package com.achelmas.numart

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

// Adapter for displaying discover level cards in a RecyclerView.
// Handles enabling/disabling levels based on unlock status and starts the discover game activity for unlocked levels.
class AdapterOfDiscover(
    var activity: Activity,
    var easyLvlList: ArrayList<ModelOfDiscover>
) : RecyclerView.Adapter<AdapterOfDiscover.MyViewHolder>() {

    // Inflates the card layout for each discover level item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(activity).inflate(R.layout.match_card_item, parent, false)
        return MyViewHolder(view)
    }

    // Returns the number of discover levels to display
    override fun getItemCount(): Int = easyLvlList.size

    // Binds data to each card and sets up click listeners based on unlock status
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model: ModelOfDiscover = easyLvlList[position]
        val previewLength = 30
        val languageCode = LanguageManager.loadSelectedLanguage(activity)

        // Get the question in the user's language, fallback to English if not available
        val questionText = model.question[languageCode] ?: model.question["en"] ?: "Question unavailable"
        val questionPreview = if (questionText.length > previewLength) {
            questionText.substring(0, previewLength) + "…"
        } else {
            questionText
        }

        holder.targetOfStar.text = "Level ${model.targetNumber}"
        holder.targetOfTitle.text = questionPreview

        if (model.isUnlocked) {
            // Level is unlocked: enable button and set click listener to start the discover game
            holder.levelButton.isEnabled = true
            holder.levelButton.alpha = 1.0f
            holder.levelButton.setOnClickListener {
                val intent = Intent(activity, DiscoverGameActivity::class.java)
                intent.putExtra("Question", questionText)
                intent.putExtra("Target", model.target)
                intent.putExtra("Target Number", model.targetNumber)
                intent.putExtra("Shape1", model.shape1)
                intent.putExtra("Shape2", model.shape2)
                intent.putExtra("Shape3", model.shape3)
                intent.putExtra("Shape4", model.shape4)
                activity.startActivity(intent)
            }
        } else {
            // Level is locked: disable button and make it semi-transparent
            holder.levelButton.isEnabled = false
            holder.levelButton.alpha = 0.5f
            holder.levelButton.setOnClickListener(null)
        }
    }

    // ViewHolder holds references to the views for each card item
    inner class MyViewHolder(i: View) : RecyclerView.ViewHolder(i) {
        var targetOfStar: TextView = i.findViewById(R.id.discoverCardItem_level)
        var targetOfTitle: TextView = i.findViewById(R.id.discoverCardItem_question)
        var levelButton: CardView = i.findViewById(R.id.discoverCardItem_buttonId)
    }
}