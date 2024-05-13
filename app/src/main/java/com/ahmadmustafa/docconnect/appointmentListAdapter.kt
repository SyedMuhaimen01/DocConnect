package com.ahmadmustafa.docconnect

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahmadmustafa.docconnect.Patient
import com.google.firebase.database.*

class appointmentListAdapter(private var appointmentList: List<bookAppointment.Appointment>) :
    RecyclerView.Adapter<appointmentListAdapter.AppointmentViewHolder>() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val patientImageView: ImageView = itemView.findViewById(R.id.circleImageView)
        // Add more views as needed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.appointment_card, parent, false)
        return AppointmentViewHolder(itemView)
    }

    fun setAppointmentList(appointments: List<bookAppointment.Appointment>) {
        appointmentList = appointments
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val currentItem = appointmentList[position]

        holder.timeTextView.text = currentItem.time
        holder.dateTextView.text = currentItem.date

        // Fetch patient details from Firebase Realtime Database
        val databaseReference: DatabaseReference = database.reference.child("patients").child(currentItem.patientId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val patient = dataSnapshot.getValue(Patient::class.java)
                patient?.let {
                    holder.nameTextView.text = it.name
                    // For image, you would fetch it from Firebase Storage similarly
                } ?: run {
                    Log.d("TAG", "Patient is null")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("TAG", "Failed to fetch patient details: ${databaseError.message}")
            }
        })

        // Bind more data to views as needed
    }

    override fun getItemCount() = appointmentList.size
}
