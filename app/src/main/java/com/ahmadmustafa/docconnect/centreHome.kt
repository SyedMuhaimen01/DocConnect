package com.ahmadmustafa.docconnect
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class centreHome : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var dbReference: DatabaseReference
    private lateinit var professionalReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AffiliatedProfessionalsAdapter
    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_centre_home)

        auth = Firebase.auth
        dbReference = Firebase.database.reference
        professionalReference =dbReference.child("professionals")
        databaseReference = FirebaseDatabase.getInstance().getReference("centers")
        sharedPreferences = getSharedPreferences("centers", Context.MODE_PRIVATE)
        recyclerView = findViewById(R.id.professionalsRecyclerView)
        fetchLoggedInCenterData()

        adapter = AffiliatedProfessionalsAdapter(emptyList())
        recyclerView.adapter = adapter
        fetchProfessionalsFromFirebase()
        recyclerView.layoutManager = LinearLayoutManager(this)
        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, centreHome::class.java))
        }



        val addProfessionalButton= findViewById<ImageButton>(R.id.addProfessional)
        addProfessionalButton.setOnClickListener {
            startActivity(Intent(this, addProfessional::class.java))
        }
        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java).apply {
                putExtra("userType", "center")
            })
        }

        val notificationButton = findViewById<ImageView>(R.id.notifications)
        notificationButton.setOnClickListener {
            startActivity(Intent(this, centerNotifications::class.java))
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, centerProfile::class.java))
        }

    }


    private fun fetchProfessionalsFromFirebase() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val userRef = databaseReference.child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val center = snapshot.getValue(Center::class.java)
                        center?.let {
                            Log.d("FetchProfessionals", "Center name: ${center.name}")
                            // Fetch professionals only affiliated with the current center
                            val professionalsRef = dbReference.child("professionals")
                            professionalsRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(professionalSnapshot: DataSnapshot) {
                                    Log.d("FetchProfessionals", "Total professionals found: ${professionalSnapshot.childrenCount}")
                                    val professionals = mutableListOf<Professional>()
                                    for (professional in professionalSnapshot.children) {
                                        val prof = professional.getValue(Professional::class.java)
                                        prof?.let {
                                            Log.d("FetchProfessionals", "Professional data: $it")
                                            // Check if the professional's affiliation contains the center's name
                                            val affiliationParts = it.affiliation?.split("|")
                                            val professionalCenterName = affiliationParts?.get(0)?.trim() // Trim whitespace
                                            Log.d("FetchProfessionals", "Professional Center Name: $professionalCenterName")
                                            if (professionalCenterName == center.name) {
                                                professionals.add(it)
                                                Log.d("FetchProfessionals", "Professional added: ${it.name}")
                                            }
                                        }
                                    }
                                    adapter.updateData(professionals)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("FetchProfessionals", "Database error: ${error.message}")
                                    // Handle onCancelled event
                                }
                            })

                        }
                    } else {
                        Log.d("FetchCenterData", "No data found for user with UID: $userId")
                        // Handle the case where no user data is found
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FetchCenterData", "Database error: ${error.message}")
                    // Handle database error
                }
            })
        } ?: Log.e("FetchCenterData", "Current user is null")
    }



    private fun fetchLoggedInCenterData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val userRef = databaseReference.child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val center = snapshot.getValue(Center::class.java)
                        center?.let {
                            saveCenterToSharedPreferences(it)
                            val centerTitleTextView = findViewById<TextView>(R.id.centerTitle)
                            centerTitleTextView.text = it.name
                            Log.d("FetchCenterData", "Center name fetched: ${it.name}")
                        }
                    } else {
                        Log.d("FetchCenterData", "No data found for user with UID: $userId")
                        // Handle the case where no user data is found
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FetchCenterData", "Database error: ${error.message}")
                    // Handle database error
                }
            })
        } ?: Log.e("FetchCenterData", "Current user is null")
    }


    private fun saveCenterToSharedPreferences(center: Center) {
        val editor = sharedPreferences.edit()
        editor.putString("centerId", center.id)
        editor.putString("name", center.name)
        editor.putString("email", center.email)
        editor.putString("contact", center.contactNumber)
        editor.putString("address", center.address)
        editor.putString("category", center.category)
        editor.putString("password", center.password)
        editor.putBoolean("status", center.centerStatus)
        editor.putString("picture", center.picture)
        editor.putString("certificate", center.certificate)
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
            fetchLoggedInCenterData()
        } else {
            // No user is signed in, redirect to login screen
            startActivity(Intent(this, login::class.java))
            finish()
        }
    }

}
