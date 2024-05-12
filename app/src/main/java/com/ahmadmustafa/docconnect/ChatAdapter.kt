package com.ahmadmustafa.docconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatAdapter(private val chats: List<Chat>, private val senderId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SENDER_VIEW_TYPE = 0
    private val RECEIVER_VIEW_TYPE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SENDER_VIEW_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.msg_right, parent, false)
            SenderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.msg_left, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = chats[position]
        if (getItemViewType(position) == SENDER_VIEW_TYPE) {
            (holder as SenderViewHolder).bind(chat)
        } else {
            (holder as ReceiverViewHolder).bind(chat)
        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (chats[position].senderId == senderId) SENDER_VIEW_TYPE else RECEIVER_VIEW_TYPE
    }

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.sentMessageTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val mediaImageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(chat: Chat) {
            messageTextView.text = chat.message
            timeTextView.text = convertTimestamp(chat.time)
            if (chat.contentType == Chat.ContentType.IMAGE) {
                mediaImageView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(chat.contentUri) // Assuming chat.contentUri contains image URL for sent media
                    .into(mediaImageView)
            } else {
                mediaImageView.visibility = View.GONE
            }
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.receivedMessageTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val mediaImageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(chat: Chat) {
            messageTextView.text = chat.message
            timeTextView.text = convertTimestamp(chat.time)
            if (chat.contentType == Chat.ContentType.IMAGE) {
                mediaImageView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(chat.contentUri) // Assuming chat.contentUri contains image URL for received media
                    .into(mediaImageView)
            } else {
                mediaImageView.visibility = View.GONE
            }
        }
    }

    private fun convertTimestamp(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}
