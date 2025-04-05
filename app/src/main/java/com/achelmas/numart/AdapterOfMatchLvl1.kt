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
import com.achelmas.numart.easyLevelMVC.ModelOfEasyLvl

class AdapterOfMatchLvl1(var activity: Activity, var easyLvlList: ArrayList<ModelOfEasyLvl>) : RecyclerView.Adapter<AdapterOfMatchLvl1.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view: View = LayoutInflater.from(activity).inflate(R.layout.levels_card_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = easyLvlList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model: ModelOfEasyLvl = easyLvlList.get(position)
        holder.targetOfStar.text = model.target
        holder.targetOfTitle.text = "${model.targetNumber}. ${activity.resources.getString(R.string.our_target)} ${model.target}"

        if (model.isUnlocked) {
            // Hedef açık
            holder.levelButton.isEnabled = true
            holder.levelButton.alpha = 1.0f // Normal görünüm
            holder.levelButton.setOnClickListener {
                val intent = Intent(activity, MatchGameActivity::class.java)
                intent.putExtra("Target", model.target)
                intent.putExtra("Target Number", model.targetNumber)
                intent.putExtra("Number1", model.number1)
                intent.putExtra("Number2", model.number2)
                intent.putExtra("Number3", model.number3)
                intent.putExtra("Number4", model.number4)
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
            targetOfStar = i.findViewById(R.id.levelsCardItem_targetOfStarId)
            targetOfTitle = i.findViewById(R.id.levelsCardItem_targetOfTitleId)
            levelButton = i.findViewById(R.id.levelsCardItem_buttonId)
        }

    }
}