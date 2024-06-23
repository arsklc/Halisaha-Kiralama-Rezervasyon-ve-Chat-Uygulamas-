package com.kilica.bitirmeproje.models

data class Sohbet(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val participants: List<String> = listOf(),
    var senderName: String = "",
    var receiverName: String = ""
)