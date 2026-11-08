package com.example.carrental.data.model



/** Details for one car rental kept during the current app session. */
data class Rental(
    val carId: Int,
    val pickUpDate: String,
    val dropOffDate: String,
    val totalCost: Int,
    val bookingDraft: BookingDraft? = null
)
