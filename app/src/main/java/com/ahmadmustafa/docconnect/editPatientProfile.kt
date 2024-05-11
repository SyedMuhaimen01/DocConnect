package com.ahmadmustafa.docconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class editPatientProfile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_patient_profile)

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        val mapButton=findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            val intent = Intent(this, map::class.java)
            startActivity(intent)
        }

        val appointButton=findViewById<ImageButton>(R.id.appoint)
        appointButton.setOnClickListener {
            val intent = Intent(this, manageAppointments::class.java)
            startActivity(intent)
        }

        val chatButton=findViewById<ImageButton>(R.id.chats)
        chatButton.setOnClickListener {
            val intent = Intent(this, chatBox::class.java)
            startActivity(intent)
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            val intent = Intent(this, doctorProfile::class.java)
            startActivity(intent)
        }

    }


}