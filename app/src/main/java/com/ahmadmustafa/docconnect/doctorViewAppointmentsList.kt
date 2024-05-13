package com.ahmadmustafa.docconnect
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class doctorViewAppointmentsList : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_view_appointments_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("professionals")
        sharedPreferences = getSharedPreferences("professionals", Context.MODE_PRIVATE)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        fetchLoggedInProfessionalData()

        val monthTextView = findViewById<TextView>(R.id.month)
        val yearTextView = findViewById<TextView>(R.id.year)

        val calendar = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.time)

        monthTextView.text = currentMonth
        yearTextView.text = currentYear
        setCurrentWeekDayAndDate()

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, doctorViewAppointmentsList::class.java))
        }

        val chatButton = findViewById<ImageButton>(R.id.chat)
        chatButton.setOnClickListener {
            startActivity(Intent(this, searchUsers::class.java).apply {
                putExtra("userType", "professional")
            })
        }

        val setWorkingHoursButton = findViewById<ImageButton>(R.id.workingHours)
        setWorkingHoursButton.setOnClickListener {
            startActivity(Intent(this, setWorkingHours::class.java).apply {
                putExtra("userType", "professional")
            })
        }

        val appointButton = findViewById<ImageButton>(R.id.appoint)
        appointButton.setOnClickListener {
            startActivity(Intent(this, doctorViewAppointmentsList::class.java))
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, doctorProfile::class.java))
        }

        val recyclerView = findViewById<RecyclerView>(R.id.userRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = appointmentListAdapter(emptyList())

        recyclerView.adapter = adapter

        // Populate RecyclerView with appointment data
        populateRecyclerView(adapter)
    }

    private fun fetchLoggedInProfessionalData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val userRef = databaseReference.child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val center = snapshot.getValue(Professional::class.java)
                        center?.let {
                            saveProfessionalToSharedPreferences(it)
                        }
                    } else {
                        Log.d("FetchProfessionalData", "No data found for user with UID: $userId")
                        // Handle the case where no user data is found
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FetchProfessionalData", "Database error: ${error.message}")
                    // Handle database error
                }
            })
        } ?: Log.e("FetchProfessionalData", "Current user is null")
    }

    private fun setCurrentWeekDayAndDate() {
        // Implement this function as in your previous code
        // This function sets the current week day and date
    }

    private fun saveProfessionalToSharedPreferences(professional: Professional) {
        val editor = sharedPreferences.edit()
        editor.putString("id", professional.id)
        editor.putString("name", professional.name)
        editor.putString("email", professional.email)
        editor.putString("contactNumber", professional.contactNumber)
        editor.putString("cnic", professional.cnic)
        editor.putString("specialization", professional.specialization)
        editor.putString("affiliation", professional.affiliation)
        editor.putBoolean("affiliationStatus", professional.affiliationStatus)
        editor.putString("password", professional.password)
        editor.putFloat("rating", professional.rating.toFloat())
        editor.putString("picture", professional.picture)
        editor.apply()
    }

    private fun populateRecyclerView(adapter: appointmentListAdapter) {
        val currentUser = auth.currentUser
        val professionalId = currentUser?.uid

        professionalId?.let { id ->
            val appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments")

            // Query appointments where professionalId matches
            appointmentsRef.orderByChild("professionalId").equalTo(id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val appointmentsList = mutableListOf<bookAppointment.Appointment>()

                        for (appointmentSnapshot in snapshot.children) {
                            val appointment = appointmentSnapshot.getValue(bookAppointment.Appointment::class.java)
                            appointment?.let {
                                appointmentsList.add(it)
                            }
                        }

                        // Update the adapter with fetched appointments
                        adapter.setAppointmentList(appointmentsList)
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FetchAppointments", "Database error: ${error.message}")
                        // Handle database error
                    }
                })
        }
    }


}
