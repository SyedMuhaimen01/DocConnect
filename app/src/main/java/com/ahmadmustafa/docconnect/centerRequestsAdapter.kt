package com.ahmadmustafa.docconnect

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
class CenterRequestsAdapter(private var centers: List<Center>) : RecyclerView.Adapter<CenterRequestsAdapter.CenterViewHolder>() {

    private var filteredCenters: List<Center> = listOf()

    init {
        filterCenters()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CenterViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.center_request_card, parent, false)
        return CenterViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CenterViewHolder, position: Int) {
        holder.bind(filteredCenters[position])
    }

    override fun getItemCount() = filteredCenters.size

    fun updateData(newCenters: List<Center>) {
        centers = newCenters
        filterCenters()
        notifyDataSetChanged()
    }

    private fun filterCenters() {
        filteredCenters = centers.filter { it.centerStatus == false }
    }

    inner class CenterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val servicesTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val profileImageView: CircleImageView = itemView.findViewById(R.id.profileImageView)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val rejectButton: Button = itemView.findViewById(R.id.rejectButton)
        val openPdfButton: Button = itemView.findViewById(R.id.openPdfButton)

        init {
            acceptButton.setOnClickListener {
                val center = filteredCenters[adapterPosition]
                center.centerStatus = true
                updateCenterInFirebase(center)
            }

            rejectButton.setOnClickListener {
                val center = filteredCenters[adapterPosition]
                removeCenterFromFirebase(center)
            }

            openPdfButton.setOnClickListener {
                val center = filteredCenters[adapterPosition]
                val pdfUrl = center.certificate
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
                itemView.context.startActivity(intent)
            }
        }

        private fun updateCenterInFirebase(center: Center) {
            val centersRef = FirebaseDatabase.getInstance().getReference("centers")
            centersRef.child(center.id).setValue(center)
        }

        private fun removeCenterFromFirebase(center: Center) {
            val centersRef = FirebaseDatabase.getInstance().getReference("centers")
            centersRef.child(center.id).removeValue()
        }

        fun bind(center: Center) {
            nameTextView.text = center.name
            servicesTextView.text = center.category
            locationTextView.text = center.address
            Glide.with(itemView.context)
                .load(center.picture)
                .into(profileImageView)
        }
    }
}
