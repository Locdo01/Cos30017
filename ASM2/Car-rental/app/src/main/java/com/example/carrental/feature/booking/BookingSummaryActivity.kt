package com.example.carrental.feature.booking

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.carrental.common.BaseCarRentalActivity
import com.example.carrental.common.CarUiText
import com.example.carrental.data.local.BookingDraftStore
import com.example.carrental.data.model.BookingDraft
import com.example.carrental.data.model.Car
import com.example.carrental.data.model.Rental
import com.example.carrental.data.repository.AppSettingsRepository
import com.example.carrental.data.repository.CarRepository
import com.example.carrental.data.repository.RentalRepository
import com.example.carrental.data.repository.WalletRepository
import com.example.carrental.domain.booking.BookingPolicy
import com.example.carrental.feature.details.CarDetailsActivity
import com.example.carrental.R
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// QUICK FIND: booking summary, balance check, confirm, cancel, result dialog.
// Reviews a new booking or an existing rental and handles its final actions.


class BookingSummaryActivity : BaseCarRentalActivity() {

    private lateinit var draft: BookingDraft
    private lateinit var car: Car
    private var existingRental: Rental? = null

    private val dateFormat = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    ).apply {
        isLenient = false
    }

    // ===== SCREEN SETUP =====
    // Initializes the screen and connects its controls.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_summary)

        val existingRentalId = intent.getIntExtra(EXTRA_RENTAL_CAR_ID, -1)
        if (existingRentalId > 0) {
            existingRental = RentalRepository.getRental(existingRentalId)
            val savedRental = existingRental
            if (savedRental == null) {
                finish()
                return
            }
            draft = savedRental.bookingDraft?.copy() ?: BookingDraft(
                carId = savedRental.carId,
                pickUpDate = savedRental.pickUpDate,
                dropOffDate = savedRental.dropOffDate
            )
        } else {
            val currentDraft = BookingDraftStore.current
            if (currentDraft == null) {
                finish()
                return
            }
            draft = currentDraft
        }

        val selectedCar = CarRepository.cars.firstOrNull {
            it.id == draft.carId
        }

        if (selectedCar == null) {
            showError(getString(R.string.error_car_not_found))
            return
        }

        car = selectedCar

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnConfirmBooking).setOnClickListener {
            if (existingRental == null) confirmBooking() else saveExistingRental()
        }

        findViewById<Button>(R.id.btnSaveRental).setOnClickListener {
            saveExistingRental()
        }

        findViewById<Button>(R.id.btnCancelExistingRental).setOnClickListener {
            confirmCancelExistingRental()
        }

        if (existingRental != null) {
            findViewById<View>(R.id.layoutExistingRentalActions).visibility = View.VISIBLE
            findViewById<Button>(R.id.btnConfirmBooking).visibility = View.GONE
        }

        showSummary()
    }

    // ===== SUMMARY CONTENT =====
    // Displays customer, driver, car, dates, cost, and balance.
    private fun showSummary() {
        val rentalDates = getValidRentalDates() ?: return
        val (pickUpDate, dropOffDate) = rentalDates
        val rentalDays = countDays(pickUpDate, dropOffDate)
        val totalCost = existingRental?.totalCost ?: calculateTotalCost(rentalDays)

        val customerInfo = listOf(
            getString(R.string.summary_name, draft.name),
            getString(R.string.summary_birth_date, draft.dateOfBirth),
            getString(R.string.summary_email, draft.email),
            getString(R.string.summary_phone, draft.phone)
        ).joinToString("\n")

        val rentalInfo = listOf(
            getString(R.string.summary_car, car.name, car.model),
            getString(R.string.summary_year, car.year),
            getString(R.string.summary_pickup, draft.pickUpDate),
            getString(R.string.summary_dropoff, draft.dropOffDate),
            getString(R.string.summary_duration, rentalDays)
        ).joinToString("\n")

        val address = listOf(
            draft.streetAddress,
            draft.district,
            draft.city
        ).filter { it.isNotBlank() }.joinToString(", ")

        val driverLines = mutableListOf(
            getString(R.string.summary_id_card, draft.idCard),
            getString(R.string.summary_license, draft.licenseNumber),
            getString(
                R.string.summary_license_class,
                CarUiText.licenseClass(this, draft.licenseClass)
            ),
            getString(R.string.summary_license_expiry, draft.licenseExpiry),
            getString(R.string.summary_pickup_address, address)
        )

        if (draft.additionalDetails.isNotBlank()) {
            driverLines.add(
                getString(R.string.summary_additional_details, draft.additionalDetails)
            )
        }

        findViewById<TextView>(R.id.tvCustomerInfo).text = customerInfo
        findViewById<TextView>(R.id.tvRentalInfo).text = rentalInfo
        findViewById<TextView>(R.id.tvDriverInfo).text =
            driverLines.joinToString("\n")

        findViewById<TextView>(R.id.tvCostPerDay).text =
            getString(R.string.car_cost_per_day, formatCredits(car.dailyCost))

        findViewById<TextView>(R.id.tvTotalCost).text =
            getString(R.string.credits_amount, formatCredits(totalCost))

        findViewById<TextView>(R.id.tvBalance).text =
            getString(
                R.string.summary_current_balance,
                formatCredits(WalletRepository.getBalance())
            )
    }

    // ===== CONFIRM, SAVE, AND CANCEL =====
    // Validates, charges, and saves the new booking.
    private fun confirmBooking() {
        if (!validateDraft()) return

        if (RentalRepository.isRented(car.id)) {
            showError(getString(R.string.error_car_already_rented))
            return
        }

        val dates = getValidRentalDates() ?: return
        val days = countDays(dates.first, dates.second)
        val totalCost = calculateTotalCost(days)

        if (!BookingPolicy.isAllowedCost(totalCost.toLong())) {
            showError(
                getString(
                    R.string.error_cost_limit,
                    BookingPolicy.MAX_TOTAL_COST
                )
            )
            return
        }

        if (totalCost > WalletRepository.getBalance()) {
            showError(
                getString(
                    R.string.error_balance_insufficient,
                    formatCredits(totalCost),
                    formatCredits(WalletRepository.getBalance())
                )
            )
            return
        }

        if (!WalletRepository.spend(totalCost)) {
            showError(getString(R.string.error_balance_changed))
            return
        }

        val rental = Rental(
            carId = car.id,
            pickUpDate = draft.pickUpDate,
            dropOffDate = draft.dropOffDate,
            totalCost = totalCost,
            bookingDraft = draft.copy()
        )

        if (!RentalRepository.rent(rental)) {
            // Return the credits if the rental could not be saved.
            WalletRepository.refund(totalCost)
            showError(getString(R.string.error_booking_save))
            return
        }

        showSuccess(
            getString(
                R.string.booking_success_balance,
                formatCredits(WalletRepository.getBalance())
            )
        )
    }

    // Checks that the booking draft contains valid required details.
    private fun validateDraft(): Boolean {
        if (!draft.termsAccepted) {
            showError(getString(R.string.error_accept_terms))
            return false
        }

        if (draft.name.isBlank() ||
            draft.dateOfBirth.isBlank() ||
            draft.email.isBlank() ||
            draft.phone.isBlank()
        ) {
            showError(getString(R.string.error_personal_incomplete))
            return false
        }

        if (draft.idCard.isBlank() ||
            draft.licenseNumber.isBlank() ||
            draft.licenseClass.isBlank() ||
            draft.licenseExpiry.isBlank() ||
            draft.city.isBlank() ||
            draft.district.isBlank() ||
            draft.streetAddress.isBlank()
        ) {
            showError(getString(R.string.error_details_incomplete))
            return false
        }

        if (draft.additionalDetails != draft.additionalDetails.trim()) {
            draft.additionalDetails = draft.additionalDetails.trim()
        }

        if (getValidRentalDates() == null) {
            showError(getString(R.string.error_dates_invalid))
            return false
        }

        return true
    }

    /** Save the existing booking details and return to the rentals list. */
    // Validates and saves changes to an existing rental.
    private fun saveExistingRental() {
        val saved = existingRental ?: return
        val updatedRental = saved.copy(
            pickUpDate = draft.pickUpDate,
            dropOffDate = draft.dropOffDate,
            bookingDraft = draft.copy()
        )

        if (!RentalRepository.update(updatedRental)) {
            showError(getString(R.string.error_save_booking))
            return
        }

        finish()
    }

    /** Ask before cancelling, then remove the rental and refund its saved cost. */
    // Asks for confirmation before cancelling the saved rental.
    private fun confirmCancelExistingRental() {
        val saved = existingRental ?: return
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.cancel_rental_question))
            .setMessage(
                getString(
                    R.string.cancel_rental_refund,
                    formatCredits(saved.totalCost)
                )
            )
            .setNegativeButton(getString(R.string.rental_keep), null)
            .setPositiveButton(getString(R.string.rental_cancel_action)) { _, _ ->
                if (RentalRepository.cancel(saved.carId)) {
                    WalletRepository.refund(saved.totalCost)
                    finish()
                } else {
                    showError(getString(R.string.error_cancel_booking))
                }
            }
            .show()
    }

    // Parses and validates the pickup and drop-off dates.
    private fun getValidRentalDates(): Pair<Date, Date>? {
        val pickUp = parseDate(draft.pickUpDate) ?: return null
        val dropOff = parseDate(draft.dropOffDate) ?: return null

        if (pickUp.time != today().timeInMillis) return null
        if (!dropOff.after(pickUp)) return null
        if (!BookingPolicy.isAllowedDuration(countDays(pickUp, dropOff))) return null

        return pickUp to dropOff
    }

    // Calculates the rental cost for the selected number of days.
    private fun calculateTotalCost(days: Int): Int {
        val total = car.dailyCost.toLong() * days

        if (total < 0 || total > Int.MAX_VALUE) {
            return -1
        }

        return total.toInt()
    }

    // Counts rental days between the selected dates.
    private fun countDays(start: Date, end: Date): Int {
        val calendar = Calendar.getInstance().apply {
            time = start
            clearTime(this)
        }

        var days = 0
        while (calendar.time.before(end)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            days++
        }

        return days
    }

    // Parses a date strictly and returns null when it is invalid.
    private fun parseDate(value: String): Date? {
        val position = ParsePosition(0)
        val parsedDate = dateFormat.parse(value, position)

        return if (parsedDate != null &&
            position.index == value.length
        ) {
            parsedDate
        } else {
            null
        }
    }

    // Returns today with the time fields cleared.
    private fun today(): Calendar =
        Calendar.getInstance().apply {
            clearTime(this)
        }

    // Clears time fields for accurate date-only comparisons.
    private fun clearTime(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    // Formats a credit amount for display.
    private fun formatCredits(amount: Int): String =
        String.format(Locale.getDefault(), "%,d", amount)

    // Displays an error dialog and plays the failure sound.
    private fun showError(message: String) {
        showResult(
            success = false,
            message = message
        )
    }

    // Displays a successful result to the user.
    private fun showSuccess(message: String) {
        showResult(
            success = true,
            message = message
        )
    }

    // ===== RESULT DIALOG AND SOUND =====
    // Displays a result dialog and plays the matching sound.
    private fun showResult(success: Boolean, message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_rental_result)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.findViewById<TextView>(R.id.tvResultTitle).apply {
            text = if (success) getString(R.string.success_title)
            else getString(R.string.error_title)
            setTextColor(
                if (success) Color.rgb(39, 130, 74)
                else Color.rgb(190, 45, 45)
            )
        }

        dialog.findViewById<TextView>(R.id.tvResultMessage).text = message

        dialog.show()
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.setDimAmount(0.6f)

        val width = (resources.displayMetrics.widthPixels * 0.88).toInt()
        dialog.window?.setLayout(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // Closes the dialog and returns to car details after a successful booking.
        fun closeDialog() {
            dialog.dismiss()

            if (success) {
                BookingDraftStore.clear()
                returnToCarDetails()
            }
        }

        dialog.findViewById<View>(R.id.btnClose).setOnClickListener {
            closeDialog()
        }

        dialog.findViewById<Button>(R.id.btnDismiss).setOnClickListener {
            closeDialog()
        }

        // Keep the assignment-required brief result message alongside the detailed dialog.
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        if (AppSettingsRepository.isSoundEnabled(this)) {
            val soundId = if (success) {
                R.raw.rental_success
            } else {
                R.raw.rental_failure
            }

            MediaPlayer.create(this, soundId)?.apply {
                setOnCompletionListener { player ->
                    player.release()
                }
                start()
            }
        }
    }

    // Returns to the details screen for this car.
    private fun returnToCarDetails() {
        val intent = Intent(this, CarDetailsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(CarDetailsActivity.EXTRA_CAR_ID, car.id)
        }

        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_RENTAL_CAR_ID = "extra_rental_car_id"
    }
}
