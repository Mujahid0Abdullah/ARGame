package com.achelmas.numart

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private var loginButton: CardView? = null
    private var emailEditText: TextInputLayout? = null
    private var passwordEditText: TextInputLayout? = null
    private var loadingAnimation: LottieAnimationView? = null
    private var loginTextFromLoginBtn: TextView? = null

    private var signupButton: TextView? = null
    private var forgotButton: TextView? = null

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        loginButton = findViewById(R.id.loginActivity_loginButtonId)
        loginButton!!.setOnClickListener {

            //Login Function
            loginProcess()
        }

        signupButton = findViewById(R.id.loginActivity_signupButtonId)
        signupButton!!.setOnClickListener {
            var intent = Intent(this , SignupActivity::class.java)
            startActivity(intent)
        }

        forgotButton = findViewById(R.id.loginActivity_forgotPasswordButtonId)
        forgotButton!!.setOnClickListener {
            var intent = Intent(this , ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginProcess() {
        //Initialize the variables
        emailEditText = findViewById(R.id.loginActivity_emailId)
        passwordEditText = findViewById(R.id.loginActivity_passwordId)
        loadingAnimation = findViewById(R.id.loginActivity_loginButtonId_loadingLottie)
        loginTextFromLoginBtn = findViewById(R.id.loginActivity_loginButtonId_textView)

        var email: String = emailEditText!!.editText!!.text.toString().trim()
        var password: String = passwordEditText!!.editText!!.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            if (email.isEmpty()) {
                Toast.makeText(this, resources.getString(R.string.please_enter_your_email), Toast.LENGTH_SHORT).show()
            }
            else if (password.isEmpty()) {
                Toast.makeText(this, resources.getString(R.string.please_enter_your_password), Toast.LENGTH_SHORT).show()
            }
        }
        else {
            //set visibility of Loading and textview of Button
            loadingAnimation!!.visibility = View.VISIBLE
            loginTextFromLoginBtn!!.visibility = View.GONE

            // Hide the keyboard
            var inputMethodManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(loginButton!!.getWindowToken(), 0)

            mAuth.signInWithEmailAndPassword(email , password).addOnCompleteListener {task ->
                if (task.isSuccessful) {

                    Toast.makeText(this@LoginActivity, resources.getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                    loadingAnimation!!.visibility = View.GONE
                    loginTextFromLoginBtn!!.visibility = View.VISIBLE

                    // Set the flag in SharedPreferences indicating a successful login
                    setLoginFlag()

                    // Redirect to MainActivity
                    var intent = Intent(this@LoginActivity , MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                }
                else {
                    Toast.makeText(this, resources.getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                    loadingAnimation!!.visibility = View.GONE
                    loginTextFromLoginBtn!!.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setLoginFlag() {
        val prefs: SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putBoolean("loginCompleted", true)
        editor.apply()
    }

}