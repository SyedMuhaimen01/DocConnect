package com.ahmadmustafa.docconnect

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_view_appointments_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val calendarView = findViewById<CalendarView>(R.id.calendarView)

        val monthTextView = findViewById<TextView>(R.id.month)
        val yearTextView = findViewById<TextView>(R.id.year)
        val calendar = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.time)

        monthTextView.text = currentMonth
        yearTextView.text = currentYear
        setCurrentWeekDayAndDate()

        // Set current week day and date

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
}