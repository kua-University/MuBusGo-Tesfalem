package com.example.muuniversitybus

import RouteAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {

    private var backPressedTime = 0L // Tracks the time of the first back press
    private lateinit var adapter: RouteAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var routesRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize Back Button in Toolbar
        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Disable default title

        buttonBack.setOnClickListener {
            // Navigate back to the previous activity
            finish()
        }

        // Initialize RecyclerView
        val recyclerViewRoutes = findViewById<RecyclerView>(R.id.recyclerViewRoutes)
        recyclerViewRoutes.layoutManager = LinearLayoutManager(this)
        adapter = RouteAdapter(emptyList()) { route ->
            Log.d("HomeActivity", "Clicked on route: ${route.id}") // Debug log
            val intent = Intent(this@Home, SelectBusScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("routeId", route.id)
            Log.d("HomeActivity", "Navigating to SelectBusScreen with routeId: ${route.id}")
            startActivity(intent)
        }
        recyclerViewRoutes.adapter = adapter

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        routesRef = database.getReference("routes")

        // Load Routes from Firebase
        loadRoutes()

        // Add search functionality
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchRoutesByName(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchRoutesByName(it) }
                return true
            }
        })

        // Handle back button events using OnBackPressedDispatcher
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    finishAffinity() // Exit the app
                } else {
                    Toast.makeText(this@Home, "Press back again to exit the app", Toast.LENGTH_SHORT).show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Log.d("HomeActivity", "Home Clicked in Bottom Nav")
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
    }



    // Handle toolbar menu item clicks


    private fun loadRoutes() {
        routesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val routes = mutableListOf<Route>()
                for (routeSnapshot in snapshot.children) {
                    val routeId = routeSnapshot.key ?: continue
                    val routeName = routeSnapshot.child("name").value.toString()
                    val imageResId = R.drawable.mu_arid_adihaki
                    routes.add(Route(routeId, routeName, imageResId))
                }
                adapter.updateRoutes(routes) // Ensure adapter is properly initialized
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Home, "Failed to load routes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchRoutesByName(query: String) {
        routesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val routes = mutableListOf<Route>()
                for (routeSnapshot in snapshot.children) {
                    val routeId = routeSnapshot.key ?: continue
                    val routeName = routeSnapshot.child("name").value.toString()
                    if (routeName.contains(query, ignoreCase = true)) {
                        routes.add(Route(routeId, routeName))
                    }
                }

                if (::adapter.isInitialized) {
                    adapter.updateRoutes(routes)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Home, "Failed to search routes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}