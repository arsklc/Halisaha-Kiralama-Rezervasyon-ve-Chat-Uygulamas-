package com.kilica.bitirmeproje.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.models.Halisaha

class HalisahaAdapter(
    private var halisahalar: List<Halisaha>,
    private val onClick: (Halisaha) -> Unit
) : RecyclerView.Adapter<HalisahaAdapter.HalisahaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HalisahaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_halisaha, parent, false)
        return HalisahaViewHolder(view)
    }

    override fun onBindViewHolder(holder: HalisahaViewHolder, position: Int) {
        holder.bind(halisahalar[position])
    }

    override fun getItemCount(): Int = halisahalar.size

    fun updateHalisahalar(newHalisahalar: List<Halisaha>) {
        halisahalar = newHalisahalar
        notifyDataSetChanged()
    }

    inner class HalisahaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.halisahaName)
        private val cityTextView: TextView = itemView.findViewById(R.id.halisahaCity)
        private val districtTextView: TextView = itemView.findViewById(R.id.halisahaDistrict)
        private val imageView: ImageView = itemView.findViewById(R.id.halisahaImage)

        fun bind(halisaha: Halisaha) {
            nameTextView.text = halisaha.name
            cityTextView.text = halisaha.city
            districtTextView.text = halisaha.district

            if (halisaha.imageUrl.isNotEmpty()) {
                Log.d("HalisahaAdapter", "Loading image from URL: ${halisaha.imageUrl}")
                Glide.with(itemView.context)
                    .load(Uri.parse(halisaha.imageUrl))
                    .placeholder(R.drawable.fotoekle)
                    .into(imageView)
            } else {
                Log.d("HalisahaAdapter", "No image URL found")

                imageView.setImageResource(R.drawable.fotoekle)
            }
            itemView.setOnClickListener {
                onClick(halisaha)
            }
        }
    }
}
