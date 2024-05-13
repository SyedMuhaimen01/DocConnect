package com.ahmadmustafa.docconnect
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class bookAppointment : AppCompatActivity() {
    private val NOTIFICATION_CHANNEL_ID = "AppointmentNotification"
    private lateinit var appointmentsRef: DatabaseReference
    private lateinit var professionalId: String
    private var selectedDate: String? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_appointment)

        // Receive professional's ID from previous activity
        professionalId = intent.getStringExtra("professionalId") ?: ""
        Log.d("bookAppointment", "Professional ID: $professionalId")

        // Initialize Firebase Database
        val database = FirebaseDatabase.getInstance()
        val professionalsRef = database.getReference("professionals")

        // Retrieve professional's details
        professionalId.let { id ->
            professionalsRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val professionalName = snapshot.child("name").getValue(String::class.java)
                        val specialization = snapshot.child("specialization").getValue(String::class.java)
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
        }

        // Retrieve professional's working hours
        val workingHoursRef = database.getReference("working_hours")

        // Set up a SimpleDateFormat for time comparison
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Get today's day of the week
        val calendar = Calendar.getInstance()
        val todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Set click listeners for date TextViews
        val dateTextViews = listOf<TextView>(
            findViewById(R.id.dateTextView1),
            findViewById(R.id.dateTextView2),
            findViewById(R.id.dateTextView3)
        )
        dateTextViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                selectedDate = getDate(index)
            }
        }

        // Set click listeners for time TextViews
        val timeTextViews = listOf<TextView>(
            findViewById(R.id.time1),
            findViewById(R.id.time2),
            findViewById(R.id.time3)
        )
        timeTextViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                selectedTime = textView.text.toString()
                Log.d("bookAppointment", "Selected Time: $selectedTime")
            }
        }

        // Set click listener for book button
        findViewById<TextView>(R.id.bookButton).setOnClickListener {
            // Check if both date and time are selected
            if (selectedDate != null && selectedTime != null) {
                // Get the current user ID from Firebase Authentication
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                // Store appointment details in the database
                val appointment = Appointment(
                    patientId= userId,
                    professionalId = professionalId,
                    appointmentId = UUID.randomUUID().toString(),
                    date = selectedDate!!,
                    time = selectedTime!!


                )
                saveAppointment(appointment)
            } else {
                // Show error message if date or time is not selected
                Log.e("bookAppointment", "Please select both date and time.")
            }
        }

        // Iterate over the next three days starting from today
        for (i in 0 until 3) {
            val dayIndex = (todayDayOfWeek - 1 + i) % 7  // Adjusting to start from Sunday

            // Retrieve schedule for the current day
            val dayScheduleRef = workingHoursRef.child(professionalId).child(getDayOfWeek(dayIndex))

            // Add listener to fetch data
            dayScheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Extract start and end times from database
                        val startTime = snapshot.child("startTime").getValue(String::class.java)
                        val endTime = snapshot.child("endTime").getValue(String::class.java)

                        // Populate UI with schedule details
                        findViewById<TextView>(R.id.dayTextView1).text = getDayOfWeek(0)
                        findViewById<TextView>(R.id.dateTextView1).text = getDate(0)
                        findViewById<TextView>(R.id.monthTextView1).text = getMonth(0)

                        findViewById<TextView>(R.id.dayTextView2).text = getDayOfWeek(1)
                        findViewById<TextView>(R.id.dateTextView2).text = getDate(1)
                        findViewById<TextView>(R.id.monthTextView2).text = getMonth(1)

                        findViewById<TextView>(R.id.dayTextView3).text = getDayOfWeek(2)
                        findViewById<TextView>(R.id.dateTextView3).text = getDate(2)
                        findViewById<TextView>(R.id.monthTextView3).text = getMonth(2)

                        // Adjusting for the current time
                        val currentTime = Calendar.getInstance()
                        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
                        val currentMinute = currentTime.get(Calendar.MINUTE)

                        // Check if startTime and endTime are null
                        if (startTime != null && endTime != null) {
                            val startHour = startTime.substringBefore(":").toIntOrNull() ?: 0
                            val startMinute =
                                startTime.substringAfter(":").substringBefore(" ").toIntOrNull()
                                    ?: 0

                            val endHour = endTime.substringBefore(":").toIntOrNull() ?: 0
                            val endMinute =
                                endTime.substringAfter(":").substringBefore(" ").toIntOrNull() ?: 0

                            val startTotalMinutes = startHour * 60 + startMinute
                            val endTotalMinutes = endHour * 60 + endMinute
                            val currentTotalMinutes = currentHour * 60 + currentMinute

                            // Determine the appropriate text for the time field
                            val timeText = when {
                                startTotalMinutes <= currentTotalMinutes && currentTotalMinutes <= endTotalMinutes ->
                                    "Now - $endTime"  // Current time falls within the appointment time
                                currentTotalMinutes < startTotalMinutes -> "$startTime - $endTime"  // Before appointment time
                                currentTotalMinutes > endTotalMinutes -> "Next Day $startTime - $endTime"  // After appointment time
                                else -> "N/A"  // Any other case
                            }
                            timeTextViews[i].text = timeText
                        } else {
                            Log.d("bookAppointment", "Start time or end time is null")
                        }
                        Log.d(
                            "bookAppointment",
                            "Day: ${getDayOfWeek(dayIndex)}, Start Time: $startTime, End Time: $endTime"
                        )
                    } else {
                        Log.d("bookAppointment", "No schedule found for ${getDayOfWeek(dayIndex)}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    Log.e("bookAppointment", "Database error: ${error.message}")
                }
            })
        }
    }

    // Function to get the day of the week
    private fun getDayOfWeek(dayIndex: Int): String {
        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat")
        return days[dayIndex]
    }

    // Function to get the date string for the next three days
    private fun getDate(dayIndex: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, dayIndex)
        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    // Function to get the month string for the next three days
    private fun getMonth(dayIndex: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, dayIndex)
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        return monthFormat.format(calendar.time)
    }

    // Function to save appointment details in the database
    private fun saveAppointment(appointment: Appointment) {
        val database = FirebaseDatabase.getInstance()
        appointmentsRef = database.getReference("appointments")

        // Concatenate current month and year with the selected date
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Month starts from 0
        val currentYear = calendar.get(Calendar.YEAR)
        val formattedDate = "${appointment.date}-$currentMonth-$currentYear"

        // Update appointment date with concatenated value
        val updatedAppointment = appointment.copy(date = formattedDate)

        appointmentsRef.child(updatedAppointment.appointmentId).setValue(updatedAppointment)
            .addOnSuccessListener {
                showRegistrationSuccessNotification()
                Log.d("bookAppointment", "Appointment saved successfully.")
            }
            .addOnFailureListener {
                Log.e("bookAppointment", "Failed to save appointment: ${it.message}")
            }
    }

    // Data class to hold appointment details
    data class Appointment(
        val patientId: String = "",
        val professionalId: String = "",
        val appointmentId: String = "",
        val date: String = "",
        val time: String = ""
    ) {
        // You can leave this class empty if you have no additional methods or functionality
    }

    private fun showRegistrationSuccessNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Appointment Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notification for appointment success"
                enableLights(true)
                lightColor = Color.GREEN
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notifications_icon_foreground)
            .setContentTitle("Appointment  Status")
            .setContentText("Your appointment have been booked successfully.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1, builder.build())
    }
}
