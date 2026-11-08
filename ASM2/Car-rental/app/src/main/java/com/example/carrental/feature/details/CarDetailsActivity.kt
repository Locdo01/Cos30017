package com.example.carrental.feature.details

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.carrental.common.BaseCarRentalActivity
import androidx.core.content.IntentCompat
import com.example.carrental.common.CarUiText
import com.example.carrental.data.local.BookingDraftStore
import com.example.carrental.data.model.Car
import com.example.carrental.data.repository.AppSettingsRepository
import com.example.carrental.data.repository.CarRepository
import com.example.carrental.data.repository.RentalRepository
import com.example.carrental.data.repository.WalletRepository
import com.example.carrental.feature.booking.PersonalInformationActivity
import com.example.carrental.feature.cars.CarCatalog
import com.example.carrental.R
import java.text.NumberFormat
import java.util.Locale

// QUICK FIND: car details, image gallery, favorites, start booking, cancel rental.
// Displays one selected car and connects its gallery and rental actions.


class CarDetailsActivity : BaseCarRentalActivity() {

    private lateinit var car: Car

    // The main image is index 0; additional images follow it.
    private val carImages = mutableListOf<Int>()
    private var currentImageIndex = 0

    // ===== SCREEN SETUP =====
    // Initializes the screen and connects its controls.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_details)

        val passedCar = IntentCompat.getParcelableExtra(intent, EXTRA_CAR, Car::class.java)
        val carId = intent.getIntExtra(EXTRA_CAR_ID, passedCar?.id ?: -1)
        val selectedCar = passedCar
            ?: CarRepository.cars.firstOrNull { it.id == carId }

        if (selectedCar == null) {
            finish()
            return
        }

        car = selectedCar

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        showCarInformation()
        setupImageNavigation()
        setupFavoriteButton()
        setupRentalButtons()
        updateRentalScreen()
    }

    // Refreshes screen content when this activity becomes visible.
    override fun onResume() {
        super.onResume()

        // Refresh the screen after returning from the booking flow.
        if (::car.isInitialized) {
            updateRentalScreen()
        }
    }

    // ===== CAR INFORMATION AND IMAGE GALLERY =====
    // Displays the selected car specifications.
    private fun showCarInformation() {
        findViewById<TextView>(R.id.tvDetailsCarName).text = car.name
        findViewById<TextView>(R.id.tvModelBadge).text = car.model
        findViewById<TextView>(R.id.tvDetailsBodyType).text =
            CarUiText.bodyType(this, car.bodyType)

        findViewById<TextView>(R.id.tvDetailsRating).text =
            String.format("%.1f", car.rating)
        findViewById<android.widget.RatingBar>(R.id.rbDetailsRating).rating = car.rating

        findViewById<TextView>(R.id.tvDetailsYear).text =
            car.year.toString()

        findViewById<TextView>(R.id.tvDetailsTransmission).text =
            CarUiText.transmission(this, car.transmission)

        findViewById<TextView>(R.id.tvDetailsFuel).text =
            CarUiText.fuel(this, car.fuelType)

        findViewById<TextView>(R.id.tvDetailsSeats).text =
            CarUiText.seatCount(this, car.seats)

        findViewById<TextView>(R.id.tvDetailsMileage).text =
            getString(R.string.car_mileage, car.kilometres)

        findViewById<TextView>(R.id.tvDetailsConsumption).text =
            car.consumptionText

        findViewById<TextView>(R.id.tvDetailsChargingFee).text =
            car.fuelPriceText

        showCarImages()
    }

    // Builds the image gallery for the selected car.
    private fun showCarImages() {
        carImages.clear()
        carImages.add(car.imageCar)
        carImages.addAll(car.detailImagesCar)

        currentImageIndex = 0
        showSelectedImage()

        val imageContainer =
            findViewById<LinearLayout>(R.id.detailImagesContainer)

        imageContainer.removeAllViews()

        car.detailImagesCar.forEachIndexed { index, imageResource ->
            val thumbnail = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(98),
                    dpToPx(74)
                ).apply {
                    marginEnd = dpToPx(8)
                }

                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageResource(imageResource)
                contentDescription = "${car.name} image ${index + 2}"

                setOnClickListener {
                    // The main car image is at index 0.
                    currentImageIndex = index + 1
                    showSelectedImage()
                }
            }

            imageContainer.addView(thumbnail)
        }
    }

    // Displays the selected gallery image and updates its counter.
    private fun showSelectedImage() {
        if (carImages.isEmpty()) return

        // Keep the current index within the available image range.
        currentImageIndex = currentImageIndex.coerceIn(0, carImages.lastIndex)

        findViewById<ImageView>(R.id.ivMainCarImage).apply {
            setImageResource(carImages[currentImageIndex])
            contentDescription = "${car.name} image ${currentImageIndex + 1}"
        }

        findViewById<TextView>(R.id.tvImageCounter).text =
            "${currentImageIndex + 1}/${carImages.size}"
    }

    // Connects the gallery arrows to image navigation.
    private fun setupImageNavigation() {
        findViewById<ImageButton>(R.id.btnImagePrevious).setOnClickListener {
            showPreviousImage()
        }

        findViewById<ImageButton>(R.id.btnImageNext).setOnClickListener {
            showNextImage()
        }
    }

    // Moves to the next image and wraps to the first at the end.
    private fun showNextImage() {
        if (carImages.isEmpty()) return

        currentImageIndex = (currentImageIndex + 1) % carImages.size
        showSelectedImage()
    }

    // Moves to the previous image and wraps to the last at the start.
    private fun showPreviousImage() {
        if (carImages.isEmpty()) return

        currentImageIndex =
            (currentImageIndex - 1 + carImages.size) % carImages.size

        showSelectedImage()
    }

    // ===== FAVORITES =====
    // Connects the favorite button to the shared favorite state.
    private fun setupFavoriteButton() {
        val favoriteButton =
            findViewById<ImageButton>(R.id.btnDetailsFavorite)

        updateFavoriteButton(favoriteButton)

        favoriteButton.setOnClickListener {
            CarCatalog.toggleFavorite(car.id)
            updateFavoriteButton(favoriteButton)
        }
    }

    // Updates the favorite icon and accessibility description.
    private fun updateFavoriteButton(button: ImageButton) {
        val favorite = CarCatalog.isFavorite(car.id)

        button.imageTintList = ColorStateList.valueOf(
            if (favorite) Color.RED else getColor(R.color.text_primary)
        )

        button.contentDescription =
            if (favorite) getString(R.string.a11y_remove_favorite)
            else getString(R.string.a11y_add_favorite)
    }

    // ===== RENTAL ACTIONS =====
    // Connects rent and cancel actions for this car.
    private fun setupRentalButtons() {
        findViewById<Button>(R.id.btnRentNow).setOnClickListener {
            if (RentalRepository.isRented(car.id)) {
                showResult(
                    success = false,
                    message = getString(R.string.error_car_already_rented)
                )
                return@setOnClickListener
            }

            // Start a fresh booking draft for the selected car.
            BookingDraftStore.start(car.id)

            startActivity(
                Intent(this, PersonalInformationActivity::class.java)
                    .putExtra(EXTRA_CAR, car)
                    .putExtra(EXTRA_CAR_ID, car.id)
            )
        }

        findViewById<Button>(R.id.btnCancelRental).setOnClickListener {
            confirmCancelRental()
        }

        // Editing dates was moved out of Car Details into the booking flow.
        findViewById<Button>(R.id.btnSaveRental).visibility = View.GONE
    }

    // Shows the correct actions for the current rental state.
    private fun updateRentalScreen() {
        val isRented = RentalRepository.isRented(car.id)

        findViewById<TextView>(R.id.tvPageTitle).text = getString(
            if (isRented) R.string.car_page_rented else R.string.car_details_title
        )

        findViewById<Button>(R.id.btnRentNow).visibility =
            if (isRented) View.GONE else View.VISIBLE

        findViewById<View>(R.id.rentedActions).visibility =
            if (isRented) View.VISIBLE else View.GONE

        findViewById<Button>(R.id.btnSaveRental).visibility = View.GONE
    }

    // Asks the user to confirm before cancelling a rental.
    private fun confirmCancelRental() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.cancel_rental_question))
            .setMessage(getString(R.string.cancel_rental_prompt))
            .setNegativeButton(getString(R.string.rental_keep), null)
            .setPositiveButton(getString(R.string.rental_cancel_action)) { _, _ ->
                cancelRentalAndRefund()
            }
            .show()
    }

    // Cancels the rental, refunds its cost, and displays the result.
    private fun cancelRentalAndRefund() {
        val savedRental = RentalRepository.getRental(car.id)

        if (savedRental == null) {
            showResult(
                success = false,
                message = getString(R.string.rental_missing)
            )
            return
        }

        val cancelled = RentalRepository.cancel(car.id)

        if (!cancelled) {
            showResult(
                success = false,
                message = getString(R.string.error_cancel_booking)
            )
            return
        }

        // Return the credits charged for this rental.
        WalletRepository.refund(savedRental.totalCost)

        updateRentalScreen()

        showResult(
            success = true,
            message = getString(
                R.string.rental_cancelled_refund,
                formatCredits(savedRental.totalCost)
            )
        )
    }

    // ===== RESULT MESSAGE AND SOUND =====
    // Displays a result dialog and plays the matching sound.
    private fun showResult(success: Boolean, message: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_rental_result)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        dialog.findViewById<TextView>(R.id.tvResultTitle).apply {
            text = if (success) getString(R.string.dialog_success) else getString(R.string.error_title)
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

        val closeDialog = View.OnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.btnClose).setOnClickListener(closeDialog)
        dialog.findViewById<Button>(R.id.btnDismiss).setOnClickListener(closeDialog)

        // Show a brief outcome message while keeping the detailed result dialog.
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        val soundId = if (success) {
            R.raw.rental_success
        } else {
            R.raw.rental_failure
        }

        playSound(soundId)
    }

    // Plays a sound when sound effects are enabled.
    private fun playSound(soundId: Int) {
        if (!AppSettingsRepository.isSoundEnabled(this)) return

        val player = MediaPlayer.create(this, soundId) ?: return

        player.setOnCompletionListener { finishedPlayer ->
            finishedPlayer.release()
        }

        player.start()
    }

    // Converts a dp value to screen pixels.
    private fun dpToPx(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    // Formats a credit amount for display.
    private fun formatCredits(amount: Int): String =
        NumberFormat.getIntegerInstance(Locale.getDefault()).format(amount)

    companion object {
        const val EXTRA_CAR_ID = "extra_car_id"
        const val EXTRA_CAR = "extra_car"
    }
}
