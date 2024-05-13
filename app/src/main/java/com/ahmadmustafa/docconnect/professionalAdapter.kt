package com.ahmadmustafa.docconnect

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfessionalAdapter(private val chats: List<Chat>, param: (Any) -> Unit) : RecyclerView.Adapter<ProfessionalAdapter.ProfessionalViewHolder>() {

    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private val databaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfessionalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_users_userscard, parent, false)
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
            val otherUserId = if (chat.senderId == currentUserUid) chat.receiverId else chat.senderId

            // Set the name
            setName(otherUserId)

            messageTextView.text = chat.message
            timeTextView.text = "5min ago"

            // Start chat with the user when card is clicked
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, chatBox::class.java).apply {
                    putExtra("receiverId", otherUserId)
                }
                itemView.context.startActivity(intent)
            }
        }

        private fun setName(userId: String) {
            // Check in "patient" table
            databaseReference.child("patients").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(patientSnapshot: DataSnapshot) {
                    if (patientSnapshot.exists()) {
                        val patientName = patientSnapshot.child("name").getValue(String::class.java)
                        nameTextView.text = patientName
                    } else {
                        // If not found in "patient" table, check in "professionals" table
                        databaseReference.child("professionals").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(professionalSnapshot: DataSnapshot) {
                                if (professionalSnapshot.exists()) {
                                    val professionalName = professionalSnapshot.child("name").getValue(String::class.java)
                                    nameTextView.text = professionalName
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle onCancelled
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }
    }
}
