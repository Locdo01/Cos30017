package com.example.carrental.common

import android.content.Context
import com.example.carrental.R

// QUICK FIND: localized car labels, body type, transmission, fuel, seat count.
// Converts catalog values into text for the currently selected app language.


/** Translates the small set of car-specification labels stored as plain text in the catalog. */
object CarUiText {

    // Returns the localized body-type label.
    fun bodyType(context: Context, value: String): String = when (value.lowercase()) {
        "suv" -> context.getString(R.string.car_body_suv)
        "sedan" -> context.getString(R.string.car_body_sedan)
        "coupe" -> context.getString(R.string.car_body_coupe)
        else -> value
    }

    // Returns the localized transmission label.
    fun transmission(context: Context, value: String): String = when (value.lowercase()) {
        "automatic" -> context.getString(R.string.car_transmission_automatic)
        "manual" -> context.getString(R.string.car_transmission_manual)
        else -> value
    }

    // Returns the localized fuel label.
    fun fuel(context: Context, value: String): String = when (value.lowercase()) {
        "electric" -> context.getString(R.string.car_fuel_electric)
        "petrol" -> context.getString(R.string.car_fuel_petrol)
        else -> value
    }

    // Returns the localized driving-license label.
    fun licenseClass(context: Context, value: String): String = when (value) {
        "B - Automatic" -> context.getString(R.string.license_class_b_auto)
        "B - Manual" -> context.getString(R.string.license_class_b_manual)
        else -> value
    }

    /** Formats the number of seats using the active app language. */
    // Formats the seat count with the correct translated plural.
    fun seatCount(context: Context, seats: Int): String =
        context.resources.getQuantityString(R.plurals.car_seats, seats, seats)
}
