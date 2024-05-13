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

class cancelledFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var appointmentsRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cancelled, container, false)
        recyclerView = view.findViewById(R.id.cancelledRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        appointmentsRef = FirebaseDatabase.getInstance().reference.child("appointments")

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val appointmentsListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val appointments = mutableListOf<bookAppointment.Appointment>()
                    for (snapshot in dataSnapshot.children) {
                        val appointment = snapshot.getValue(bookAppointment.Appointment::class.java)
                        appointment?.let {
                            // Filter appointments for the current user with status "cancelled"
                            if (it.patientId == currentUser.uid && it.status == "cancelled") {
                                appointments.add(it)
                            }
                        }
                    }
                    displayCancelledAppointments(appointments)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    println("Error loading cancelled appointments: $databaseError")
                }
            }
            appointmentsRef.addValueEventListener(appointmentsListener)
        }
    }

    private fun displayCancelledAppointments(appointments: List<bookAppointment.Appointment>) {
        val adapter = CancelledAppointmentsAdapter(appointments)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }
}
