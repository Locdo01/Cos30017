package com.example.carrental

import com.example.carrental.domain.booking.BookingPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingPolicyTest {

    @Test
    fun rentalDurationMustBeWithinOneAndSevenDays() {
        assertFalse(BookingPolicy.isAllowedDuration(0))
        assertTrue(BookingPolicy.isAllowedDuration(1))
        assertTrue(BookingPolicy.isAllowedDuration(7))
        assertFalse(BookingPolicy.isAllowedDuration(8))
    }

    @Test
    fun rentalCostMustBePositiveAndWithinTheLimit() {
        assertFalse(BookingPolicy.isAllowedCost(0))
        assertTrue(BookingPolicy.isAllowedCost(400))
        assertFalse(BookingPolicy.isAllowedCost(401))
    }

    @Test
    fun rentalPeriodMustStartTodayAndStayWithinSevenDays() {
        val today = Calendar.getInstance().apply { clearTime() }
        val tomorrow = (today.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val eightDaysLater = (today.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 8)
        }
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        assertTrue(
            BookingPolicy.isValidRentalPeriod(
                formatter.format(today.time),
                formatter.format(tomorrow.time)
            )
        )
        assertFalse(
            BookingPolicy.isValidRentalPeriod(
                formatter.format(today.time),
                formatter.format(eightDaysLater.time)
            )
        )
    }

    private fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
