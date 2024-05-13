package com.ahmadmustafa.docconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class UpcomingAppointmentsAdapter(private val appointments: List<bookAppointment.Appointment>) :
    RecyclerView.Adapter<UpcomingAppointmentsAdapter.ViewHolder>() {

    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val professionalsRef: DatabaseReference by lazy { database.reference.child("professionals") }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: CircleImageView = itemView.findViewById(R.id.profileImageView)

        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val ratingTextView: TextView = itemView.findViewById(R.id.ratingTextView)
        val reviewsTextView: TextView = itemView.findViewById(R.id.reviewsTextView)
        val cancelButton: Button = itemView.findViewById(R.id.cancelButton)
        val rescheduleButton: Button = itemView.findViewById(R.id.rescheduleButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.upcoming_page_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]


        // Fetch professional details from Firebase based on appointment's professional ID
        professionalsRef.child(appointment.professionalId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val professional = dataSnapshot.getValue(Professional::class.java)
                    professional?.let {
                        holder.nameTextView.text = it.name
                        holder.messageTextView.text = it.specialization
                        holder.ratingTextView.text = it.rating.toString()
                        holder.reviewsTextView.text = "(500+ Reviews)"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    println("Error fetching professional details: $databaseError")
                }
            })

        // Set click listeners for buttons
        holder.cancelButton.setOnClickListener {
            // Handle cancel button click
        }
        holder.rescheduleButton.setOnClickListener {
            // Handle reschedule button click
        }
    }

    override fun getItemCount(): Int {
        return appointments.size
    }
}
