package com.example.muuniversitybus

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import kotlin.random.Random

class Bus_detail : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var routeId: String
    private lateinit var busId: String
    private lateinit var routeCoordinates: List<LatLng>
    private lateinit var busMarker: Marker
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private var isMoving = false
    private lateinit var busStartPosition: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = resources.getColor(android.R.color.white, theme)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.map_normal -> googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                R.id.map_satellite -> googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                R.id.map_terrain -> googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                R.id.map_hybrid -> googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            drawerLayout.closeDrawers()
            true
        }

        routeId = intent.getStringExtra("routeId") ?: finish().let { return }
        busId = intent.getStringExtra("busId") ?: finish().let { return }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val database = FirebaseDatabase.getInstance()
        val routeRef = database.getReference("routes/$routeId")

        routeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val startLat = snapshot.child("start/latitude").getValue(Double::class.java)
                val startLng = snapshot.child("start/longitude").getValue(Double::class.java)
                val destLat = snapshot.child("destination/latitude").getValue(Double::class.java)
                val destLng = snapshot.child("destination/longitude").getValue(Double::class.java)

                if (startLat != null && startLng != null && destLat != null && destLng != null) {
                    val start = LatLng(startLat, startLng)
                    val destination = LatLng(destLat, destLng)

                    busStartPosition = getRandomPointOnRoute(start, destination)
                    routeCoordinates = generateWaypoints(start, destination, 10) // Start from the static start point

                    // Add markers for start and destination with titles
                    googleMap.addMarker(
                        MarkerOptions().position(start)
                            .title("Start: ${getLocationName(start)}")
                    )

                    googleMap.addMarker(
                        MarkerOptions().position(destination)
                            .title("Destination: ${getLocationName(destination)}")
                    )

                    // Initialize the bus marker with the bus ID as the title
                    busMarker = googleMap.addMarker(
                        MarkerOptions().position(busStartPosition).title("Bus: $busId")
                    ) ?: return // Ensure the marker is added successfully

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15f)) // Focus on the static start point

                    googleMap.addPolyline(
                        PolylineOptions().addAll(routeCoordinates).color(Color.BLUE).width(10f)
                    )

                    startBusMovement()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Bus_detail, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startBusMovement() {
        if (!isMoving) {
            isMoving = true
            moveBusSmoothly()
        }
    }

    private fun moveBusSmoothly() {
        if (currentIndex < routeCoordinates.size - 1) {
            val start = routeCoordinates[currentIndex]
            val end = routeCoordinates[currentIndex + 1]

            animateBusMovement(start, end, 6000) { // Increased duration to slow down the bus
                currentIndex++
                moveBusSmoothly()
            }
        } else {
            currentIndex = 0
            moveBusSmoothly()
        }
    }

    private fun animateBusMovement(start: LatLng, end: LatLng, duration: Long, onEnd: () -> Unit) {
        val startTime = System.currentTimeMillis()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                val fraction = elapsed.toFloat() / duration
                if (fraction < 1.0) {
                    busMarker.position = interpolate(start, end, fraction)

                    if (currentIndex % 3 == 0) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(busMarker.position))
                    }

                    handler.postDelayed(this, 100)
                } else {
                    busMarker.position = end
                    onEnd()
                }
            }
        })
    }

    private fun interpolate(start: LatLng, end: LatLng, fraction: Float): LatLng {
        val lat = start.latitude + (end.latitude - start.latitude) * fraction
        val lng = start.longitude + (end.longitude - start.longitude) * fraction
        return LatLng(lat, lng)
    }

    private fun getRandomPointOnRoute(start: LatLng, end: LatLng): LatLng {
        val lat = start.latitude + (end.latitude - start.latitude) * Random.nextFloat()
        val lng = start.longitude + (end.longitude - start.longitude) * Random.nextFloat()
        return LatLng(lat, lng)
    }

    private fun generateWaypoints(start: LatLng, end: LatLng, numPoints: Int): List<LatLng> {
        val waypoints = mutableListOf<LatLng>()
        waypoints.add(start) // Include the static start point
        for (i in 1 until numPoints) {
            val fraction = i.toFloat() / numPoints
            val lat = start.latitude + (end.latitude - start.latitude) * fraction
            val lng = start.longitude + (end.longitude - start.longitude) * fraction
            waypoints.add(LatLng(lat, lng))
        }
        waypoints.add(end) // Include the destination
        return waypoints
    }

    private fun getLocationName(latLng: LatLng): String {
        return "(${latLng.latitude}, ${latLng.longitude})" // Replace with a geocoding API if needed
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
}