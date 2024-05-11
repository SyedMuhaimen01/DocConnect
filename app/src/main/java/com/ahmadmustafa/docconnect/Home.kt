package com.ahmadmustafa.docconnect

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import java.io.IOException

class Home : AppCompatActivity() {
    private lateinit var popularDoctorRecyclerView: RecyclerView
    private lateinit var professionalAdapter: popularDoctorAdapter
    private val topProfessionals: MutableList<Professional> = mutableListOf()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationTextView: TextView

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        popularDoctorRecyclerView = findViewById(R.id.popularDoctorRecyclerView)
        professionalAdapter = popularDoctorAdapter(topProfessionals)

        popularDoctorRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Home, LinearLayoutManager.VERTICAL, false)
            adapter = professionalAdapter
        }

        fetchTopProfessionals()

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            val intent = Intent(this, manageAppointments::class.java)
            startActivity(intent)
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            val intent = Intent(this, doctorProfile::class.java)
            startActivity(intent)
        }

        val mapButton=findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            val intent = Intent(this,map::class.java)
            startActivity(intent)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        locationTextView = findViewById(R.id.location)


        getUserLocation()
    }

    private fun fetchTopProfessionals() {
        val database = FirebaseDatabase.getInstance().reference.child("professionals")
        val query = database.orderByChild("rating").limitToLast(20) // Fetch top 20 professionals based on rating
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                topProfessionals.clear()
                for (snapshot in dataSnapshot.children) {
                    val professional = snapshot.getValue(Professional::class.java)
                    professional?.let {
                        topProfessionals.add(it)
                    }
                }
                topProfessionals.sortByDescending { it.rating } // Sort professionals by rating in descending order
                professionalAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // Got last known location
                    location?.let {
                        val geocoder = Geocoder(this)
                        try {
                            val addresses: MutableList<Address>? = geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            )
                            if (addresses != null) {
                                val locality: String? = addresses[0].locality // City
                                val subLocality: String? = addresses[0].subLocality // Sub-locality (neighborhood)
                                val thoroughfare: String? = addresses[0].thoroughfare // Street name
                                val addressParts = mutableListOf<String>()
                                if (!subLocality.isNullOrEmpty()) {
                                    addressParts.add(subLocality)
                                }
                                if (!thoroughfare.isNullOrEmpty()) {
                                    addressParts.add(thoroughfare)
                                }
                                val locationText = addressParts.joinToString(", ") + ", $locality"
                                val country: String = addresses[0].countryName ?: ""
                                // Display user's current address in the TextView
                                locationTextView.text = "$locationText, $country"
                            } else {
                                locationTextView.text = "Location not found"
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
        } else {
            // Handle the case where location permission is not granted
            // You should request the permission here
        }
    }
}
