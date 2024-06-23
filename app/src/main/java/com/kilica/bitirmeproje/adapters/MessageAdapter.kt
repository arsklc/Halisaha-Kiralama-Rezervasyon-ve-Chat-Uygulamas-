package com.kilica.bitirmeproje.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.models.Sohbet

class MessageAdapter(private val messageList: List<Sohbet>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.bind(message, currentUserId)
    }

    override fun getItemCount() = messageList.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userMessageLayout: LinearLayout = itemView.findViewById(R.id.userMessageLayout)
        private val receiverMessageLayout: LinearLayout = itemView.findViewById(R.id.receiverMessageLayout)
        private val textViewUserMessage: TextView = itemView.findViewById(R.id.textViewUserMessage)
        private val textViewReceiverMessage: TextView = itemView.findViewById(R.id.textViewReceiverMessage)

        fun bind(message: Sohbet, currentUserId: String?) {
            if (message.senderId == currentUserId) {
                userMessageLayout.visibility = View.VISIBLE
                receiverMessageLayout.visibility = View.GONE
                textViewUserMessage.text = message.message
            } else {
                userMessageLayout.visibility = View.GONE
                receiverMessageLayout.visibility = View.VISIBLE
                textViewReceiverMessage.text = message.message
            }
        }
    }
}