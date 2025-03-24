package com.achelmas.numart

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.achelmas.numart.hardLevelMVC.HardLevelActivity

class Main : AppCompatActivity(){
    private lateinit var SumGameBtn : RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageManager.loadLocale(this)

        setContentView(R.layout.activity_mainpage)
        SumGameBtn = findViewById(R.id.sum_game)

        SumGameBtn.setOnClickListener {
            var intent = Intent(baseContext , MainActivity::class.java)
            startActivity(intent)
        }

    }
}