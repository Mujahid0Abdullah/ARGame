package com.achelmas.numart

import com.achelmas.numart.AdapterOfMatchLvl1
import com.achelmas.numart.easyLevelMVC.ModelOfEasyLvl



import android.util.Log

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelmas.numart.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class MatchLvl1Activity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterOfMatchLvl1
    private lateinit var easyLvlList: ArrayList<ModelOfEasyLvl>
    private lateinit var myReference: DatabaseReference
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_level1)

        mAuth = FirebaseAuth.getInstance()

        // Initialize Views
        toolbar = findViewById(R.id.easyLvlActivity_toolBarId)
        recyclerView = findViewById(R.id.easyLvlActivity_recyclerViewId)

        // Set arrow back button to Toolbar
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        getDataFromFirebase()
    }

    private fun getDataFromFirebase() {
        easyLvlList = ArrayList()
        myReference = FirebaseDatabase.getInstance().reference

        val userId = mAuth!!.currentUser!!.uid
        val userProgressRef = myReference.child("UserProgress").child(userId).child("MatchEasyLevel")
        val targetsRef = myReference.child("Match Level1")

        // Kullanıcı ilerlemesini ve hedefleri paralel olarak çek
        userProgressRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(userProgressSnapshot: DataSnapshot) {
                targetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(targetsSnapshot: DataSnapshot) {
                        Log.d(targetsSnapshot.children.toString(),"firebase")
                        Log.d(userProgressSnapshot.value.toString(),"firebase")
                        Log.d("TAG", userProgressRef.toString())

                        for (snapshot: DataSnapshot in targetsSnapshot.children) {
                            val model = ModelOfEasyLvl()
                            model.target = snapshot.child("Target").value.toString()
                            model.targetNumber = snapshot.child("Target Number").value.toString()
                            model.number1 = snapshot.child("Number1").value.toString()
                            model.number2 = snapshot.child("Number2").value.toString()
                            model.number3 = snapshot.child("Number3").value.toString()
                            model.number4 = snapshot.child("Number4").value.toString()

                            // İlk hedef her zaman açık olacak
                            model.isUnlocked = userProgressSnapshot.child(model.targetNumber).value == true || model.targetNumber == "1"
                            easyLvlList.add(model)
                            Log.d("TAG", "isunlocked: ${model.isUnlocked}")

                            Log.d("TAG", "Target: ${model.target}, Target Number: ${model.targetNumber}, Numbers: ${model.number1}, ${model.number2}, ${model.number3}, ${model.number4}")

                        }

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