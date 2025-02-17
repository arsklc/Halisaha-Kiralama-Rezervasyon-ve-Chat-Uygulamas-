package com.kilica.bitirmeproje.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kilica.bitirmeproje.R
import java.text.SimpleDateFormat
import java.util.*

class DateAdapter(
    private val dates: List<Date>,
    private val onDateClick: (Date) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = dates[position]
        holder.bind(date)
    }

    override fun getItemCount(): Int = dates.size

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewDate: TextView = itemView.findViewById(R.id.text_view_date)

        fun bind(date: Date) {
            textViewDate.text = dateFormat.format(date)

            itemView.setOnClickListener {
                onDateClick(date)
            }
        }
    }
}