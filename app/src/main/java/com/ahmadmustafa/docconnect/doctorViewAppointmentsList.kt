package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class doctorViewAppointmentsList : AppCompatActivity() {
    private val dayTextViewIds = listOf(
        R.id.monDayTextView,
        R.id.tueDayTextView,
        R.id.wedDayTextView,
        R.id.thuDayTextView,
        R.id.friDayTextView,
        R.id.satDayTextView,
        R.id.sunDayTextView
    )
    private val dayLayouts = listOf(
        R.id.monDateLayout,
        R.id.tueDateLayout,
        R.id.wedDateLayout,
        R.id.thuDateLayout,
        R.id.friDateLayout,
        R.id.satDateLayout,
        R.id.sunDateLayout
    )

    private val dateTextViewIds = listOf(
        R.id.monDateTextView,
        R.id.tueDateTextView,
        R.id.wedDateTextView,
        R.id.thuDateTextView,
        R.id.friDateTextView,
        R.id.satDateTextView,
        R.id.sunDateTextView
    )
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
        auth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().getReference("professionals")
        sharedPreferences = getSharedPreferences("professionals", Context.MODE_PRIVATE)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        fetchLoggedInprofessionalData()

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
            startActivity(Intent(this, chatBox::class.java).apply {
                putExtra("userType", "professional")
            })
        }

        val mapButton = findViewById<ImageButton>(R.id.workingHours)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java).apply {
                putExtra("userType", "professional")
            })
        }
        val appointButton=findViewById<ImageButton>(R.id.appoint)
        appointButton.setOnClickListener {
            startActivity(Intent(this, doctorViewAppointmentsList::class.java))
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, doctorProfile::class.java))
        }


    }

    private fun fetchLoggedInprofessionalData() {
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

        val calendar = Calendar.getInstance()

        // Set the calendar to the beginning of the current week (Sunday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        // Iterate over each day of the week and set the text of the corresponding TextViews
        for (i in 0 until 7) {
            // Get the current day of the week and date as strings
            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
            val dateOfMonth = calendar.get(Calendar.DAY_OF_MONTH).toString()

            // Find the TextViews corresponding to the current day of the week
            val dayTextView = findViewById<TextView>(dayTextViewIds[i])
            val dateTextView = findViewById<TextView>(dateTextViewIds[i])

            // Set the text of the TextViews to the day of the week and date
            dayTextView.text = dayOfWeek
            dateTextView.text = dateOfMonth
            Log.d("TextViewDebug", "Day TextView: $dayTextView")
            Log.d("TextViewDebug", "Date TextView: $dateTextView")
            // Log the values being set
            Log.d("CalendarTextView", "Day: $dayOfWeek, Date: $dateOfMonth")

            // Check if this TextView represents today, and set its background color accordingly
            val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()
            if (today == dateOfMonth) {
                dateTextView.setBackgroundColor(resources.getColor(R.color.appred))
                dayTextView.setBackgroundColor(resources.getColor(R.color.appred))
                val dateLayout = findViewById<View>(dayLayouts[i])
                // Set the background color of the layout to red
                dateLayout.setBackgroundColor(resources.getColor(R.color.appred))

            } else {
                dateTextView.setBackgroundColor(resources.getColor(android.R.color.black))
            }

            // Move to the next day of the week
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

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
            fetchLoggedInprofessionalData()
        } else {
            // No user is signed in, redirect to login screen
            startActivity(Intent(this, login::class.java))
            finish()
        }
    }
}