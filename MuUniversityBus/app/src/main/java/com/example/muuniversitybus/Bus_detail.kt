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
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class Bus_detail : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var routeId: String
    private lateinit var busId: String
    private lateinit var busMarker: Marker
    private var routeCoordinates: List<LatLng> = listOf()
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private var isMoving = false

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

                    fetchRouteFromGoogleAPI(start, destination)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Bus_detail, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRouteFromGoogleAPI(start: LatLng, destination: LatLng) {
        val apiKey = "YOUR_GOOGLE_MAPS_API_KEY"
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${start.latitude},${start.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=driving" +
                "&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@Bus_detail, "Failed to get route", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val jsonResponse = JSONObject(responseBody)
                    val routes = jsonResponse.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val legs = routes.getJSONObject(0).getJSONArray("legs")
                        val steps = legs.getJSONObject(0).getJSONArray("steps")

                        val path = mutableListOf<LatLng>()
                        for (i in 0 until steps.length()) {
                            val step = steps.getJSONObject(i)
                            val endLocation = step.getJSONObject("end_location")
                            val lat = endLocation.getDouble("lat")
                            val lng = endLocation.getDouble("lng")
                            path.add(LatLng(lat, lng))
                        }

                        runOnUiThread {
                            updateRouteOnMap(path)
                        }
                    }
                }
            }
        })
    }

    private fun updateRouteOnMap(route: List<LatLng>) {
        routeCoordinates = route

        googleMap.addPolyline(
            PolylineOptions().addAll(routeCoordinates).color(Color.BLUE).width(10f)
        )

        busMarker = googleMap.addMarker(
            MarkerOptions().position(routeCoordinates[0]).title("Bus: $busId")
        )!!

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routeCoordinates[0], 15f))

        startBusMovement()
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

            animateBusMovement(start, end, 3000) {
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
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(busMarker.position))
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
}
