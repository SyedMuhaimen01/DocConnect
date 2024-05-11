package com.ahmadmustafa.docconnect

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class Home : AppCompatActivity() {
    private lateinit var popularDoctorRecyclerView: RecyclerView
    private lateinit var professionalAdapter: popularDoctorAdapter
    private val topProfessionals: MutableList<Professional> = mutableListOf()

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
                topProfessionals.sortByDescending { it.rating }
                professionalAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        val chatButton = findViewById<ImageButton>(R.id.chats)
        chatButton.setOnClickListener {
            startActivity(Intent(this, chatBox::class.java))
        }

        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java))
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, patientProfile::class.java))
        }

    }

}
