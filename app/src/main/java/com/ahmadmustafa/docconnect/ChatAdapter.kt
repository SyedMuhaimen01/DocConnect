package com.ahmadmustafa.docconnect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val chats: List<Chat>, private val senderId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val MSG_TYPE_LEFT = 0
    private val MSG_TYPE_RIGHT = 1
    private val IMG_MSG_TYP_LEFT = 2
    private val IMG_MSG_TYP_RIGHT = 3
    private val VID_MSG_TYP_LEFT = 4
    private val VID_MSG_TYP_RIGHT = 5

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MSG_TYPE_LEFT -> ReceiverViewHolder(inflater.inflate(R.layout.msg_left, parent, false))
            MSG_TYPE_RIGHT -> SenderViewHolder(inflater.inflate(R.layout.msg_right, parent, false))
            IMG_MSG_TYP_LEFT -> ReceiverImageViewHolder(inflater.inflate(R.layout.image_left, parent, false))
            IMG_MSG_TYP_RIGHT -> SenderImageViewHolder(inflater.inflate(R.layout.image_right, parent, false))
            VID_MSG_TYP_LEFT -> ReceiverVideoViewHolder(inflater.inflate(R.layout.video_left, parent, false))
            VID_MSG_TYP_RIGHT -> SenderVideoViewHolder(inflater.inflate(R.layout.video_right, parent, false))
            else -> throw IllegalArgumentException("Invalid View Type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = chats[position]
        when (holder.itemViewType) {
            MSG_TYPE_LEFT -> (holder as ReceiverViewHolder).bind(chat)
            MSG_TYPE_RIGHT -> (holder as SenderViewHolder).bind(chat)
            IMG_MSG_TYP_LEFT -> (holder as ReceiverImageViewHolder).bind(chat)
            IMG_MSG_TYP_RIGHT -> (holder as SenderImageViewHolder).bind(chat)
            VID_MSG_TYP_LEFT -> (holder as ReceiverVideoViewHolder).bind(chat)
            VID_MSG_TYP_RIGHT -> (holder as SenderVideoViewHolder).bind(chat)
        }
    }

    override fun getItemCount(): Int = chats.size

    override fun getItemViewType(position: Int): Int {
        val chat = chats[position]
        return when {
            chat.senderId == senderId && chat.contentType == Chat.ContentType.IMAGE -> IMG_MSG_TYP_RIGHT
            chat.senderId == senderId && chat.contentType == Chat.ContentType.VIDEO -> VID_MSG_TYP_RIGHT
            chat.senderId == senderId -> MSG_TYPE_RIGHT
            chat.contentType == Chat.ContentType.IMAGE -> IMG_MSG_TYP_LEFT
            chat.contentType == Chat.ContentType.VIDEO -> VID_MSG_TYP_LEFT
            else -> MSG_TYPE_LEFT
        }
    }

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.sentMessageTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.time)

        fun bind(chat: Chat) {
            messageTextView.text = chat.message
            timeTextView.text = convertTimestamp(chat.time ?: 0)
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.receivedMessageTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.time)

        fun bind(chat: Chat) {
            messageTextView.text = chat.message
            timeTextView.text = convertTimestamp(chat.time ?: 0)
        }
    }

    inner class SenderImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val mediaImageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(chat: Chat) {
            timeTextView.text = convertTimestamp(chat.time ?: 0)
            Glide.with(itemView.context).load(chat.contentUri).into(mediaImageView)
        }
    }

    inner class ReceiverImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val mediaImageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(chat: Chat) {
            timeTextView.text = convertTimestamp(chat.time ?: 0)
            Glide.with(itemView.context).load(chat.contentUri).into(mediaImageView)
        }
    }

    inner class SenderVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val videoView: VideoView = itemView.findViewById(R.id.videoViewConstraint)

        fun bind(chat: Chat) {
            timeTextView.text = convertTimestamp(chat.time ?: 0)
            videoView.setVideoURI(Uri.parse(chat.contentUri))
            videoView.start()
        }
    }

    inner class ReceiverVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val videoView: VideoView = itemView.findViewById(R.id.videoViewConstraint)

        fun bind(chat: Chat) {
            timeTextView.text = convertTimestamp(chat.time ?: 0)
            videoView.setVideoURI(Uri.parse(chat.contentUri))
            videoView.start()
        }
    }

    private fun convertTimestamp(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}
