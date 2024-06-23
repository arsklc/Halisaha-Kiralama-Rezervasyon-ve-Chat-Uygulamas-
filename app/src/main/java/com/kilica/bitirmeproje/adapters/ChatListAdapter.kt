package com.kilica.bitirmeproje.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.models.Sohbet

class ChatListAdapter(
    private val chatList: List<Sohbet>,
    private val onChatClicked: (Sohbet) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val sohbet = chatList[position]
        holder.bind(sohbet)
        holder.itemView.setOnClickListener {
            onChatClicked(sohbet)
        }
    }

    override fun getItemCount() = chatList.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        private val textViewLastMessage: TextView = itemView.findViewById(R.id.textViewLastMessage)

        fun bind(sohbet: Sohbet) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            textViewName.text = if (sohbet.senderId == currentUserId) sohbet.receiverName else sohbet.senderName
            textViewLastMessage.text = sohbet.message
        }
    }
}
