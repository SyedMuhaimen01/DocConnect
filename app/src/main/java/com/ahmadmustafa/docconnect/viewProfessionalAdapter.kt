package com.ahmadmustafa.docconnect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class viewProfessionalAdapter(private var professionals: List<Professional>) : RecyclerView.Adapter<viewProfessionalAdapter.ProfessionalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfessionalViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.professional_card, parent, false)
        return ProfessionalViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfessionalViewHolder, position: Int) {
        val currentProfessional = professionals[position]
        holder.nameTextView.text = currentProfessional.name
        holder.specializationTextView.text = currentProfessional.specialization
        val affiliation = currentProfessional.affiliation ?: ""
        val indexOfSlash = affiliation.indexOf("|")
        val location = if (indexOfSlash != -1 && indexOfSlash < affiliation.length - 1) {
            affiliation.substring(indexOfSlash + 1)
        } else {
            affiliation
        }
        holder.locationTextView.text = location
        Glide.with(holder.itemView.context)
            .load(currentProfessional.picture)
            .into(holder.profileImageView)
        // Bind other data fields as needed
    }

    override fun getItemCount() = professionals.size

    fun updateData(newProfessionals: List<Professional>) {
        professionals = newProfessionals
        notifyDataSetChanged()
    }

    class ProfessionalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val specializationTextView: TextView = itemView.findViewById(R.id.specializationTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val profileImageView: CircleImageView = itemView.findViewById(R.id.ImageView)
        // Add other TextViews or views as needed
    }
}