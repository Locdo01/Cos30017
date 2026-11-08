package com.example.carrental.feature.booking

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.carrental.common.BaseCarRentalActivity
import androidx.core.content.IntentCompat
import com.example.carrental.data.local.BookingDraftStore
import com.example.carrental.data.model.Car
import com.example.carrental.data.repository.AppSettingsRepository
import com.example.carrental.data.repository.CarRepository
import com.example.carrental.data.repository.WalletRepository
import com.example.carrental.domain.booking.BookingPolicy
import com.example.carrental.feature.details.CarDetailsActivity
import com.example.carrental.R
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// QUICK FIND: personal information, input trimming, validation, booking dates.
// Validates customer details and rental dates before continuing the booking flow.


class PersonalInformationActivity : BaseCarRentalActivity() {

    private lateinit var nameInput: EditText
    private lateinit var dayInput: EditText
    private lateinit var monthInput: EditText
    private lateinit var yearInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var pickUpInput: EditText
    private lateinit var dropOffInput: EditText
    private lateinit var termsCheckBox: CheckBox
    private lateinit var rentalDaysSeekBar: SeekBar
    private lateinit var rentalDaysLabel: TextView

    private val dateFormat = SimpleDateFormat(
        "dd/MM/yyyy", Locale.getDefault()).apply {
        isLenient = false
    }

    // Maximum name length used by this form.
    private val maximumNameLength = 50

    // ===== SCREEN SETUP =====
    // Initializes the screen and connects its controls.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_information)

        bindViews()

        // Start or restore the booking draft using the selected car ID.
        val passedCar = IntentCompat.getParcelableExtra(
            intent,
            CarDetailsActivity.EXTRA_CAR,
            Car::class.java
        )
        val carId = intent.getIntExtra(
            CarDetailsActivity.EXTRA_CAR_ID,
            passedCar?.id ?: -1
        )

        if (carId > 0 && (BookingDraftStore.current == null ||
                    BookingDraftStore.current?.carId != carId)
        ) { BookingDraftStore.start(carId) }

        if (BookingDraftStore.current == null) {
            showError(getString(R.string.error_car_not_found))
            finish()
            return
        }

        restoreDraft()
        setupDatePickers()
        setupButtons()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelBookingAndExit()
            }
        })
    }

    // ===== FORM RESTORE AND DATE PICKERS =====
    // Connects screen fields to their layout views.
    private fun bindViews() {
        nameInput = findViewById(R.id.etName)
        dayInput = findViewById(R.id.etDay)
        monthInput = findViewById(R.id.etMonth)
        yearInput = findViewById(R.id.etYear)
        emailInput = findViewById(R.id.etEmail)
        phoneInput = findViewById(R.id.etPhone)
        pickUpInput = findViewById(R.id.etPickUpDate)
        dropOffInput = findViewById(R.id.etDropOffDate)
        termsCheckBox = findViewById(R.id.cbTerms)
        rentalDaysSeekBar = findViewById(R.id.seekRentalDays)
        rentalDaysLabel = findViewById(R.id.tvRentalDays)
    }

    // Connects the screen buttons to their actions.
    private fun setupButtons() {
        findViewById<View>(R.id.btnBack).setOnClickListener {
            cancelBookingAndExit()
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            if (validateForm()) {
                saveDraftAndContinue()
            }
        }
    }

    // Restores previously entered values from the booking draft.
    private fun restoreDraft() {
        val draft = BookingDraftStore.current ?: return

        nameInput.setText(draft.name)

        val dateParts = draft.dateOfBirth.split("/")
        if (dateParts.size == 3) {
            dayInput.setText(dateParts[0])
            monthInput.setText(dateParts[1])
            yearInput.setText(dateParts[2])
        }

        emailInput.setText(draft.email)
        phoneInput.setText(draft.phone)
        // The assignment uses immediate pickup, so begin with today's date.
        val pickupDate = today().time
        pickUpInput.setText(dateFormat.format(pickupDate))

        val restoredDropOff = parseDate(draft.dropOffDate)
        val restoredDays = if (restoredDropOff != null) {
            countDays(pickupDate, restoredDropOff).coerceIn(1, BookingPolicy.MAX_RENTAL_DAYS)
        } else {
            1
        }
        rentalDaysSeekBar.progress = restoredDays - 1
        updateDropOffDate(restoredDays)
        termsCheckBox.isChecked = draft.termsAccepted
    }

    // Connects date fields to calendar pickers.
    private fun setupDatePickers() {
        pickUpInput.showSoftInputOnFocus = false
        dropOffInput.showSoftInputOnFocus = false

        pickUpInput.setOnClickListener {
            showDatePicker(pickUpInput)
        }

        // Drop-off is calculated from today's pickup and the duration slider.
        dropOffInput.isClickable = false

        rentalDaysSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateDropOffDate(progress + 1)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        updateDropOffDate(rentalDaysSeekBar.progress + 1)
    }

    private fun updateDropOffDate(days: Int) {
        val rentalDays = days.coerceIn(1, BookingPolicy.MAX_RENTAL_DAYS)
        val pickupDate = parseDate(pickUpInput.text.toString()) ?: today().time
        val dropOffDate = Calendar.getInstance().apply {
            time = pickupDate
            clearTime(this)
            add(Calendar.DAY_OF_MONTH, rentalDays)
        }
        dropOffInput.setText(dateFormat.format(dropOffDate.time))
        rentalDaysLabel.text = getString(R.string.rental_duration_days, rentalDays)
    }

    // Opens a calendar with the correct minimum allowed date.
    private fun showDatePicker(targetInput: EditText) {
        // Do not allow a rental date in the past.
        val minimumDate = today()

        val existingDate = parseDate(targetInput.text.toString())

        val initialDate = Calendar.getInstance().apply {
            time = existingDate ?: minimumDate.time
            clearTime(this)

            if (before(minimumDate)) {
                timeInMillis = minimumDate.timeInMillis
            }
        }

        val picker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth -> val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                targetInput.setText(dateFormat.format(selectedDate.time))
                updateDropOffDate(rentalDaysSeekBar.progress + 1)
            },
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        )

        picker.datePicker.minDate = minimumDate.timeInMillis
        // Immediate pickup means the only valid pickup date is today.
        picker.datePicker.maxDate = minimumDate.timeInMillis
        picker.show()
    }

    // ===== INPUT VALIDATION =====
    // Validates required fields before allowing the user to continue.
    private fun validateForm(): Boolean {
        trimAllInputs()

        if (!validateName()) return false
        if (!validateDateOfBirth()) return false
        if (!validateEmail()) return false
        if (!validatePhone()) return false
        if (!termsCheckBox.isChecked) {
            showError(getString(R.string.error_accept_terms))
            return false
        }
        if (!validateRentalDatesAndBalance()) return false

        return true
    }

    // Trims leading and trailing spaces from the input fields.
    private fun trimAllInputs() {
        // Remove leading and trailing spaces from every text field.
        nameInput.setText(nameInput.text.toString().trim())
        dayInput.setText(dayInput.text.toString().trim())
        monthInput.setText(monthInput.text.toString().trim())
        yearInput.setText(yearInput.text.toString().trim())
        emailInput.setText(emailInput.text.toString().trim())
        phoneInput.setText(phoneInput.text.toString().trim())
        pickUpInput.setText(pickUpInput.text.toString().trim())
        dropOffInput.setText(dropOffInput.text.toString().trim())
    }

    // Checks the name format and maximum length.
    private fun validateName(): Boolean {
        val name = nameInput.text.toString()

        if (name.isEmpty()) {
            showError(getString(R.string.error_name_required))
            return false
        }

        if (name.length > maximumNameLength) {
            showError(getString(R.string.error_name_length, maximumNameLength))
            return false
        }

        // Allow letters, spaces, apostrophes, and hyphens. This supports names
        // such as O'Connor and Jean-Luc while rejecting digits.
        val nameIsValid = name.all { character ->
            character.isLetter() ||
                    character.isWhitespace() ||
                    character == '\'' ||
                    character == '-'
        }

        if (!nameIsValid) {
            showError(getString(R.string.error_name_format))
            return false
        }

        return true
    }

    // Checks the birth date and allowed age range.
    private fun validateDateOfBirth(): Boolean {
        val day = dayInput.text.toString()
        val month = monthInput.text.toString()
        val year = yearInput.text.toString()

        if (day.isEmpty() || month.isEmpty() || year.isEmpty()) {
            showError(getString(R.string.error_dob_required))
            return false
        }

        val dayNumber = day.toIntOrNull()
        val monthNumber = month.toIntOrNull()
        val yearNumber = year.toIntOrNull()

        if (dayNumber == null || dayNumber !in 1..31) {
            showError(getString(R.string.error_day_range))
            return false
        }

        if (monthNumber == null || monthNumber !in 1..12) {
            showError(getString(R.string.error_month_range))
            return false
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        if (yearNumber == null ||
            yearNumber < currentYear - 80 ||
            yearNumber > currentYear
        ) { showError(getString(R.string.error_birth_year))
            return false
        }

        // Strict parsing rejects impossible dates such as 31/02/2000.
        val dateOfBirth = parseDate("$day/$month/$year")
        if (dateOfBirth == null) {
            showError(getString(R.string.error_birth_date))
            return false
        }

        val earliestAllowedDate = today().apply {
            add(Calendar.YEAR, -80)
        }

        if (dateOfBirth.before(earliestAllowedDate.time) ||
            dateOfBirth.after(today().time)
        ) {
            showError(getString(R.string.error_birth_age))
            return false
        }

        return true
    }

    // Checks that the email uses the required format.
    private fun validateEmail(): Boolean {
        val email = emailInput.text.toString()

        if (email.isEmpty()) {
            showError(getString(R.string.error_email_required))
            return false
        }

        val emailHasRequiredFormat = android.util.Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches() &&
                    email.contains("@") &&
                    email.lowercase(Locale.ROOT).endsWith(".com")

        if (!emailHasRequiredFormat) {
            showError(getString(R.string.error_email_format))
            return false
        }

        return true
    }

    // Checks that the phone number contains exactly ten digits.
    private fun validatePhone(): Boolean {
        val phone = phoneInput.text.toString()

        if (phone.isEmpty()) {
            showError(getString(R.string.error_phone_required))
            return false
        }

        // This form requires exactly 10 digits and does not allow letters,
        // spaces, or punctuation.
        if (!phone.matches(Regex("^[0-9]{10}$"))) {
            showError(getString(R.string.error_phone_format))
            return false
        }

        return true
    }

    // Checks rental dates and confirms the total fits the wallet balance.
    private fun validateRentalDatesAndBalance(): Boolean {
        val pickUp = parseDate(pickUpInput.text.toString())
        val dropOff = parseDate(dropOffInput.text.toString())

        if (pickUp == null) {
            showError(getString(R.string.error_pickup_required))
            return false
        }

        if (dropOff == null) {
            showError(getString(R.string.error_dropoff_required))
            return false
        }

        if (pickUp.time != today().timeInMillis) {
            showError(getString(R.string.error_pickup_immediate))
            return false
        }

        if (!dropOff.after(pickUp)) {
            showError(getString(R.string.error_dropoff_order))
            return false
        }

        val carId = BookingDraftStore.current?.carId ?: -1
        val car = CarRepository.cars.firstOrNull { it.id == carId }

        if (car == null) {
            showError(getString(R.string.error_car_not_found))
            return false
        }

        val numberOfDays = countDays(pickUp, dropOff)
        if (!BookingPolicy.isAllowedDuration(numberOfDays)) {
            showError(
                getString(
                    R.string.error_rental_days_limit,
                    BookingPolicy.MAX_RENTAL_DAYS
                )
            )
            return false
        }

        val totalCost = car.dailyCost.toLong() * numberOfDays

        if (!BookingPolicy.isAllowedCost(totalCost)) {
            showError(
                getString(
                    R.string.error_cost_limit,
                    BookingPolicy.MAX_TOTAL_COST
                )
            )
            return false
        }

        if (totalCost > WalletRepository.getBalance()) {
            showError(
                getString(
                    R.string.error_balance_dates,
                    totalCost,
                    WalletRepository.getBalance()
                )
            )
            return false
        }

        if (totalCost > Int.MAX_VALUE) {
            showError(getString(R.string.error_cost_overflow))
            return false
        }

        return true
    }

    // ===== SAVE AND CONTINUE =====
    // Saves validated personal details and opens the next booking screen.
    private fun saveDraftAndContinue() {
        val draft = BookingDraftStore.current ?: run {
            showError(getString(R.string.error_booking_unavailable))
            return
        }

        draft.name = nameInput.text.toString()
        draft.dateOfBirth = listOf(
            dayInput.text.toString(),
            monthInput.text.toString(),
            yearInput.text.toString()
        ).joinToString("/")

        draft.email = emailInput.text.toString()
        draft.phone = phoneInput.text.toString()
        draft.pickUpDate = pickUpInput.text.toString()
        draft.dropOffDate = dropOffInput.text.toString()
        draft.termsAccepted = termsCheckBox.isChecked

        startActivity(
            Intent(this, BookingDetailsActivity::class.java)
        )
    }

    // Clears the unfinished form and returns to car details as a cancellation.
    private fun cancelBookingAndExit() {
        BookingDraftStore.clear()
        Toast.makeText(this, R.string.booking_cancelled, Toast.LENGTH_LONG).show()
        finish()
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

    // Counts rental days between the selected dates.
    private fun countDays(startDate: Date, endDate: Date): Int {
        val calendar = Calendar.getInstance().apply {
            time = startDate
            clearTime(this)
        }

        var days = 0

        while (calendar.time.before(endDate)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            days++
        }

        return days
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

    // ===== ERROR DIALOG AND SOUND =====
    // Displays an error dialog and plays the failure sound.
    private fun showError(message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_rental_result)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.findViewById<TextView>(R.id.tvResultTitle).apply {
            text = getString(R.string.error_form_title)
            setTextColor(Color.rgb(190, 45, 45))
        }

        dialog.findViewById<TextView>(R.id.tvResultMessage).text = message

        dialog.show()
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.setDimAmount(0.6f)

        val dialogWidth =
            (resources.displayMetrics.widthPixels * 0.88).toInt()

        dialog.window?.setLayout(
            dialogWidth,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<View>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnDismiss).setOnClickListener {
            dialog.dismiss()
        }

        playFailureSound()
    }

    // Plays the failure sound when sound effects are enabled.
    private fun playFailureSound() {
        MediaPlayer.create(this, R.raw.rental_failure)?.apply {
            setOnCompletionListener { player ->
                player.release()
            }
            start()
        }
    }
}
