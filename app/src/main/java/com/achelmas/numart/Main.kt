package com.achelmas.numart

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.achelmas.numart.hardLevelMVC.HardLevelActivity
import android.util.Log
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject


class Main : AppCompatActivity(){
    private lateinit var toolbar: Toolbar

    private lateinit var SumGameBtn : RelativeLayout
    private lateinit var MatchGameBtn : RelativeLayout
    private lateinit var DiscGameBtn : RelativeLayout

    private lateinit var fullName: String
    private lateinit var fullNameTxtView: TextView
    private lateinit var age: String
    private var mAuth: FirebaseAuth? = null

    private lateinit var myReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set language
        LanguageManager.loadLocale(this)
        // Set edge to edge
        setContentView(R.layout.activity_mainpage)
        mAuth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.mainActivity_toolBarId)
        SumGameBtn = findViewById(R.id.sum_game)
        DiscGameBtn = findViewById(R.id.Discover_game)

        MatchGameBtn = findViewById(R.id.match_game)
        fullNameTxtView = findViewById(R.id.mainActivity_fullnameId)


        SumGameBtn.setOnClickListener {
            var intent = Intent(baseContext , MainActivity::class.java)
            startActivity(intent)



        }
        DiscGameBtn.setOnClickListener {
            var intent = Intent(baseContext , DiscoverLvlActivity::class.java)
            startActivity(intent)



        }




        // Set arrow back button to Toolbar
        toolbar.inflateMenu(R.menu.menu_off)
        // Handle menu item clicks
        itemsOfToolbar()

        // get fullname from firebase
        getFullNameProcess()

        MatchGameBtn.setOnClickListener {
            var intent = Intent(baseContext , MatchMainActivity::class.java)
            startActivity(intent)



        }
        myReference = FirebaseDatabase.getInstance().reference

    }
    private fun getFullNameProcess() {

        var reference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(mAuth!!.currentUser!!.uid)

        // Use ValueEventListener to get the value of the "fullname" child
        reference.child("fullname").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get the value from the dataSnapshot
                fullName = snapshot.getValue(String::class.java)!!
                val text =  "${resources.getString(R.string.welcome)}, ${fullName}! 👋"

                // Create a SpannableString with the desired text
                val spannable = SpannableString(text)
                // Find the start and end index of the full name in the text
                val startIndex = text.indexOf(fullName)
                val endIndex = startIndex + fullName.length
                // Apply the color span to the full name
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(baseContext, R.color.primaryColor)), // Use a color of your choice
                    startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                // Set the SpannableString to the TextView
                fullNameTxtView.text = spannable
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        // Use ValueEventListener to get the value of the "age" child
        reference.child("age").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get the value from the dataSnapshot
                age = snapshot.getValue(String::class.java)!!
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun itemsOfToolbar() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settingsId -> {
                    var intent = Intent(baseContext , SettingsActivity::class.java)
                    intent.putExtra("fullname",fullName)
                    intent.putExtra("age",age)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }





}