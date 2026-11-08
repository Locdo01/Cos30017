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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.example.carrental.common.BaseCarRentalActivity
import com.example.carrental.data.repository.AppSettingsRepository
import com.example.carrental.data.local.BookingDraftStore
import com.example.carrental.data.model.BookingDraft
import com.example.carrental.R
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// QUICK FIND: booking details, driver license, address validation, save draft.
// Collects and validates driver and address information before the summary step.


class BookingDetailsActivity : BaseCarRentalActivity() {

    private lateinit var idCardInput: EditText
    private lateinit var licenseNumberInput: EditText
    private lateinit var licenseExpiryInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var districtInput: EditText
    private lateinit var streetAddressInput: EditText
    private lateinit var additionalDetailsInput: EditText
    private lateinit var licenseClassSpinner: Spinner

    private lateinit var draft: BookingDraft

    private val licenseClasses = listOf(
        "",
        "B - Automatic",
        "B - Manual",
        "C",
        "C1"
    )

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
        setContentView(R.layout.activity_booking_details)

        val savedDraft = BookingDraftStore.current

        if (savedDraft == null) {
            showError(getString(R.string.error_booking_unavailable))
            finish()
            return
        }

        draft = savedDraft

        bindViews()
        setupLicenseClassOptions()
        restoreDraft()
        setupLicenseExpiryPicker()
        setupButtons()
    }

    // Connects screen fields to their layout views.
    private fun bindViews() {
        idCardInput = findViewById(R.id.etIdCard)
        licenseNumberInput = findViewById(R.id.etLicenseNumber)
        licenseExpiryInput = findViewById(R.id.etLicenseExpiry)
        cityInput = findViewById(R.id.etCity)
        districtInput = findViewById(R.id.etDistrict)
        streetAddressInput = findViewById(R.id.etStreetAddress)
        additionalDetailsInput = findViewById(R.id.etAdditionalDetails)
        licenseClassSpinner = findViewById(R.id.spLicenseClass)
    }

    // Fills the license dropdown with supported choices.
    private fun setupLicenseClassOptions() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf(
                getString(R.string.license_class_select),
                getString(R.string.license_class_b_auto),
                getString(R.string.license_class_b_manual),
                "C",
                "C1"
            )
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        licenseClassSpinner.adapter = adapter
    }

    // Restores previously entered values from the booking draft.
    private fun restoreDraft() {
        idCardInput.setText(draft.idCard)
        licenseNumberInput.setText(draft.licenseNumber)
        licenseExpiryInput.setText(draft.licenseExpiry)
        cityInput.setText(draft.city)
        districtInput.setText(draft.district)
        streetAddressInput.setText(draft.streetAddress)
        additionalDetailsInput.setText(draft.additionalDetails)

        val savedSelection = licenseClasses.indexOf(draft.licenseClass)
        if (savedSelection >= 0) {
            licenseClassSpinner.setSelection(savedSelection)
        } else {
            licenseClassSpinner.setSelection(0)
        }
    }

    // Prevents keyboard input and opens the date picker when this field is tapped.
    private fun setupLicenseExpiryPicker() {
        licenseExpiryInput.showSoftInputOnFocus = false

        licenseExpiryInput.setOnClickListener {
            showLicenseExpiryDatePicker()
        }
    }

    // Opens a calendar to select the license expiry date.
    private fun showLicenseExpiryDatePicker() {
        val minimumDate = today()
        val rentalEndDate = parseDate(draft.dropOffDate)
        if (rentalEndDate != null) {
            val rentalEndCalendar = Calendar.getInstance().apply {
                time = rentalEndDate
                clearTime(this)
            }
            if (rentalEndCalendar.after(minimumDate)) {
                minimumDate.timeInMillis = rentalEndCalendar.timeInMillis
            }
        }

        val existingDate = parseDate(
            licenseExpiryInput.text.toString().trim()
        )

        val initialDate = Calendar.getInstance().apply {
            time = existingDate ?: minimumDate.time
            clearTime(this)

            if (before(minimumDate)) {
                timeInMillis = minimumDate.timeInMillis
            }
        }

        val picker = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                licenseExpiryInput.setText(
                    dateFormat.format(selectedDate.time)
                )
            },
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        )

        picker.datePicker.minDate = minimumDate.timeInMillis
        picker.show()
    }

    // Connects the screen buttons to their actions.
    private fun setupButtons() {
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            trimAllInputs()

            if (validateForm()) {
                saveDraft()
                openSummary()
            }
        }
    }

    // Trims leading and trailing spaces from the input fields.
    private fun trimAllInputs() {
        idCardInput.setText(idCardInput.text.toString().trim())
        licenseNumberInput.setText(licenseNumberInput.text.toString().trim())
        licenseExpiryInput.setText(licenseExpiryInput.text.toString().trim())
        cityInput.setText(cityInput.text.toString().trim())
        districtInput.setText(districtInput.text.toString().trim())
        streetAddressInput.setText(streetAddressInput.text.toString().trim())

        // Additional details are optional, but trim them when present.
        additionalDetailsInput.setText(
            additionalDetailsInput.text.toString().trim()
        )
    }

    // ===== FORM VALIDATION =====
    // Validates required fields before allowing the user to continue.
    private fun validateForm(): Boolean {
        if (!validateIdCard()) return false
        if (!validateLicenseNumber()) return false
        if (!validateLicenseClass()) return false
        if (!validateLicenseExpiry()) return false
        if (!validateCity()) return false
        if (!validateDistrict()) return false
        if (!validateStreetAddress()) return false

        // additionalDetailsInput is optional.

        return true
    }

    // Checks that the ID card contains exactly twelve digits.
    private fun validateIdCard(): Boolean {
        val value = idCardInput.text.toString()

        if (value.isEmpty()) {
            showError(getString(R.string.error_id_required))
            return false
        }

        if (!value.matches(Regex("^[0-9]{12}$"))) {
            showError(getString(R.string.error_id_format))
            return false
        }

        return true
    }

    // Checks that the license number contains exactly twelve digits.
    private fun validateLicenseNumber(): Boolean {
        val value = licenseNumberInput.text.toString()

        if (value.isEmpty()) {
            showError(getString(R.string.error_license_required))
            return false
        }

        if (!value.matches(Regex("^[0-9]{12}$"))) {
            showError(getString(R.string.error_license_format))
            return false
        }

        return true
    }

    // Checks that a driving-license class was selected.
    private fun validateLicenseClass(): Boolean {
        if (licenseClassSpinner.selectedItemPosition == 0) {
            showError(getString(R.string.error_license_class))
            return false
        }

        return true
    }

    // Checks that the license remains valid through the rental.
    private fun validateLicenseExpiry(): Boolean {
        val expiryDate = parseDate(
            licenseExpiryInput.text.toString()
        )

        if (expiryDate == null) {
            showError(getString(R.string.error_license_expiry))
            return false
        }

        if (expiryDate.before(today().time)) {
            showError(getString(R.string.error_license_expired))
            return false
        }

        val dropOffDate = parseDate(draft.dropOffDate)

        if (dropOffDate != null && expiryDate.before(dropOffDate)) {
            showError(
                getString(R.string.error_license_dropoff)
            )
            return false
        }

        return true
    }

    // Checks the city name format and length.
    private fun validateCity(): Boolean {
        val value = cityInput.text.toString()

        if (value.isEmpty()) {
            showError(getString(R.string.error_city_required))
            return false
        }

        if (value.length > 20) {
            showError(getString(R.string.error_city_length))
            return false
        }

        if (!containsLettersAndAllowedSeparators(value)) {
            showError(getString(R.string.error_city_format))
            return false
        }

        return true
    }

    // Checks the district name format and length.
    private fun validateDistrict(): Boolean {
        val value = districtInput.text.toString()

        if (value.isEmpty()) {
            showError(getString(R.string.error_district_required))
            return false
        }

        if (value.length > 20) {
            showError(getString(R.string.error_district_length))
            return false
        }

        if (!containsLettersAndAllowedSeparators(value)) {
            showError(getString(R.string.error_district_format))
            return false
        }

        return true
    }

    // Checks that a location name contains only allowed characters.
    private fun containsLettersAndAllowedSeparators(value: String): Boolean {
        return value.all { character ->
            character.isLetter() ||
                    character.isWhitespace() ||
                    character == '-' ||
                    character == '\''
        }
    }

    // Checks that the street address is present and within its limit.
    private fun validateStreetAddress(): Boolean {
        val value = streetAddressInput.text.toString()

        if (value.isEmpty()) {
            showError(getString(R.string.error_street_required))
            return false
        }

        if (value.length > 30) {
            showError(getString(R.string.error_street_length))
            return false
        }

        return true
    }

    // ===== SAVE AND CONTINUE =====
    // Copies validated form values into the booking draft.
    private fun saveDraft() {
        draft.idCard = idCardInput.text.toString()
        draft.licenseNumber = licenseNumberInput.text.toString()
        draft.licenseClass = licenseClasses[licenseClassSpinner.selectedItemPosition]
        draft.licenseExpiry = licenseExpiryInput.text.toString()
        draft.city = cityInput.text.toString()
        draft.district = districtInput.text.toString()
        draft.streetAddress = streetAddressInput.text.toString()
        draft.additionalDetails =
            additionalDetailsInput.text.toString()
    }

    // Opens the final booking summary screen.
    private fun openSummary() {
        startActivity(
            Intent(this, BookingSummaryActivity::class.java)
        )
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

        val width = (resources.displayMetrics.widthPixels * 0.88).toInt()

        dialog.window?.setLayout(
            width,
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
        if (!AppSettingsRepository.isSoundEnabled(this)) return

        MediaPlayer.create(this, R.raw.rental_failure)?.apply {
            setOnCompletionListener { player ->
                player.release()
            }
            start()
        }
    }
}
