package com.achelmas.numart

import com.achelmas.numart.AdapterOfMatchLvl1
import com.achelmas.numart.easyLevelMVC.ModelOfEasyLvl

import android.util.Log
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Activity for displaying Match Level 1 targets in a RecyclerView
class MatchLvl1Activity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterOfMatchLvl1
    private lateinit var easyLvlList: ArrayList<ModelOfEasyLvl>
    private lateinit var myReference: DatabaseReference
    private var mAuth: FirebaseAuth? = null

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_level1)

        mAuth = FirebaseAuth.getInstance()

        // Initialize toolbar and RecyclerView
        toolbar = findViewById(R.id.easyLvlActivity_toolBarId)
        recyclerView = findViewById(R.id.easyLvlActivity_recyclerViewId)

        // Set up back navigation for toolbar
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Set up RecyclerView layout
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        // Fetch data from Firebase and populate the list
        getDataFromFirebase()
    }

    // Fetches user progress and level targets from Firebase, then updates the RecyclerView
    private fun getDataFromFirebase() {
        easyLvlList = ArrayList()
        myReference = FirebaseDatabase.getInstance().reference

        val userId = mAuth!!.currentUser!!.uid
        val userProgressRef = myReference.child("UserProgress").child(userId).child("MatchEasyLevel")
        val targetsRef = myReference.child("Match Level1")

        // Fetch user progress and targets in parallel
        userProgressRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userProgressSnapshot: DataSnapshot) {
                targetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(targetsSnapshot: DataSnapshot) {
                        // Iterate through each target and build the model list
                        for (snapshot: DataSnapshot in targetsSnapshot.children) {
                            val model = ModelOfEasyLvl()
                            model.target = snapshot.child("Target").value.toString()
                            model.targetNumber = snapshot.child("Target Number").value.toString()
                            model.number1 = snapshot.child("Number1").value.toString()
                            model.number2 = snapshot.child("Number2").value.toString()
                            model.number3 = snapshot.child("Number3").value.toString()
                            model.number4 = snapshot.child("Number4").value.toString()

                            // Unlock the first target by default, others based on user progress
                            model.isUnlocked = userProgressSnapshot.child(model.targetNumber).value == true || model.targetNumber == "1"
                            easyLvlList.add(model)
                        }

                        // Set up the adapter and update the RecyclerView
                        adapter = AdapterOfMatchLvl1(this@MatchLvl1Activity, easyLvlList)
                        recyclerView.adapter = adapter
                        adapter.notifyDataSetChanged()
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}