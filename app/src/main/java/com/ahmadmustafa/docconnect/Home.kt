package com.ahmadmustafa.docconnect

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class Home : AppCompatActivity() {
    private lateinit var popularDoctorRecyclerView: RecyclerView
    private lateinit var professionalAdapter: popularDoctorAdapter
    private val topProfessionals: MutableList<Professional> = mutableListOf()
    private var userType:String=""
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().getReference("patients")
        sharedPreferences = getSharedPreferences("patients", Context.MODE_PRIVATE)
        fetchLoggedInpatientData()
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
        professionalAdapter = popularDoctorAdapter(topProfessionals) { professional ->
            // Start BookAppointment activity and pass professional details as extras
            val intent = Intent(this, bookAppointment::class.java).apply {
                putExtra("professionalId", professional.id)
                putExtra("professionalName", professional.name)
                putExtra("professionalSpecialization", professional.specialization)
                // Add more details as needed
            }
            startActivity(intent)
        }


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

    private fun fetchLoggedInpatientData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val userRef = databaseReference.child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val center = snapshot.getValue(Patient::class.java)
                        center?.let {
                            savePatientToSharedPreferences(it)
                        }
                    } else {
                        Log.d("FetchPatientData", "No data found for user with UID: $userId")
                        // Handle the case where no user data is found
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FetchPatientData", "Database error: ${error.message}")
                    // Handle database error
                }
            })
        } ?: Log.e("FetchPatientData", "Current user is null")
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

    private fun savePatientToSharedPreferences(patient: Patient) {
        val editor = sharedPreferences.edit()
        editor.putString("id", patient.id)
        editor.putString("name", patient.name)
        editor.putString("email", patient.email)
        editor.putString("contactNumber", patient.contactNumber)
        editor.putString("cnic", patient.cnic)
        editor.putString("password", patient.password)
        editor.putString("picture", patient.picture)
        editor.apply()
    }

    override fun onStart() {
        super.onStart()
        // Attach an authentication state listener
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        // Detach the authentication state listener
        auth.removeAuthStateListener(authListener)
    }

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // User is signed in, fetch user details again
            fetchLoggedInpatientData()
        } else {
            // No user is signed in, redirect to login screen
            //startActivity(Intent(this, login::class.java))
            //finish()
        }
    }
}
