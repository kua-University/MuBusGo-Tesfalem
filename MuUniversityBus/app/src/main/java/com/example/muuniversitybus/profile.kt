package com.example.muuniversitybus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class profile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Disable default title

        // Initialize views
        val textViewUsername = findViewById<TextView>(R.id.textViewUserName)
        val textViewEmail = findViewById<TextView>(R.id.textViewEmail)
        val textViewPassword = findViewById<TextView>(R.id.textViewPassword)
        val buttonEditProfile = findViewById<Button>(R.id.buttonEditProfile)
        val buttonLogout = findViewById<Button>(R.id.buttonLogout)
        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)

        // Load profile data from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "No Username")
        val password = sharedPreferences.getString("password", "No Password")
        val email = sharedPreferences.getString("email", "No email")

        // Set profile data to TextViews
        textViewUsername.text = username
        textViewPassword.text = password
        textViewEmail.text = email

        // Edit Profile Button Click Listener
        buttonEditProfile.setOnClickListener {
            // Navigate to Edit Profile Activity
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        // Logout Button Click Listener
        buttonLogout.setOnClickListener {
            // Clear SharedPreferences (simulate logout)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Show a toast message
            Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()

            // Navigate to Login Activity
            val intent = Intent(this, Sign_In::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Close the current activity
        }

        // Back Button Click Listener
        buttonBack.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Bottom Navigation Setup
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Navigate to Home Activity
                    val intent = Intent(this, Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish() // Close the current activity
                    true
                }
                R.id.nav_profile -> {
                    Log.d("HomeActivity", "Profile Clicked in Bottom Nav")
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(Intent(this, profile::class.java))

                    true
                }
                else -> false
            }
        }

        // Set toolbar text color
        toolbar.post {
            for (i in 0 until toolbar.childCount) {
                val view = toolbar.getChildAt(i)
                if (view is TextView) {
                    view.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
            }
        }

        bottomNavigationView.selectedItemId = R.id.nav_profile
    }

}
