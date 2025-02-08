package com.achelmas.numart

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
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

class MainActivity : AppCompatActivity() {


    private lateinit var toolbar: Toolbar
    private lateinit var fullNameTxtView: TextView
    private lateinit var fullName: String
    private lateinit var age: String
    private lateinit var easyLevelBtn : RelativeLayout
    private lateinit var mediumLevelBtn : RelativeLayout
    private lateinit var hardLevelBtn : RelativeLayout

    //Firebase
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set language
        LanguageManager.loadLocale(this)

        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        // Initialize variables
        toolbar = findViewById(R.id.mainActivity_toolBarId)
        fullNameTxtView = findViewById(R.id.mainActivity_fullnameId)
        easyLevelBtn = findViewById(R.id.mainActivity_easyLevelId)
        mediumLevelBtn = findViewById(R.id.mainActivity_mediumLevelId)
        hardLevelBtn = findViewById(R.id.mainActivity_hardLevelId)

        // Set items ( Profile and Settings ) in Toolbar
        toolbar.inflateMenu(R.menu.menu_off)
        // Handle menu item clicks
        itemsOfToolbar()

        // get fullname from firebase
        getFullNameProcess()

        // Handle button clicks
        easyLevelBtn.setOnClickListener {
            var intent = Intent(baseContext , EasyLevelActivity::class.java)
            startActivity(intent)
        }
        mediumLevelBtn.setOnClickListener {
            var intent = Intent(baseContext , MediumLevelActivity::class.java)
            startActivity(intent)
        }
        hardLevelBtn.setOnClickListener {
            var intent = Intent(baseContext , HardLevelActivity::class.java)
            startActivity(intent)
        }

        val userId = mAuth!!.currentUser?.uid
        if (userId != null) {
            setInitialTargetProgress(userId)  // Kullanıcının ilerlemesini Firebase'e kaydeder
        }
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

    fun setInitialTargetProgress(userId: String) {
        val userProgressRef = FirebaseDatabase.getInstance().reference
            .child("UserProgress")
            .child(userId)  // Kullanıcıya özel

        val initialProgress = mapOf(
            "A1EasyLevel" to mapOf(
                "1" to true,   // İlk hedef açık
                "2" to false,  // İkinci hedef kapalı
                "3" to false,   // Üçüncü hedef kapalı
                "4" to false,
                "5" to false,
                "6" to false,
                "7" to false,
                "8" to false
            ),
            "A2MediumLevel" to mapOf(
                "1" to false,   // İlk hedef açık
                "2" to false,  // İkinci hedef kapalı
                "3" to false,   // Üçüncü hedef kapalı
                "4" to false,
                "5" to false,
                "6" to false,
                "7" to false,
                "8" to false
            ),
            "A3HardLevel" to mapOf(
                "1" to false,   // İlk hedef açık
                "2" to false,  // İkinci hedef kapalı
                "3" to false,   // Üçüncü hedef kapalı
                "4" to false,
                "5" to false,
                "6" to false,
                "7" to false,
                "8" to false
            )
        )

        // Firebase'e kaydetme işlemi
        userProgressRef.setValue(initialProgress)
            .addOnSuccessListener {}
            .addOnFailureListener {}
    }


}