package com.ahmadmustafa.docconnect

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class professionalAdapter(private val chats: List<Chat>, param: (Any) -> Unit) : RecyclerView.Adapter<professionalAdapter.ProfessionalViewHolder>() {

    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private val databaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfessionalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_users_userscard, parent, false)
        return ProfessionalViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfessionalViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    inner class ProfessionalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)

        fun bind(chat: Chat) {
            // Determine the other user's ID
            val otherUserId =
                if (chat.senderId == currentUserUid) chat.receiverId else chat.senderId

            // Set the name
            setName(otherUserId) { name ->
                nameTextView.text = name

                itemView.setOnClickListener {
                    val intent = Intent(itemView.context, chatBox::class.java).apply {
                        putExtra("receiverId", otherUserId)
                        putExtra("receiverName", name)
                    }
                    try {
                        itemView.context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(
                            "ProfessionalAdapter",
                            "Error starting ChatBox activity: ${e.message}"
                        )
                    }
                }
            }

            messageTextView.text = chat.message
            timeTextView.text = "5min ago"
        }


        private fun setName(userId: String, callback: (String) -> Unit) {
            // Check if the user is a professional
            val professionalRef =
                FirebaseDatabase.getInstance().reference.child("professionals").child(userId)
            professionalRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(professionalSnapshot: DataSnapshot) {
                    if (professionalSnapshot.exists()) {
                        // The user is a professional, get their name
                        val professionalName =
                            professionalSnapshot.child("name").getValue(String::class.java)
                        callback.invoke(professionalName ?: "")
                    } else {
                        // Check if the user is a patient
                        val patientRef =
                            FirebaseDatabase.getInstance().reference.child("patients").child(userId)
                        patientRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(patientSnapshot: DataSnapshot) {
                                if (patientSnapshot.exists()) {
                                    // The user is a patient, get their name
                                    val patientName =
                                        patientSnapshot.child("name").getValue(String::class.java)
                                    callback.invoke(patientName ?: "")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(
                                    "ProfessionalAdapter",
                                    "Error fetching patient data: ${error.message}"
                                )
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "ProfessionalAdapter",
                        "Error fetching professional data: ${error.message}"
                    )
                }
            })
        }
    }
}