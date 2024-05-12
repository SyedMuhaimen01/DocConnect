package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class setWorkingHours : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var professionalId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_working_hours)

        // Initialize Firebase Auth and Database
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference.child("working_hours")

        // Get current user's ID
        professionalId = auth.currentUser?.uid ?: ""

        val startTimeEditText = findViewById<EditText>(R.id.startTime)
        val endTimeEditText = findViewById<EditText>(R.id.endTime)
        val weekdaysSpinner = findViewById<Spinner>(R.id.weekdaysSpinner)
        val setWorkingHoursButton = findViewById<Button>(R.id.setWorkingHoursButton)
        val startTimeAmPmSpinner = findViewById<Spinner>(R.id.startTimeAmPmSpinner)
        val endTimeAmPmSpinner = findViewById<Spinner>(R.id.endTimeAmPmSpinner)

        // Set up spinners for AM/PM selection
        val amPmAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.am_pm_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            startTimeAmPmSpinner.adapter = adapter
            endTimeAmPmSpinner.adapter = adapter
        }

        // Set up spinner for weekdays selection
        val weekdaysAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.weekdays_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            weekdaysSpinner.adapter = adapter
        }

        // Set input filter for start and end time to allow only valid time format
        val timeFilter = TimeInputFilter()
        startTimeEditText.filters = arrayOf(timeFilter)
        endTimeEditText.filters = arrayOf(timeFilter)

        setWorkingHoursButton.setOnClickListener {
            val startTime = "${startTimeEditText.text} ${startTimeAmPmSpinner.selectedItem}"
            val endTime = "${endTimeEditText.text} ${endTimeAmPmSpinner.selectedItem}"
            val selectedWeekdays = weekdaysSpinner.selectedItem as String

            Log.d("SetWorkingHours", "Start Time: $startTime")
            Log.d("SetWorkingHours", "End Time: $endTime")
            Log.d("SetWorkingHours", "Selected Weekdays: $selectedWeekdays")

            if (validateInput(startTime, endTime, selectedWeekdays)) {
                Log.d("SetWorkingHours", "Validation Passed")

                // Save working hours to database for each selected weekday under professional's ID
                val weekdays = resources.getStringArray(R.array.weekdays_array)
                for (weekday in weekdays) {
                    if (selectedWeekdays.contains(weekday)) {
                        val workingHoursId = database.child(professionalId).child(weekday).push().key
                        workingHoursId?.let {
                            val workingHours = WorkingHours(
                                it,
                                professionalId,
                                startTime,
                                endTime
                            )
                            database.child(professionalId).child(weekday).child(it).setValue(workingHours)
                        }
                    }
                }
                Toast.makeText(this, "Working hours set successfully", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("SetWorkingHours", "Validation Failed")
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInput(startTime: String, endTime: String, selectedWeekdays: String): Boolean {
        // Implement validation logic here
        // You can check if the time is in the correct format and if the end time is after the start time
        // You can also validate the selected weekdays

        // Validate time format
        if (!isValidTime(startTime.substringBefore(" ")) || !isValidTime(endTime.substringBefore(" "))) {
            return false
        }

        // Check if any weekday is selected
        return selectedWeekdays.isNotEmpty()
    }

    private fun isValidTime(time: String): Boolean {
        val regex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]\$")
        return regex.matches(time)
    }

    data class WorkingHours(
        val id: String,
        val professionalId: String,
        val startTime: String,
        val endTime: String
    )

    private inner class TimeInputFilter : InputFilter {
        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            // Allow only digits, colons, and spaces
            val allowedChars = "1234567890: "
            val sb = StringBuilder()
            for (i in start until end) {
                val c = source?.get(i)
                if (c!! in allowedChars) {
                    sb.append(c)
                }
            }
            return sb.toString()
        }
    }
}
