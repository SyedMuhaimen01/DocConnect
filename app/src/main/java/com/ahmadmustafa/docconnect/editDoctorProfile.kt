package com.ahmadmustafa.docconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class editDoctorProfile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_doctor_profile)

        val profileButton=findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener{
            val intent= Intent(this,doctorProfile::class.java)
            startActivity(intent)
        }

        val homeButton=findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener{
            val intent= Intent(this,manageAppointments::class.java)
            startActivity(intent)
        }

    }
}