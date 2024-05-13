package com.ahmadmustafa.docconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppLogsAdapter(private val appLogs: List<AppLog>) :
    RecyclerView.Adapter<AppLogsAdapter.LogViewHolder>() {

    inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val notificationTextView: TextView = itemView.findViewById(R.id.notificationTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        fun bind(log: AppLog) {
            titleTextView.text = log.title
            notificationTextView.text = log.notification
            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            val date = Date(log.timestamp)
            dateTextView.text = dateFormat.format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_card, parent, false)
        return LogViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(appLogs[position])
    }

    override fun getItemCount() = appLogs.size
}
