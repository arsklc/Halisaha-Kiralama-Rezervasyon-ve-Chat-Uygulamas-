package com.kilica.bitirmeproje.models

data class ReservationRequest(
    var id: String = "",
    var date: String = "",
    var hour: Int = 0,
    var isReserved: Boolean = false,
    var requestedBy: String = "",
    var halisahaName: String = "",
    var halisaha_id: String = ""
)
