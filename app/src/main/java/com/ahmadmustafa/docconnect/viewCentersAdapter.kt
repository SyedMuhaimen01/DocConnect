package com.ahmadmustafa.docconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
class viewCenterAdapter(private var centers: List<Center>) : RecyclerView.Adapter<viewCenterAdapter.CenterViewHolder>() {

    private var filteredCenters: List<Center> = listOf()

    init {
        filterCenters()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CenterViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.center_card, parent, false)
        return CenterViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CenterViewHolder, position: Int) {
        val currentCenter = filteredCenters[position]
        holder.nameTextView.text = currentCenter.name
        holder.servicesTextView.text = currentCenter.category
        holder.locationTextView.text = currentCenter.address
        Glide.with(holder.itemView.context)
            .load(currentCenter.picture)
            .into(holder.profileImageView)
        // Bind other data fields as needed
    }

    override fun getItemCount() = filteredCenters.size

    fun updateData(newCenters: List<Center>) {
        centers = newCenters
        filterCenters()
        notifyDataSetChanged()
    }

    private fun filterCenters() {
        filteredCenters = centers.filter { it.centerStatus }
    }

    class CenterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val servicesTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val profileImageView: CircleImageView = itemView.findViewById(R.id.ImageView)

        // Add other TextViews or views as needed
    }
}
