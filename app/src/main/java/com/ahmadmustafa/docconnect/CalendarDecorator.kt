package com.ahmadmustafa.docconnect

import android.os.Bundle
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class YourActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_appointment_date)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val selectedDateMillis = calendar.timeInMillis
            val currentDateMillis = System.currentTimeMillis()

            if (selectedDateMillis == currentDateMillis) {
                // Customize the selected date circle color
                setCalendarViewSelectedDateCircleColor(calendarView, R.color.appred)
            } else {
                // Reset to the default selected date circle color
                setCalendarViewSelectedDateCircleColor(calendarView, android.R.color.transparent)
            }
        }
    }

    private fun setCalendarViewSelectedDateCircleColor(calendarView: CalendarView, colorResId: Int) {
        try {
            val delegateField = CalendarView::class.java.getDeclaredField("mDelegate")
            delegateField.isAccessible = true
            val delegate = delegateField.get(calendarView)
            val method = delegate.javaClass.getDeclaredMethod(
                "setSelectedDayCircleColor", Int::class.javaPrimitiveType
            )
            method.isAccessible = true
            method.invoke(delegate, getColor(colorResId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
