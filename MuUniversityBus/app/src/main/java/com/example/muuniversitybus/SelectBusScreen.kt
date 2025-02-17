package com.example.muuniversitybus

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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class SelectBusScreen : AppCompatActivity() {
    private var backPressedTime = 0L
    private lateinit var routeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SelectBusScreenn", "onCreate started") // Add this first!
        setContentView(R.layout.activity_select_bus_screen)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false) // Disable default title

        // Get route ID from intent
        routeId = intent.getStringExtra("routeId") ?: run {
            Log.e("SelectBusScreen", "routeId is null")
            Toast.makeText(this, "Error: Route ID is missing!", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        Log.d("SelectBusScreen", "Received routeId: $routeId")

        // Initialize RecyclerView with an empty adapter
        val recyclerViewBuses = findViewById<RecyclerView>(R.id.recyclerViewBuses)
        recyclerViewBuses.layoutManager = LinearLayoutManager(this)
        val adapter = BusAdapter(emptyList()) { bus ->
            if (bus.id.isNotEmpty() && routeId.isNotEmpty()) {
                val intent1 = Intent(this@SelectBusScreen, Bus_detail::class.java)
                intent1.putExtra("busId", bus.id)
                intent1.putExtra("routeId", routeId)
                startActivity(intent1)
            } else {
                Log.e("SelectBusScreen", "Error: Missing busId or routeId")
                Toast.makeText(this, "Error: Missing bus details!", Toast.LENGTH_LONG).show()
            }

        }
        recyclerViewBuses.adapter = adapter

        // Initialize Firebase Database
        val database = FirebaseDatabase.getInstance()
        val busesRef = database.getReference("buses")
        busesRef.orderByChild("routeId").equalTo(routeId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.e("SelectBusScreen", "No buses found for routeId: $routeId")
                    Toast.makeText(this@SelectBusScreen, "No buses found for this route!", Toast.LENGTH_SHORT).show()
                    return
                }

                val buses = mutableListOf<Bus>()
                for (busSnapshot in snapshot.children) {
                    val busName = busSnapshot.child("name").value?.toString() ?: continue
                    val busId = busSnapshot.key ?: continue
                    buses.add(Bus(busId, busName))
                }

                if (buses.isEmpty()) {
                    Log.e("SelectBusScreen", "No buses found for routeId: $routeId")
                    Toast.makeText(this@SelectBusScreen, "No buses found for this route!", Toast.LENGTH_SHORT).show()
                } else {
                    adapter.updateBuses(buses)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SelectBusScreen", "Failed to load buses: ${error.message}")
                Toast.makeText(this@SelectBusScreen, "Failed to load buses: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Bottom navigation setup
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {

                    startActivity(Intent(this, Home::class.java))
                    true
                }
                R.id.nav_profile -> {

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

        // Set the selected item on bottom navigation
        bottomNavigationView.selectedItemId = 0

        // Back button in Toolbar
        val buttonBack = findViewById<ImageButton>(R.id.buttonBack)
        buttonBack.setOnClickListener {
            onBackPressed()
        }
    }

}

// Bus data class
data class Bus(
    val id: String,
    val name: String
)

// Bus adapter for RecyclerView
class BusAdapter(
    private var buses: List<Bus>,
    private val onItemClick: (Bus) -> Unit
) : RecyclerView.Adapter<BusAdapter.BusViewHolder>() {

    fun updateBuses(newBuses: List<Bus>) {
        this.buses = newBuses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): BusViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return BusViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        val bus = buses[position]
        holder.bind(bus)
        holder.itemView.setOnClickListener { onItemClick(bus) }
    }

    override fun getItemCount(): Int = buses.size

    class BusViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val textView: android.widget.TextView = itemView.findViewById(android.R.id.text1)

        fun bind(bus: Bus) {
            textView.text = bus.name
        }
    }
}
