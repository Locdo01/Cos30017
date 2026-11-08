package com.example.carrental.data.repository

import com.example.carrental.data.model.BookingDraft
import com.example.carrental.data.model.Rental
import com.example.carrental.domain.booking.BookingPolicy

/** Keeps active rentals in memory for the lifetime of the app process. */
object RentalRepository {

    private val rentalsByCarId = linkedMapOf<Int, Rental>()

    /** Returns the active rental for a car, if one exists. */
    fun getRental(carId: Int): Rental? = rentalsByCarId[carId]

    /** Checks whether a car is currently rented. */
    fun isRented(carId: Int): Boolean = rentalsByCarId.containsKey(carId)

    /** Returns a copy of all active rentals. */
    fun getAllRentals(): List<Rental> = rentalsByCarId.values.toList()

    /** Adds a rental only when it is valid and the car is available. */
    fun rent(rental: Rental): Boolean {
        if (!isValid(rental) || isRented(rental.carId)) return false
        rentalsByCarId[rental.carId] = rental
        return true
    }

    /** Replaces an existing rental after validating its required values. */
    fun update(rental: Rental): Boolean {
        if (!isValid(rental) || !isRented(rental.carId)) return false
        rentalsByCarId[rental.carId] = rental
        return true
    }

    /** Removes a rental so the car becomes available again. */
    fun cancel(carId: Int): Boolean = rentalsByCarId.remove(carId) != null

    /** Rejects incomplete rentals and costs outside the assignment limit. */
    private fun isValid(rental: Rental): Boolean =
        rental.carId > 0 &&
                BookingPolicy.isValidRentalPeriod(rental.pickUpDate, rental.dropOffDate) &&
                BookingPolicy.isAllowedCost(rental.totalCost.toLong())
}
