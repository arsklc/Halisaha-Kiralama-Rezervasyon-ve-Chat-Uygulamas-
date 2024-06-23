package com.kilica.bitirmeproje.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.adapters.MessageAdapter
import com.kilica.bitirmeproje.models.Sohbet

class Chat : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Sohbet>()
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var textViewChatPartnerName: TextView
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.recyclerView)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        textViewChatPartnerName = findViewById(R.id.textViewChatPartnerName)

        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter

        val receiverId = intent.getStringExtra("receiverId") ?: return
        val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Log.d("ChatActivity", "Chat started with receiverId: $receiverId, senderId: $senderId")

        buttonSend.setOnClickListener {
            val messageText = editTextMessage.text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(senderId, receiverId, messageText)
            }
        }

        fetchMessages(senderId, receiverId)
        fetchReceiverName(receiverId)
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }

    private fun fetchMessages(senderId: String, receiverId: String) {
        listenerRegistration = FirebaseFirestore.getInstance().collection("chats")
            .whereArrayContains("participants", senderId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("fetchMessages", "Error: ${error.message}")
                    return@addSnapshotListener
                }
                if (value != null) {
                    val newMessages = mutableListOf<Sohbet>()
                    for (doc in value) {
                        val message = doc.toObject(Sohbet::class.java)
                        if ((message.senderId == senderId && message.receiverId == receiverId) ||
                            (message.senderId == receiverId && message.receiverId == senderId)) {
                            newMessages.add(message)
                        }
                    }
                    messageList.clear()
                    messageList.addAll(newMessages)
                    messageAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messageList.size - 1)
                }
            }
    }


    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        if (message.isEmpty()) return

        val sohbet = Sohbet(senderId, receiverId, message, System.currentTimeMillis(), listOf(senderId, receiverId))
        FirebaseFirestore.getInstance().collection("chats")
            .add(sohbet)
            .addOnSuccessListener {
                Log.d("sendMessage", "Message sent successfully")
                editTextMessage.text.clear()
            }
            .addOnFailureListener { e ->
                Log.e("sendMessage", "Failed to send message: ${e.message}")
                Toast.makeText(this, "Mesaj gönderilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchReceiverName(receiverId: String) {
        FirebaseFirestore.getInstance().collection("users").document(receiverId)
            .get()
            .addOnSuccessListener { document ->
                val firstName = document.getString("firstName") ?: ""
                val lastName = document.getString("lastName") ?: ""
                val fullName = "$firstName $lastName"
                textViewChatPartnerName.text = fullName
            }
            .addOnFailureListener { e ->
                Log.e("fetchReceiverName", "Failed to fetch receiver name: ${e.message}")
                Toast.makeText(this, "Kullanıcı adı alınamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}