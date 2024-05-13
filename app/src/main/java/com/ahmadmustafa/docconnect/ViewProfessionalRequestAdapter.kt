package com.ahmadmustafa.docconnect

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class ViewProfessionalRequestAdapter(private var professionals: MutableList<Professional>) : RecyclerView.Adapter<ViewProfessionalRequestAdapter.ProfessionalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfessionalViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.professional_request_card, parent, false)
        return ProfessionalViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfessionalViewHolder, position: Int) {
        val currentProfessional = professionals[position]
        holder.bind(currentProfessional)
    }

    override fun getItemCount() = professionals.size

    fun updateData(newProfessionals: List<Professional>) {
        professionals.clear()
        professionals.addAll(newProfessionals.filter { !it.affiliationStatus }) // Filter professionals with affiliationStatus = false
        notifyDataSetChanged()
    }

    inner class ProfessionalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: CircleImageView = itemView.findViewById(R.id.profileImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val specializationTextView: TextView = itemView.findViewById(R.id.specializationTextView)
        private val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        private val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        private val declineButton: Button = itemView.findViewById(R.id.declineButton)

        fun bind(professional: Professional) {
            Glide.with(itemView.context)
                .load(professional.picture)
                .circleCrop()
                .into(profileImageView)
            nameTextView.text = professional.name
            specializationTextView.text = professional.specialization
            val affiliation = professional.affiliation ?: ""
            val indexOfPipe = affiliation.indexOf("|")
            val location = if (indexOfPipe != -1 && indexOfPipe < affiliation.length - 1) {
                affiliation.substring(indexOfPipe + 1)
            } else {
                affiliation
            }
            locationTextView.text = location

            acceptButton.setOnClickListener {
                Log.d("ViewProfessionalRequestAdapter", "Accept button clicked for ${professional.name}")
                // Update professional's affiliationStatus and store in database
                professional.affiliationStatus = true
                updateProfessionalInDatabase(professional)
                // Remove the professional from the list
                removeProfessional(professional)
            }

            declineButton.setOnClickListener {
                Log.d("ViewProfessionalRequestAdapter", "Decline button clicked for ${professional.name}")
                // Update professional's affiliationStatus and affiliation, then store in database
                professional.affiliationStatus = false
                professional.affiliation = ""
                updateProfessionalInDatabase(professional)
                // Remove the professional from the list
                removeProfessional(professional)
            }
        }

        private fun updateProfessionalInDatabase(professional: Professional) {
            val professionalRef = FirebaseDatabase.getInstance().getReference("professionals").child(professional.id)
            professionalRef.setValue(professional)
                .addOnSuccessListener {
                    Log.d("ViewProfessionalRequestAdapter", "Professional updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("ViewProfessionalRequestAdapter", "Error updating professional: ${e.message}")
                }
        }

        private fun removeProfessional(professional: Professional) {
            val position = professionals.indexOf(professional)
            if (position != -1) {
                professionals.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }
}
