package com.ahmadmustafa.docconnect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class upcommingFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var appointmentsRef: DatabaseReference
    private lateinit var professionalsRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_upcomming, container, false)
        recyclerView = view.findViewById(R.id.userRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        appointmentsRef = FirebaseDatabase.getInstance().reference.child("appointments")
        professionalsRef = FirebaseDatabase.getInstance().reference.child("professionals")

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val appointmentsListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val appointments = mutableListOf<bookAppointment.Appointment>()
                    for (snapshot in dataSnapshot.children) {
                        val appointment = snapshot.getValue(bookAppointment.Appointment::class.java)
                        appointment?.let {
                            // Filter appointments for the current user
                            if (it.patientId == currentUser.uid) {
                                appointments.add(it)
                            }
                        }
                    }
                    // Fetch professionals for appointments
                    fetchProfessionalsForAppointments(appointments)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    println("Error loading appointments: $databaseError")
                }
            }
            appointmentsRef.addValueEventListener(appointmentsListener)
        }
    }

    private fun fetchProfessionalsForAppointments(
        appointments: List<bookAppointment.Appointment>
    ) {
        val professionalsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val professionalsMap = mutableMapOf<String, Professional>()
                for (snapshot in dataSnapshot.children) {
                    val professional = snapshot.getValue(Professional::class.java)
                    professional?.let { professionalsMap[it.id] = it }
                }
                // Associate professionals with appointments
                val appointmentsWithProfessionals = mutableListOf<bookAppointment.Appointment>()
                for (appointment in appointments) {
                    val professional = professionalsMap[appointment.professionalId]
                    professional?.let {
                        val appointmentWithProfessional = bookAppointment.Appointment(
                            appointment.patientId,
                            appointment.professionalId,
                            appointment.appointmentId,
                            appointment.date,
                            appointment.time,
                            appointment.status,
                        )
                        appointmentsWithProfessionals.add(appointmentWithProfessional)
                    }
                }
                // Display appointments with professionals
                displayAppointmentsWithProfessionals(appointmentsWithProfessionals)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                println("Error loading professionals: $databaseError")
            }
        }
        professionalsRef.addValueEventListener(professionalsListener)
    }

    private fun displayAppointmentsWithProfessionals(appointments: List<bookAppointment.Appointment>) {
        val adapter = UpcomingAppointmentsAdapter(appointments)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }
}
