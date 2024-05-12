package com.ahmadmustafa.docconnect

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
    private var userType:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userType = intent.getStringExtra("userType").toString()
        if(userType=="patient") {

            val chatButton: ImageButton = findViewById(R.id.chats)
            chatButton.setOnClickListener {
                startActivity(Intent(this, chatBox::class.java).apply {
                    putExtra("userType", "patient")
                })
            }

            val profileButton: ImageButton = findViewById(R.id.profile)
            profileButton.setOnClickListener {
                startActivity(Intent(this, patientProfile::class.java))
            }

            val homeButton: ImageButton = findViewById(R.id.home)
            homeButton.setOnClickListener {
                startActivity(Intent(this, Home::class.java).apply {
                    putExtra("userType", "patient")
                })
            }

            val mapButton: ImageButton = findViewById(R.id.map)
            mapButton.setOnClickListener {
                startActivity(Intent(this, map::class.java).apply {
                    putExtra("userType", "patient")
                })
            }
            val appointButton: ImageButton = findViewById(R.id.appoint)
            appointButton.setOnClickListener {
                startActivity(Intent(this, manageAppointments::class.java).apply {
                    putExtra("userType", "patient")
                })
            }
        }
        else
        {
            val appointButton: ImageButton = findViewById(R.id.appoint)
            appointButton.setOnClickListener {
                showLoginDialog()
            }
            val chatButton: ImageButton = findViewById(R.id.chats)
            chatButton.setOnClickListener {
                showLoginDialog()
            }

            val profileButton: ImageButton = findViewById(R.id.profile)
            profileButton.setOnClickListener {
                showLoginDialog()
            }

            val homeButton: ImageButton = findViewById(R.id.home)
            homeButton.setOnClickListener {
                startActivity(Intent(this, Home::class.java))
            }

            val mapButton: ImageButton = findViewById(R.id.map)
            mapButton.setOnClickListener {
                startActivity(Intent(this, map::class.java))
            }
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
                topProfessionals.sortByDescending { it.rating } // Sort professionals by rating in descending order
                professionalAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun showLoginDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Login Required")
            .setMessage("Please login to access this feature.")
            .setPositiveButton("Login") { dialogInterface: DialogInterface, _: Int ->
                startActivity(Intent(this, login::class.java))
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
