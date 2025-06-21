// Adapter for displaying match level cards in a RecyclerView.
// Handles enabling/disabling levels based on unlock status and starts the game activity for unlocked levels.

package com.achelmas.numart

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.achelmas.numart.easyLevelMVC.ModelOfEasyLvl

class AdapterOfMatchLvl1(
    var activity: Activity,
    var easyLvlList: ArrayList<ModelOfEasyLvl>
) : RecyclerView.Adapter<AdapterOfMatchLvl1.MyViewHolder>() {

    // Inflates the card layout for each level item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(activity).inflate(R.layout.levels_card_item, parent, false)
        return MyViewHolder(view)
    }

    // Returns the number of levels to display
    override fun getItemCount(): Int = easyLvlList.size

    // Binds data to each card and sets up click listeners based on unlock status
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model: ModelOfEasyLvl = easyLvlList[position]
        holder.targetOfStar.text = model.target
        holder.targetOfTitle.text = "${model.targetNumber}. ${activity.resources.getString(R.string.our_target)} ${model.target}"

        if (model.isUnlocked) {
            // Level is unlocked: enable button and set click listener to start the game
            holder.levelButton.isEnabled = true
            holder.levelButton.alpha = 1.0f
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
            // Level is locked: disable button and make it semi-transparent
            holder.levelButton.isEnabled = false
            holder.levelButton.alpha = 0.5f
            holder.levelButton.setOnClickListener(null)
        }
    }

    // ViewHolder holds references to the views for each card item
    inner class MyViewHolder(i: View) : RecyclerView.ViewHolder(i) {
        var targetOfStar: TextView = i.findViewById(R.id.levelsCardItem_targetOfStarId)
        var targetOfTitle: TextView = i.findViewById(R.id.levelsCardItem_targetOfTitleId)
        var levelButton: CardView = i.findViewById(R.id.levelsCardItem_buttonId)
    }
}