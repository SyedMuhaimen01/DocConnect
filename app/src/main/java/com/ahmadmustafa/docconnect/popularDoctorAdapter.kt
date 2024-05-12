package com.ahmadmustafa.docconnect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class popularDoctorAdapter(private val professionals: List<Professional>, private val onItemClick: (Professional) -> Unit) : RecyclerView.Adapter<popularDoctorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.popular_doctor_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val professional = professionals[position]
        holder.bind(professional)
        holder.itemView.setOnClickListener { onItemClick(professional) }
    }

    override fun getItemCount(): Int {
        return professionals.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        private val circleImageView: CircleImageView = itemView.findViewById(R.id.circleImageView)

        fun bind(professional: Professional) {
            nameTextView.text = professional.name
            messageTextView.text = professional.specialization
            val affiliationParts = professional.affiliation.split("|")
            if (affiliationParts.size == 2) {
                val location = affiliationParts[1].trim()
                locationTextView.text = location
            } else {
                locationTextView.text = "Location not available"
            }

            // Load the doctor's picture using Glide
            Glide.with(itemView.context)
                .load(professional.picture)
                .placeholder(R.drawable.imagecircles)
                .error(R.drawable.imagecircles)
                .into(circleImageView)
        }
    }
}
