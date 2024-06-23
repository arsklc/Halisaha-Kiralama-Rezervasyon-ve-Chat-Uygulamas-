package com.kilica.bitirmeproje.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.activities.UserProfile
import com.kilica.bitirmeproje.models.ReservationRequest

class ReservationRequestsAdapter(
    private val requests: List<ReservationRequest>,
    private val onRequestAction: (ReservationRequest, Boolean) -> Unit
) : RecyclerView.Adapter<ReservationRequestsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewRequestDetails: TextView = view.findViewById(R.id.text_view_request_details)
        val buttonAccept: Button = view.findViewById(R.id.button_accept)
        val buttonReject: Button = view.findViewById(R.id.button_reject)
        val buttonViewProfile: Button = view.findViewById(R.id.button_view_profile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reservation_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        holder.textViewRequestDetails.text = "${request.date}, ${request.hour}:00 - ${request.halisahaName}"

        holder.buttonAccept.setOnClickListener {
            onRequestAction(request, true)
        }

        holder.buttonReject.setOnClickListener {
            onRequestAction(request, false)
        }

        holder.buttonViewProfile.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, UserProfile::class.java)
            intent.putExtra("userId", request.requestedBy)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = requests.size
}
