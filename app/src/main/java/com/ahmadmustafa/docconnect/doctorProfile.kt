package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView

class doctorProfile : AppCompatActivity() {
    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile)

        val homeButton = findViewById<ImageButton>(R.id.home2)
        homeButton.setOnClickListener {
            val intent = Intent(this, manageAppointments::class.java)
            startActivity(intent)
        }

        val profileButton = findViewById<ImageButton>(R.id.profile2)
        profileButton.setOnClickListener {
            val intent = Intent(this, doctorProfile::class.java)
            startActivity(intent)
        }

        val editProfile=findViewById<Button>(R.id.editprof)
        editProfile.setOnClickListener {
            val intent = Intent(this, editDoctorProfile::class.java)
            startActivity(intent)
        }

        val logoutButton = findViewById<ImageView>(R.id.logout)
        logoutButton.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }
    }
}