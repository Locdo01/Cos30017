package com.example.carrental.data.local

import com.example.carrental.data.model.BookingDraft

// Holds the current booking draft while the user moves between activities.
object BookingDraftStore {
    var current: BookingDraft? = null

    // Starts a clean draft for the selected car.
    fun start(carId: Int) {
        current = BookingDraft(carId = carId)
    }

    // Discards the unfinished booking draft.
    fun clear() {
        current = null
    }
}
