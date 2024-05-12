package com.ahmadmustafa.docconnect
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Calendar

class bookAppointment : AppCompatActivity() {

    private lateinit var appointmentsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_appointment)

        // Receive professional's ID from previous activity
        val professionalId = intent.getStringExtra("professionalId")
        Log.d("bookAppointment", "Professional ID: $professionalId")

        // Initialize Firebase Database
        val database = FirebaseDatabase.getInstance()
        val professionalsRef = database.getReference("professionals")

        // Retrieve professional's details
        professionalId?.let { id ->
            professionalsRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val professionalName = snapshot.child("name").getValue(String::class.java)
                        val specialization = snapshot.child("specialization").getValue(String::class.java)
                        val affiliationStatus = snapshot.child("affiliation_status").getValue(Boolean::class.java)
                        val affiliationDetails = snapshot.child("affiliation").getValue(String::class.java)
                        val profileImageUrl = snapshot.child("picture").getValue(String::class.java)

                        // Populate UI with professional's details
                        professionalName?.let { findViewById<TextView>(R.id.professionalNameTextView).text = it }
                        specialization?.let { findViewById<TextView>(R.id.specializationTextView).text = it }
                        affiliationDetails?.let { findViewById<TextView>(R.id.locationTextView).text = it }

                        // Load professional's rounded picture into ImageView
                        profileImageUrl?.let { url ->
                            val profileImageView = findViewById<CircleImageView>(R.id.circleImageView)
                            Glide.with(this@bookAppointment)
                                .load(url)
                                .transform(CircleCrop())
                                .into(profileImageView)
                        }
                    } else {
                        Log.d("bookAppointment", "Professional data not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    Log.e("bookAppointment", "Database error: ${error.message}")
                }
            })
        } ?: run {
            Log.e("bookAppointment", "Professional ID is null")
        }

        // Retrieve professional's working hours
        val workingHoursRef = database.getReference("working_hours")

        // Assuming we need to display schedules for the next 3 days including today
        val currentDate = Calendar.getInstance()
        val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val layoutIds = listOf(R.id.layout1, R.id.layout2, R.id.layout3)

        for (i in 0 until 3) {
            val dayIndex = (currentDate.get(Calendar.DAY_OF_WEEK) + i) % 7
            val dayLayoutId = layoutIds[i]

            val dayScheduleRef = workingHoursRef.child(professionalId ?: "").child(days[dayIndex])

            dayScheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val startTime = snapshot.child("startTime").getValue(String::class.java)
                        val endTime = snapshot.child("endTime").getValue(String::class.java)

                        // Populate UI with schedule details
                        findViewById<TextView>(R.id.dayTextView1).text = days[dayIndex]
                        findViewById<TextView>(R.id.dateTextView1).text = currentDate.get(Calendar.DAY_OF_MONTH).toString()
                        findViewById<TextView>(R.id.time1).text = "$startTime - $endTime"
                        Log.d("bookAppointment", "Day: ${days[dayIndex]}, Start Time: $startTime, End Time: $endTime")
                    } else {
                        Log.d("bookAppointment", "No schedule found for ${days[dayIndex]}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    Log.e("bookAppointment", "Database error: ${error.message}")
                }
            })

            currentDate.add(Calendar.DAY_OF_MONTH, 1) // Move to the next day
        }
    }
}
