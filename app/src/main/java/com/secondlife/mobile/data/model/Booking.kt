package com.secondlife.mobile.data.model

import java.util.Date

data class Booking(
    val id: Int,
    val serviceName: String,
    val status: String,
    val date: Date,
    val price: Double
)