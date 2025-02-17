package com.example.muuniversitybus


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback

class welcome : AppCompatActivity() {
    private var backPressedOnce = false
    private val handler = Handler(Looper.getMainLooper()) // Declare handler at class level
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        var circularButton1 = findViewById<ImageButton>(R.id.circularButton1)
        circularButton1.setOnClickListener {
            val intent1 = Intent(this, Sign_In::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent1)
            finish()
        }

        // Handle back press using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedOnce) {
                    finishAffinity() // Exits the app completely
                } else {
                    backPressedOnce = true
                    Toast.makeText(this@welcome, "Press back again to exit", Toast.LENGTH_SHORT).show()

                    // Reset backPressedOnce after 2 seconds
                    handler.postDelayed({ backPressedOnce = false }, 2000)
                }
            }
        })
    }
    }