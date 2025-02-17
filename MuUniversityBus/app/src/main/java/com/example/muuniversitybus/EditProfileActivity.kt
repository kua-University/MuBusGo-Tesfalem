package com.example.muuniversitybus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val buttonSaveProfile = findViewById<Button>(R.id.buttonSaveProfile)
        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)

        val sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val currentUsername = sharedPreferences.getString("username", "")
        val currentEmail = sharedPreferences.getString("email", "")
        val currentPassword = sharedPreferences.getString("password", "")

        editTextUsername.setText(currentUsername)
        editTextEmail.setText(currentEmail)
        editTextPassword.setText(currentPassword)

        buttonBack.setOnClickListener {
            val intent = Intent(this, profile::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Close the current activity
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
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


        buttonSaveProfile.setOnClickListener {
            val newUsername = editTextUsername.text.toString()
            val newPassword = editTextPassword.text.toString()
            val newEmail = editTextEmail.text.toString()

            if (newUsername.isNotEmpty() && newPassword.isNotEmpty() && newEmail.isNotEmpty()) {
                val editor = sharedPreferences.edit()
                editor.putString("username", newUsername)
                editor.putString("password", newPassword)
                editor.putString("email", newEmail)
                editor.apply()

                // Show a toast message
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()

                // Navigate back to Profile Activity

                val intent = Intent(this, profile::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish() // Close the current activity
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}