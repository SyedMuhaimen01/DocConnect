package com.ahmadmustafa.docconnect

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminNotificationsAdapter(
    private var notifications: MutableList<Notification>,
    private val listener: OnNotificationClickListener
) : RecyclerView.Adapter<AdminNotificationsAdapter.NotificationViewHolder>() {

    interface OnNotificationClickListener {
        fun onNotificationClick(notification: Notification)
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val notificationTextView: TextView = itemView.findViewById(R.id.notificationTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val notification = notifications[position]
                listener.onNotificationClick(notification)
            }
        }

        fun bind(notification: Notification) {
            titleTextView.text = notification.title
            notificationTextView.text = "${notification.centerName} wants to signup on DOConnect"
            dateTextView.text = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(notification.timestamp)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_card, parent, false)
        return NotificationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount() = notifications.size

    fun addNotification(notification: Notification) {
        notifications.add(notification)
        notifyDataSetChanged()
    }
}
