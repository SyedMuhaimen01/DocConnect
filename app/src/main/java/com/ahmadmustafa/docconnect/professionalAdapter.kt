package com.ahmadmustafa.docconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfessionalAdapter(private val chats: List<Chat>) : RecyclerView.Adapter<ProfessionalAdapter.ProfessionalViewHolder>() {

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
            nameTextView.text = chat.senderId
            messageTextView.text = chat.message
            timeTextView.text = "5min ago"
        }
    }
}