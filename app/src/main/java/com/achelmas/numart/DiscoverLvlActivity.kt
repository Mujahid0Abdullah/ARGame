package com.achelmas.numart

import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Adapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * DiscoverLvlActivity displays a list of discoverable levels for the user,
 * manages user progress, and handles toolbar actions.
 */
class DiscoverLvlActivity : AppCompatActivity() {

    // UI components
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterOfDiscover
    private lateinit var discoverList: ArrayList<ModelOfDiscover>
    private lateinit var fullNameTxtView: TextView
    private lateinit var fullName: String
    private lateinit var age: String

    // Firebase references
    private lateinit var myReference: DatabaseReference
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_level1)
        Log.d("TAG", "giiilşilşili " + LanguageManager.loadSelectedLanguage(this))

        // Initialize Firebase Auth and UI elements
        mAuth = FirebaseAuth.getInstance()
        fullNameTxtView = findViewById(R.id.mainActivity_fullnameId)
        toolbar = findViewById(R.id.easyLvlActivity_toolBarId)
        recyclerView = findViewById(R.id.easyLvlActivity_recyclerViewId)

        // Set up toolbar with back arrow and menu
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.menu_off)
        itemsOfToolbar()

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        // Initialize user progress in Firebase if not already set
        val userId = mAuth!!.currentUser?.uid
        if (userId != null) {
            setInitialTargetProgress(userId)
        }

        // Fetch and display user's full name
        getFullNameProcess()
    }

    override fun onResume() {
        super.onResume()
        getDataFromFirebase()
    }

    /**
     * Fetches discoverable levels and user progress from Firebase,
     * and updates the RecyclerView with the data.
     */
    private fun getDataFromFirebase() {
        discoverList = ArrayList()
        myReference = FirebaseDatabase.getInstance().reference

        val userId = mAuth!!.currentUser!!.uid
        val userRef = myReference.child("Users").child(userId)

        // Get user's age to determine which dataset to use
        userRef.child("age").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(ageSnapshot: DataSnapshot) {
                val userAgeStr = ageSnapshot.getValue(String::class.java) ?: "10"
                val userAge = userAgeStr.toIntOrNull() ?: 10

                // Choose dataset based on age
                val datasetName = if (userAge > 10) "DiscoverHardLevel" else "Dicover game"
                val userProgressRef = myReference.child("UserProgress").child(userId).child("DiscoverEasyLevel")
                val targetsRef = myReference.child(datasetName)

                userProgressRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userProgressSnapshot: DataSnapshot) {
                        targetsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(targetsSnapshot: DataSnapshot) {
                                for (snapshot: DataSnapshot in targetsSnapshot.children) {
                                    val model = ModelOfDiscover()
                                    val questionMap = mutableMapOf<String, String>()
                                    val questionSnapshot = snapshot.child("Question")
                                    for (lang in questionSnapshot.children) {
                                        val langCode = lang.key ?: continue
                                        val text = lang.getValue(String::class.java) ?: continue
                                        questionMap[langCode] = text
                                    }
                                    model.question = questionMap
                                    model.target = snapshot.child("Target").value.toString()
                                    model.targetNumber = snapshot.child("Target Number").value.toString()
                                    model.shape1 = snapshot.child("shape1").value.toString()
                                    model.shape2 = snapshot.child("shape2").value.toString()
                                    model.shape3 = snapshot.child("shape3").value.toString()
                                    model.shape4 = snapshot.child("shape4").value.toString()
                                    // Unlock first target or if user progress says unlocked
                                    model.isUnlocked = userProgressSnapshot.child(model.targetNumber).value == true || model.targetNumber == "1"
                                    discoverList.add(model)
                                }
                                adapter = AdapterOfDiscover(this@DiscoverLvlActivity, discoverList)
                                recyclerView.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Fetches the user's full name and age from Firebase and updates the UI.
     * Applies color styling to the full name in the welcome message.
     */
    private fun getFullNameProcess() {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(mAuth!!.currentUser!!.uid)

        // Get full name and update welcome message
        reference.child("fullname").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fullName = snapshot.getValue(String::class.java)!!
                val text = "${resources.getString(R.string.welcome)}, ${fullName}! 👋"
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
     * Sets up the toolbar menu and handles menu item clicks.
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
     * Initializes the user's progress in Firebase if it does not already exist.
     * @param userId The unique ID of the user.
     */
    fun setInitialTargetProgress(userId: String) {
        val userProgressRef = FirebaseDatabase.getInstance().reference
            .child("UserProgress")
            .child(userId)

        userProgressRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.child("DiscoverEasyLevel").exists()) {
                    val initialProgress = mapOf(
                        "DiscoverEasyLevel" to mapOf(
                            "1" to true,
                            "2" to false,
                            "3" to false,
                            "4" to false,
                            "5" to false,
                            "6" to false,
                            "7" to false,
                            "9" to false,
                            "10" to false,
                            "11" to false,
                            "12" to false,
                            "13" to false,
                            "14" to false,
                            "15" to false,
                            "16" to false,
                            "17" to false,
                            "18" to false,
                            "19" to false,
                            "20" to false
                        )
                    )
                    userProgressRef.updateChildren(initialProgress)
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