package com.achelmas.numart

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.achelmas.numart.GameActivity
import com.achelmas.numart.R
import com.achelmas.numart.ModelOfDiscover



class AdapterOfDiscover (var activity: Activity, var easyLvlList: ArrayList<ModelOfDiscover>) : RecyclerView.Adapter<AdapterOfDiscover.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view: View = LayoutInflater.from(activity).inflate(R.layout.match_card_item, parent, false)


        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = easyLvlList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model: ModelOfDiscover = easyLvlList.get(position)
        // Show only the first 30 characters of the question
        val previewLength = 30
        val languageCode = LanguageManager.loadSelectedLanguage(activity)

        val questionText = model.question[languageCode] ?: model.question["en"] ?: "Question unavailable"
        val questionPreview = if (questionText.length > previewLength) {
            questionText.substring(0, previewLength) + "…"
        } else {
            questionText
        }


        holder.targetOfStar.text = "Level ${model.targetNumber}"
        holder.targetOfTitle.text = questionPreview

        if (model.isUnlocked) {
            // Hedef açık
            holder.levelButton.isEnabled = true
            holder.levelButton.alpha = 1.0f // Normal görünüm
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
            // Hedef kapalı
            holder.levelButton.isEnabled = false
            holder.levelButton.alpha = 0.5f // Şeffaf görünüm
            holder.levelButton.setOnClickListener(null) // Tıklamayı kaldır
        }
    }

    inner class MyViewHolder(i: View) : RecyclerView.ViewHolder(i) {
        var targetOfStar: TextView
        var targetOfTitle: TextView
        var levelButton: CardView

        init {
            targetOfStar = i.findViewById(R.id.discoverCardItem_level)
            targetOfTitle = i.findViewById(R.id.discoverCardItem_question)
            levelButton = i.findViewById(R.id.discoverCardItem_buttonId)
        }

    }
}