package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
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

class addCenters : AppCompatActivity() {

    private lateinit var centerAdapter: CenterRequestsAdapter
    private lateinit var centerRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_centers)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        centerRecyclerView = findViewById(R.id.centersRecyclerView)

        auth = Firebase.auth
        databaseReference = Firebase.database.reference

        // Initialize adapters

        centerAdapter = CenterRequestsAdapter(emptyList())

        // Set adapters to RecyclerViews

        centerRecyclerView.adapter = centerAdapter

        // Fetch data from Firebase and update RecyclerViews

        fetchCentersFromFirebase()

        // Set layout manager for RecyclerViews

        centerRecyclerView.layoutManager = LinearLayoutManager(this)

        val notificationButton = findViewById<ImageView>(R.id.notifications)
        notificationButton.setOnClickListener {
            startActivity(Intent(this, adminNotifications::class.java))
        }
        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, adminHome::class.java))
        }
        val logbutton = findViewById<ImageButton>(R.id.logs)
        logbutton.setOnClickListener {
            startActivity(Intent(this, appLogs::class.java))
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, login::class.java))
        }
        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java).apply {
                putExtra("userType", "admin")
            })
        }

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
