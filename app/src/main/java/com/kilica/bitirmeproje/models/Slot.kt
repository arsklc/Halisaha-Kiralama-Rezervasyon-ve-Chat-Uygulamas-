package com.kilica.bitirmeproje.models

import java.util.Date

data class Slot(
    val date: Date,
    val hour: Int,
    var isReserved: Boolean
)
