package com.ahmadmustafa.docconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class signupOptions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_options)

        val patientSignup=findViewById<View>(R.id.patient)
        val doctorSignup=findViewById<View>(R.id.doctor)
        val centerSignup=findViewById<View>(R.id.center)

        patientSignup.setOnClickListener {
            val intent = Intent(this, signupPatient::class.java)
            startActivity(intent)
        }

        doctorSignup.setOnClickListener {
            val intent = Intent(this, signupDoctor::class.java)
            startActivity(intent)
        }

        centerSignup.setOnClickListener {
            val intent = Intent(this, signupCenter::class.java)
            startActivity(intent)
        }

    }
}