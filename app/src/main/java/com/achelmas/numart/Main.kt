package com.achelmas.numart

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.achelmas.numart.hardLevelMVC.HardLevelActivity
import android.util.Log
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import android.widget.Toast
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

// Utility object to check network connectivity
object NetworkUtils {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

// Main menu activity for the app
class Main : AppCompatActivity(){
    private lateinit var toolbar: Toolbar

    // Main menu buttons
    private lateinit var SumGameBtn : RelativeLayout
    private lateinit var MatchGameBtn : RelativeLayout
    private lateinit var DiscGameBtn : RelativeLayout
    private lateinit var zomGameBtn : RelativeLayout

    // User info
    private lateinit var fullName: String
    private lateinit var fullNameTxtView: TextView
    private lateinit var age: String
    private var mAuth: FirebaseAuth? = null

    private lateinit var myReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set language based on user preference
        LanguageManager.loadLocale(this)
        setContentView(R.layout.activity_mainpage)
        mAuth = FirebaseAuth.getInstance()
        val userId = mAuth!!.currentUser?.uid
        if (userId != null) {
            setInitialTargetProgress(userId)  // Initialize user progress in Firebase if not present
        }
        // Initialize UI components
        toolbar = findViewById(R.id.mainActivity_toolBarId)
        SumGameBtn = findViewById(R.id.sum_game)
        DiscGameBtn = findViewById(R.id.Discover_game)
        zomGameBtn   = findViewById(R.id.zombie_game)
        MatchGameBtn = findViewById(R.id.match_game)
        fullNameTxtView = findViewById(R.id.mainActivity_fullnameId)

        // Set click listeners for main menu buttons
        SumGameBtn.setOnClickListener {
            if (NetworkUtils.isNetworkAvailable(this)) {
                val intent = Intent(baseContext, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show()
            }
        }
        DiscGameBtn.setOnClickListener {
            if (NetworkUtils.isNetworkAvailable(this)) {
                val intent = Intent(baseContext, DiscoverLvlActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show()
            }
        }
        zomGameBtn.setOnClickListener {
            val intent = Intent(baseContext , ZombieActivity::class.java)
            startActivity(intent)
        }

        // Set up toolbar menu
        toolbar.inflateMenu(R.menu.menu_off)
        itemsOfToolbar()

        // Fetch and display user's full name
        getFullNameProcess()

        MatchGameBtn.setOnClickListener {
            if (NetworkUtils.isNetworkAvailable(this)) {
                val intent = Intent(baseContext, MatchMainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show()
            }
        }
        myReference = FirebaseDatabase.getInstance().reference
    }

    // Initializes user progress in Firebase if not already set
    fun setInitialTargetProgress(userId: String) {
        val userProgressRef = FirebaseDatabase.getInstance().reference
            .child("UserProgress")
            .child(userId)

        userProgressRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val initialProgress = mapOf(
                        "A1EasyLevel" to mapOf(
                            "1" to true,
                            "2" to false,
                            "3" to false,
                            "4" to false,
                            "5" to false,
                            "6" to false,
                            "7" to false,
                            "8" to false,
                            "9" to false,
                            "10" to false
                        ),
                        "A2MediumLevel" to mapOf(
                            "11" to true,
                            "12" to false,
                            "13" to false,
                            "14" to false,
                            "15" to false,
                            "16" to false,
                            "17" to false,
                            "18" to false,
                            "19" to false,
                            "20" to false
                        ),
                        "A3HardLevel" to mapOf(
                            "1" to false,
                            "2" to false,
                            "3" to false,
                            "4" to false,
                            "5" to false,
                            "6" to false,
                            "7" to false,
                            "8" to false
                        )
                    )

                    userProgressRef.setValue(initialProgress)
                        .addOnSuccessListener {
                            // Progress initialized successfully
                        }
                        .addOnFailureListener {
                            // Handle failure
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // Fetches user's full name and age from Firebase and updates the UI
    private fun getFullNameProcess() {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(mAuth!!.currentUser!!.uid)

        // Get full name and display with color highlight
        reference.child("fullname").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullName = snapshot.getValue(String::class.java)!!
                val text =  "${resources.getString(R.string.welcome)}, ${fullName}! 👋"
                val spannable = SpannableString(text)
                val startIndex = text.indexOf(fullName)
                val endIndex = startIndex + fullName.length
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(baseContext, R.color.primaryColor)),
                    startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                fullNameTxtView.text = spannable
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Get age (not displayed here, but fetched for use elsewhere)
        reference.child("age").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                age = snapshot.getValue(String::class.java)!!
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Handles toolbar menu item clicks (e.g., settings)
    private fun itemsOfToolbar() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settingsId -> {
                    val intent = Intent(baseContext , SettingsActivity::class.java)
                    intent.putExtra("fullname", fullName)
                    intent.putExtra("age", age)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}