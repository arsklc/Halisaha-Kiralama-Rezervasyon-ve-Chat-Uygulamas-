package com.kilica.bitirmeproje.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.models.Slot

class SlotsAdapter(
    private val slots: List<Slot>,
    private val onSlotClick: (Slot) -> Unit
) : RecyclerView.Adapter<SlotsAdapter.SlotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slot = slots[position]
        holder.bind(slot)
    }

    override fun getItemCount(): Int = slots.size

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewHour: TextView = itemView.findViewById(R.id.text_view_hour)
        private val textViewStatus: TextView = itemView.findViewById(R.id.text_view_status)

        fun bind(slot: Slot) {
            textViewHour.text = "${slot.hour}:00"
            textViewStatus.text = if (slot.isReserved) "Rezerve" else "Müsait"

            itemView.setOnClickListener {
                onSlotClick(slot)
            }
        }
    }
}

