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
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

class Bus_detail : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var routeId: String
    private lateinit var busId: String
    private lateinit var busMarker: Marker
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private var isMoving = false
    private lateinit var routeCoordinates: List<LatLng>

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

                    fetchRoute(start, destination)

                    googleMap.addMarker(
                        MarkerOptions().position(start)
                            .title("Start: ${getLocationName(start)}")
                    )

                    googleMap.addMarker(
                        MarkerOptions().position(destination)
                            .title("Destination: ${getLocationName(destination)}")
                    )

                    busMarker = googleMap.addMarker(
                        MarkerOptions().position(start).title("Bus: $busId")
                    ) ?: return 

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15f)) 
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Bus_detail, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRoute(start: LatLng, destination: LatLng) {
        val apiKey = "googleApi" 
        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${start.latitude},${start.longitude}&destination=${destination.latitude},${destination.longitude}&key=$apiKey"

        CoroutineScope(Dispatchers.IO).launch {
            val response = makeApiCall(url)
            withContext(Dispatchers.Main) {
                handleApiResponse(response)
            }
        }
    }

    private fun makeApiCall(url: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        return try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handleApiResponse(response: String?) {
        if (response != null) {
            val jsonObject = JSONObject(response)
            val routes = jsonObject.getJSONArray("routes")
            if (routes.length() > 0) {
                val points = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
                routeCoordinates = decodePoly(points)

               
                startBusMovement()
            } else {
                Toast.makeText(this, "No routes found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if (result and 1 != 0) {
                -(result shr 1)
            } else {
                result shr 1
            }
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlng = if (result and 1 != 0) {
                -(result shr 1)
            } else {
                result shr 1
            }
            lng += dlng

            val p = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(p)
        }
        return poly
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

            animateBusMovement(start, end, 6000) { 
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
                val fraction = elapsed.toFloat() 
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

    private fun getLocationName(latLng: LatLng): String {
        return "(${latLng.latitude}, ${latLng.longitude})" 
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
}
