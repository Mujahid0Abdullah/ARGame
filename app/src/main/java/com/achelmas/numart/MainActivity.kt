package com.achelmas.numart

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.achelmas.numart.easyLevelMVC.EasyLevelActivity
import com.achelmas.numart.hardLevelMVC.HardLevelActivity
import com.achelmas.numart.mediumLevelMVC.MediumLevelActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * MainActivity is the entry point for the app after login.
 * Handles user greeting, navigation to levels, and toolbar actions.
 */
class MainActivity : AppCompatActivity() {

    // UI components
    private lateinit var toolbar: Toolbar
    private lateinit var fullNameTxtView: TextView
    private lateinit var fullName: String
    private lateinit var age: String
    private lateinit var easyLevelBtn : RelativeLayout
    private lateinit var mediumLevelBtn : RelativeLayout
    private lateinit var hardLevelBtn : RelativeLayout

    // Firebase authentication instance
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set app language based on saved preference
        LanguageManager.loadLocale(this)

        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()
        Log.d("TAG", mAuth.toString())

        // Find and assign UI elements
        toolbar = findViewById(R.id.mainActivity_toolBarId)
        fullNameTxtView = findViewById(R.id.mainActivity_fullnameId)
        easyLevelBtn = findViewById(R.id.mainActivity_easyLevelId)
        mediumLevelBtn = findViewById(R.id.mainActivity_mediumLevelId)
        hardLevelBtn = findViewById(R.id.mainActivity_hardLevelId)

        // Set up toolbar navigation and menu
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.menu_off)
        itemsOfToolbar() // Set up toolbar menu item click listeners

        // Fetch and display user's full name from Firebase
        getFullNameProcess()

        // Set up click listeners for level buttons
        easyLevelBtn.setOnClickListener {
            // Only allow navigation if network is available
            if (NetworkUtils.isNetworkAvailable(this)) {
                val intent = Intent(baseContext, EasyLevelActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show()
            }
        }
        mediumLevelBtn.setOnClickListener {
            if (NetworkUtils.isNetworkAvailable(this)) {
                val intent = Intent(baseContext, MediumLevelActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show()
            }
        }
        hardLevelBtn.setOnClickListener {
            if (NetworkUtils.isNetworkAvailable(this)) {
                val intent = Intent(baseContext, HardLevelActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize user progress in Firebase if not already set
        val userId = mAuth!!.currentUser?.uid
        if (userId != null) {
            setInitialTargetProgress(userId)
        }
    }

    /**
     * Fetches the user's full name and age from Firebase and updates the UI.
     * Applies color styling to the full name in the welcome message.
     */
    private fun getFullNameProcess() {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference
            .child("Users").child(mAuth!!.currentUser!!.uid)

        // Get full name and update welcome message
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

        // Get age (used for settings)
        reference.child("age").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                age = snapshot.getValue(String::class.java)!!
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Sets up toolbar menu item click listeners.
     * Handles navigation to settings, passing user info.
     */
    private fun itemsOfToolbar() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settingsId -> {
                    val intent = Intent(baseContext, SettingsActivity::class.java)
                    intent.putExtra("fullname", fullName)
                    intent.putExtra("age", age)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Initializes user progress in Firebase if not already present.
     * Sets up default progress for each level.
     */
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
}