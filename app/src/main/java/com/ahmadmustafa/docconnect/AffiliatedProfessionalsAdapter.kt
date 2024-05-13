package com.ahmadmustafa.docconnect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
class AffiliatedProfessionalsAdapter(private var professionals: List<Professional>) : RecyclerView.Adapter<AffiliatedProfessionalsAdapter.ProfessionalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfessionalViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.professional_card, parent, false)
        return ProfessionalViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfessionalViewHolder, position: Int) {
        val currentProfessional = professionals[position]
        // Only bind if affiliationStatus is false
        if (currentProfessional.affiliationStatus) {
            holder.bind(currentProfessional)
        } else {
            // Optionally hide or handle the view if affiliationStatus is true
            holder.itemView.visibility = View.GONE
        }
    }

    override fun getItemCount() = professionals.size

    fun updateData(newProfessionals: List<Professional>) {
        professionals = newProfessionals
        notifyDataSetChanged()
    }

    class ProfessionalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: CircleImageView = itemView.findViewById(R.id.ImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val specializationTextView: TextView = itemView.findViewById(R.id.specializationTextView)
        private val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)

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
        }
    }
}

