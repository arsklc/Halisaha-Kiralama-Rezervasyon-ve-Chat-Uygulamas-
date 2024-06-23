package com.kilica.bitirmeproje.fragments.halisahascreenfrags

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.activities.Chat
import com.kilica.bitirmeproje.adapters.ChatListAdapter
import com.kilica.bitirmeproje.models.Sohbet

class MesajEkrani : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatListAdapter: ChatListAdapter
    private val chatList = mutableListOf<Sohbet>()
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mesaj_ekrani, container, false)

        firestore = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.recyclerVieww)
        recyclerView.layoutManager = LinearLayoutManager(context)
        chatListAdapter = ChatListAdapter(chatList) { sohbet ->
            val intent = Intent(context, Chat::class.java)
            val receiverId = if (sohbet.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                sohbet.receiverId
            } else {
                sohbet.senderId
            }
            intent.putExtra("receiverId", receiverId)
            startActivity(intent)
        }
        recyclerView.adapter = chatListAdapter

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchChats()
    }

    private fun fetchChats() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firestore.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                chatList.clear()
                val tempMap = mutableMapOf<String, Sohbet>()
                for (doc in value!!) {
                    val sohbet = doc.toObject(Sohbet::class.java)
                    val otherUserId = if (sohbet.senderId == currentUserId) sohbet.receiverId else sohbet.senderId
                    if (!tempMap.containsKey(otherUserId) || tempMap[otherUserId]?.timestamp ?: 0 < sohbet.timestamp) {
                        tempMap[otherUserId] = sohbet
                    }
                }

                val userIds = tempMap.keys.toList()
                firestore.collection("users").whereIn(FieldPath.documentId(), userIds)
                    .get()
                    .addOnSuccessListener { documents ->
                        val userMap = mutableMapOf<String, String>()
                        for (document in documents) {
                            val firstName = document.getString("firstName") ?: ""
                            val lastName = document.getString("lastName") ?: ""
                            val fullName = "$firstName $lastName"
                            userMap[document.id] = fullName
                        }

                        for (sohbet in tempMap.values) {
                            val otherUserId = if (sohbet.senderId == currentUserId) sohbet.receiverId else sohbet.senderId
                            sohbet.receiverName = userMap[sohbet.receiverId] ?: ""
                            sohbet.senderName = userMap[sohbet.senderId] ?: ""
                            chatList.add(sohbet)
                        }
                        chatList.sortByDescending { it.timestamp }
                        chatListAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Kullanıcı adları alınamadı", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}