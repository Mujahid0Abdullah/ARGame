package com.achelmas.numart

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.achelmas.numart.hardLevelMVC.HardLevelActivity
import com.achelmas.numart.mediumLevelMVC.MediumLevelActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MatchMainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var fullNameTxtView: TextView
    private lateinit var fullName: String
    private lateinit var age: String
    private lateinit var easyLevelBtn: RelativeLayout
    private lateinit var mediumLevelBtn: RelativeLayout
    private lateinit var hardLevelBtn: RelativeLayout

    //   Firebase authentication instance
    private var mAuth: FirebaseAuth? = null
    /**
     * ----------------------------------------------------------------------------- Called when the
     * activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set language
        LanguageManager.loadLocale(this)

        setContentView(R.layout.activity_match_main)

        mAuth = FirebaseAuth.getInstance()
        Log.d("TAG", mAuth.toString())

        // Initialize variables
        toolbar = findViewById(R.id.mainActivity_toolBarId)
        fullNameTxtView = findViewById(R.id.mainActivity_fullnameId)
        easyLevelBtn = findViewById(R.id.mainActivity_easyLevelId)
        mediumLevelBtn = findViewById(R.id.mainActivity_mediumLevelId)
        hardLevelBtn = findViewById(R.id.mainActivity_hardLevelId)
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener { finish() }
        // Set items ( Profile and Settings ) in Toolbar
        toolbar.inflateMenu(R.menu.menu_off)
        // Handle menu item clicks
        itemsOfToolbar()

        // get fullname from firebase
        getFullNameProcess()

        // Handle button clicks
        easyLevelBtn.setOnClickListener {

            if (NetworkUtils.isNetworkAvailable(this)) {
                val intent = Intent(baseContext, MatchLvl1Activity::class.java)
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
            var intent = Intent(baseContext, HardLevelActivity::class.java)
            startActivity(intent)
        }

        val userId = mAuth!!.currentUser?.uid
        if (userId != null) {
            setInitialTargetProgress(userId) // Kullanıcının ilerlemesini Firebase'e kaydeder
        }
        println("hi")
    }
    /**
     * ------------------------------------------------------------------------------------- Fetches
     * the user's full name and age from Firebase and updates the UI.
     */
    private fun getFullNameProcess() {

        var reference: DatabaseReference =
                FirebaseDatabase.getInstance()
                        .reference
                        .child("Users")
                        .child(mAuth!!.currentUser!!.uid)

        // Use ValueEventListener to get the value of the "fullname" child
        reference
                .child("fullname")
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // Get the value from the dataSnapshot
                                fullName = snapshot.getValue(String::class.java)!!
                                val text =
                                        "${resources.getString(R.string.welcome)}, ${fullName}! 👋"

                                // Create a SpannableString with the desired text
                                val spannable = SpannableString(text)
                                // Find the start and end index of the full name in the text
                                val startIndex = text.indexOf(fullName)
                                val endIndex = startIndex + fullName.length
                                // Apply the color span to the full name
                                spannable.setSpan(
                                        ForegroundColorSpan(
                                                ContextCompat.getColor(
                                                        baseContext,
                                                        R.color.primaryColor
                                                )
                                        ), // Use a color of your choice
                                        startIndex,
                                        endIndex,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                // Set the SpannableString to the TextView
                                fullNameTxtView.text = spannable
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        }
                )

        // Use ValueEventListener to get the value of the "age" child
        reference
                .child("age")
                .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // Get the value from the dataSnapshot
                                age = snapshot.getValue(String::class.java)!!
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        }
                )
    }
    /**
     * -----------------------------------------------------------------------------------------
     * Sets up the toolbar menu and handles menu item clicks.
     */
    private fun itemsOfToolbar() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settingsId -> {
                    var intent = Intent(baseContext, SettingsActivity::class.java)
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
     * --------------------------------------------------------------------------------------------------
     * Initializes the user's progress in Firebase if it does not already exist.
     * @param userId The unique ID of the user.
     */
    fun setInitialTargetProgress(userId: String) {
        val userProgressRef =
                FirebaseDatabase.getInstance().reference.child("UserProgress").child(userId)

        userProgressRef.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.child("MatchEasyLevel").exists()) {
                            val initialProgress =
                                    mapOf(
                                            "MatchEasyLevel" to
                                                    mapOf(
                                                            "1" to true,
                                                            "2" to false,
                                                            "3" to false,
                                                            "4" to false,
                                                            "5" to false,
                                                            "6" to false,
                                                            "7" to false,
                                                            "8" to false
                                                    ),
                                            "MatchMediumLevel" to
                                                    mapOf(
                                                            "1" to false,
                                                            "2" to false,
                                                            "3" to false,
                                                            "4" to false,
                                                            "5" to false,
                                                            "6" to false,
                                                            "7" to false,
                                                            "8" to false
                                                    ),
                                            "MatchHardLevel" to
                                                    mapOf(
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

                            userProgressRef
                                    .updateChildren(initialProgress)
                                    .addOnSuccessListener {
                                        // Handle success
                                    }
                                    .addOnFailureListener {
                                        // Handle failure
                                    }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear any references and finish the activity
        finish()
    }

    override fun onBackPressed() {
        // Finish the activity when back is pressed
        finish()
        super.onBackPressed()
    }
}
