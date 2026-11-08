package com.example.carrental.feature.rentals

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.example.carrental.common.BaseCarRentalActivity
import com.example.carrental.common.BottomNavigationHelper
import com.example.carrental.common.BottomTab
import com.example.carrental.common.CarUiText
import com.example.carrental.data.model.Car
import com.example.carrental.data.model.Rental
import com.example.carrental.data.repository.CarRepository
import com.example.carrental.data.repository.RentalRepository
import com.example.carrental.data.repository.WalletRepository
import com.example.carrental.feature.booking.BookingSummaryActivity
import com.example.carrental.feature.cars.CarActivity
import com.example.carrental.feature.cars.FavoriteActivity
import com.example.carrental.feature.settings.SettingsActivity
import com.example.carrental.MainActivity
import com.example.carrental.R
import java.text.NumberFormat
import java.util.Locale

// QUICK FIND: rentals list, rental card, view summary, cancel, refund.
// Displays active rentals and lets the user review or cancel one.


class RentalsActivity : BaseCarRentalActivity() {

    // Initializes the screen and connects its controls.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rentals)
        setupBottomNavigation()
    }

    // Refreshes screen content when this activity becomes visible.
    override fun onResume() {
        super.onResume()
        showRentals()
    }

    // Displays active rental cards or the empty-rentals message.
    private fun showRentals() {
        val rentals = RentalRepository.getAllRentals()
        val list = findViewById<LinearLayout>(R.id.rentalListContainer)
        val scroll = list.parent as View
        val empty = findViewById<View>(R.id.emptyRentalsContainer)

        findViewById<TextView>(R.id.tvRentalCount).text =
            getString(R.string.rentals_count, rentals.size)
        list.removeAllViews()

        if (rentals.isEmpty()) {
            scroll.visibility = View.GONE
            empty.visibility = View.VISIBLE
            return
        }

        scroll.visibility = View.VISIBLE
        empty.visibility = View.GONE

        rentals.forEach { rental ->
            val car = CarRepository.cars.firstOrNull { it.id == rental.carId } ?: return@forEach
            val card = layoutInflater.inflate(R.layout.item_rental, list, false)

            card.findViewById<android.widget.ImageView>(R.id.ivRentalCar).apply {
                setImageResource(car.imageCar)
                contentDescription = car.name
            }
            card.findViewById<TextView>(R.id.tvRentalCarName).text = car.name
            card.findViewById<TextView>(R.id.tvRentalCarBody).text =
                CarUiText.bodyType(this, car.bodyType)
            card.findViewById<TextView>(R.id.tvRentalDates).text =
                getString(R.string.rentals_pick_up, rental.pickUpDate) +
                        "   •   " + getString(R.string.rentals_drop_off, rental.dropOffDate)
            card.findViewById<TextView>(R.id.tvRentalPrice).text =
                getString(R.string.rentals_total, formatCredits(rental.totalCost))

            card.findViewById<View>(R.id.btnViewRental).setOnClickListener {
                startActivity(
                    Intent(this, BookingSummaryActivity::class.java)
                        .putExtra(BookingSummaryActivity.EXTRA_RENTAL_CAR_ID, rental.carId)
                )
            }

            card.findViewById<View>(R.id.btnCancelRental).setOnClickListener {
                confirmCancelRental(rental, car.name)
            }

            list.addView(card)
        }
    }

    // Asks the user to confirm before cancelling a rental.
    private fun confirmCancelRental(rental: Rental, carName: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.cancel_rental_question))
            .setMessage(getString(R.string.rental_confirm_cancel_message, carName))
            .setNegativeButton(getString(R.string.rental_keep), null)
            .setPositiveButton(getString(R.string.rental_cancel_action)) { _, _ ->
                val cancelled = RentalRepository.cancel(rental.carId)
                if (cancelled) {
                    WalletRepository.refund(rental.totalCost)
                    Toast.makeText(
                        this,
                        getString(
                            R.string.rental_cancelled_refund,
                            formatCredits(rental.totalCost)
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_cancel_booking),
                        Toast.LENGTH_LONG
                    ).show()
                }
                showRentals()
            }
            .show()
    }

    // Connects the bottom navigation items for this screen.
    private fun setupBottomNavigation() {
        BottomNavigationHelper.setup(
            activity = this,
            selectedTab = BottomTab.RENTALS,
            onHomeClick = { openPage(MainActivity::class.java) },
            onCarsClick = { openPage(CarActivity::class.java) },
            onFavoriteClick = { openPage(FavoriteActivity::class.java) },
            onRentalsClick = { /* Rentals is already open. */ },
            onSettingsClick = { openPage(SettingsActivity::class.java) }
        )
    }

    // Opens the requested screen.
    private fun openPage(destination: Class<out AppCompatActivity>) {
        startActivity(Intent(this, destination))
        finish()
    }

    // Formats a credit amount for display.
    private fun formatCredits(amount: Int): String =
        NumberFormat.getIntegerInstance(Locale.getDefault()).format(amount)
}
