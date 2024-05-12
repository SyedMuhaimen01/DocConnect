package com.ahmadmustafa.docconnect

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class adminHome : AppCompatActivity() {
    private lateinit var professionalAdapter: viewProfessionalAdapter
    private lateinit var centerAdapter: viewCenterAdapter
    private lateinit var professionalRecyclerView: RecyclerView
    private lateinit var centerRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        professionalRecyclerView = findViewById(R.id.professionalRecyclerView)
        centerRecyclerView = findViewById(R.id.centerRecyclerView)

        auth = Firebase.auth
        databaseReference = Firebase.database.reference

        // Initialize adapters
        professionalAdapter = viewProfessionalAdapter(emptyList())
        centerAdapter = viewCenterAdapter(emptyList())

        // Set adapters to RecyclerViews
        professionalRecyclerView.adapter = professionalAdapter
        centerRecyclerView.adapter = centerAdapter

        // Fetch data from Firebase and update RecyclerViews
        fetchProfessionalsFromFirebase()
        fetchCentersFromFirebase()

        // Set layout manager for RecyclerViews
        professionalRecyclerView.layoutManager = LinearLayoutManager(this)
        centerRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set click listeners for buttons
        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, adminHome::class.java))
        }

        val chatButton = findViewById<ImageButton>(R.id.chat)
        chatButton.setOnClickListener {
            // Handle button click
        }

        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            // Handle button click
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            // Handle button click
        }
    }

    private fun fetchProfessionalsFromFirebase() {
        val professionalsRef = databaseReference.child("professionals")
        professionalsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val professionals = mutableListOf<Professional>()
                for (professionalSnapshot in snapshot.children) {
                    val professional = professionalSnapshot.getValue(Professional::class.java)
                    professional?.let {
                        professionals.add(it)
                    }
                }
                professionalAdapter.updateData(professionals)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event
            }
        })
    }

    private fun fetchCentersFromFirebase() {
        val centersRef = databaseReference.child("centers")
        centersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val centers = mutableListOf<Center>()
                for (centerSnapshot in snapshot.children) {
                    val center = centerSnapshot.getValue(Center::class.java)
                    center?.let {
                        centers.add(it)
                    }
                }
                centerAdapter.updateData(centers)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event
            }
        })
    }
}
