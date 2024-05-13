package com.ahmadmustafa.docconnect
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class addProfessional : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var dbReference: DatabaseReference
    private lateinit var professionalReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ViewProfessionalRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_professional)

        auth = Firebase.auth
        dbReference = Firebase.database.reference
        professionalReference =dbReference.child("professionals")
        databaseReference = FirebaseDatabase.getInstance().getReference("centers")
        recyclerView = findViewById(R.id.professionalsRecyclerView)
        adapter = ViewProfessionalRequestAdapter(mutableListOf())

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, centreHome::class.java))
        }

        fetchProfessionalsFromFirebase()
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
}
