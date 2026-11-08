package com.example.carrental.domain.booking

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


/** Shared limits for one assignment booking. */
object BookingPolicy {
    const val MAX_RENTAL_DAYS = 7
    const val MAX_TOTAL_COST = 400

    fun isAllowedDuration(days: Int): Boolean = days in 1..MAX_RENTAL_DAYS

    fun isAllowedCost(totalCost: Long): Boolean =
        totalCost in 1L..MAX_TOTAL_COST.toLong()

    /** Requires pickup today and a drop-off between one and seven days later. */
    fun isValidRentalPeriod(pickUpText: String, dropOffText: String): Boolean {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            isLenient = false
        }
        val pickUp = parseDate(format, pickUpText) ?: return false
        val dropOff = parseDate(format, dropOffText) ?: return false
        val today = Calendar.getInstance().apply { clearTime() }
        val pickupCalendar = Calendar.getInstance().apply {
            time = pickUp
            clearTime()
        }
        val dropOffCalendar = Calendar.getInstance().apply {
            time = dropOff
            clearTime()
        }

        if (pickupCalendar.timeInMillis != today.timeInMillis) return false
        if (!dropOffCalendar.after(pickupCalendar)) return false

        var days = 0
        while (pickupCalendar.before(dropOffCalendar) && days <= MAX_RENTAL_DAYS) {
            pickupCalendar.add(Calendar.DAY_OF_YEAR, 1)
            days++
        }
        return isAllowedDuration(days)
    }

    /** Parses a date only when the complete input matches dd/MM/yyyy. */
    private fun parseDate(format: SimpleDateFormat, value: String): Date? {
        val position = ParsePosition(0)
        val date = format.parse(value.trim(), position) ?: return null
        return date.takeIf { position.index == value.trim().length }
    }

    /** Removes the time fields so calendar-day comparisons are consistent. */
    private fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
