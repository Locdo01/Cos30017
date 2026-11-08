package com.example.carrental

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.carrental.common.BottomNavigationHelper
import com.example.carrental.common.BottomTab
import com.example.carrental.common.BaseCarRentalActivity
import com.example.carrental.common.CarUiText
import com.example.carrental.data.model.Car
import com.example.carrental.data.repository.AppSettingsRepository
import com.example.carrental.data.repository.CarRepository
import com.example.carrental.data.repository.RentalRepository
import com.example.carrental.data.repository.WalletRepository
import com.example.carrental.feature.cars.CarActivity
import com.example.carrental.feature.cars.CarCatalog
import com.example.carrental.feature.cars.CarFilterController
import com.example.carrental.feature.cars.FavoriteActivity
import com.example.carrental.feature.details.CarDetailsActivity
import com.example.carrental.feature.rentals.RentalsActivity
import com.example.carrental.feature.settings.SettingsActivity
import java.util.Locale

// QUICK FIND: Home screen, featured car, previous/next, favorite, navigation.
// Shows one filtered car at a time and opens its details or another screen.


class MainActivity : BaseCarRentalActivity() {

    private lateinit var filterController: CarFilterController

    private var visibleCars: List<Car> = emptyList()
    private var currentCarIndex = 0
    private var restoreCarPosition = false

    // ===== SCREEN SETUP =====
    // Initializes the screen and connects its controls.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentCarIndex = savedInstanceState?.getInt(STATE_CAR_INDEX) ?: 0
        restoreCarPosition = savedInstanceState != null
        setContentView(R.layout.activity_home)
        updateBalanceText()
        setupThemeToggle()
        setupBottomNavigation()
        setupSeeAllButton()
        setupPreviousAndNextButtons()

        filterController = CarFilterController(
            activity = this,
            favoritesOnly = false,
            onResultsChanged = { cars ->
                visibleCars = cars
                currentCarIndex = if (restoreCarPosition) {
                    currentCarIndex.coerceIn(0, (cars.size - 1).coerceAtLeast(0))
                } else {
                    0
                }
                restoreCarPosition = false
                showCurrentCar()
            }
        )

        filterController.start()
    }

    // Keeps the selected car when Android recreates this screen.
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_CAR_INDEX, currentCarIndex)
        super.onSaveInstanceState(outState)
    }

    // ===== CAR DISPLAY =====
    // Show the selected car, or the not-found view when there are no matches.
    // Displays the current car or the no-results message.
    private fun showCurrentCar() {
        updateHomeRentalSummary()
        val card = findViewById<View>(R.id.carCard)
        val notFound = findViewById<View>(R.id.notFoundContainer)
        val navigation = findViewById<View>(R.id.carNavigation)
        val total = findViewById<TextView>(R.id.tvTotalCar)

        total.text = getString(R.string.cars_total, visibleCars.size)

        if (visibleCars.isEmpty()) {
            card.visibility = View.GONE
            navigation.visibility = View.GONE
            notFound.visibility = View.VISIBLE
            return
        }

        card.visibility = View.VISIBLE
        navigation.visibility = View.VISIBLE
        notFound.visibility = View.GONE

        val car = visibleCars[currentCarIndex]
        findViewById<View>(R.id.btnRentNow).setOnClickListener {
            openCarDetails(car)
        }

        findViewById<TextView>(R.id.tvBodyType).text = CarUiText.bodyType(this, car.bodyType)
        findViewById<TextView>(R.id.tvCarName).text = car.name
        findViewById<ImageView>(R.id.ivCarImage).apply {
            setImageResource(car.imageCar)
            contentDescription = car.name
        }
        findViewById<TextView>(R.id.tvCarMileage).text =
            getString(R.string.car_mileage, car.kilometres)
        findViewById<TextView>(R.id.tvDailyCost).text =
            getString(R.string.car_daily_cost, car.dailyCost)
        findViewById<TextView>(R.id.tvCarRating).text =
            String.format(Locale.getDefault(), "%.1f", car.rating)
        findViewById<android.widget.RatingBar>(R.id.rbCarRating).rating = car.rating
        findViewById<TextView>(R.id.tvTransmission).text = CarUiText.transmission(this, car.transmission)
        findViewById<TextView>(R.id.tvFuelType).text = CarUiText.fuel(this, car.fuelType)
        findViewById<TextView>(R.id.tvSeats).text = CarUiText.seatCount(this, car.seats)
        findViewById<TextView>(R.id.tvYear).text = car.year.toString()

        val favoriteButton = findViewById<ImageButton>(R.id.btnFavorite)
        updateFavoriteIcon(favoriteButton, car.id)

        favoriteButton.setOnClickListener {
            CarCatalog.toggleFavorite(car.id)
            updateFavoriteIcon(favoriteButton, car.id)
        }

        updateNavigationButtons()
    }

    // Shows the latest rental on Home and links the message to the Rentals page.
    private fun updateHomeRentalSummary() {
        val summary = findViewById<TextView>(R.id.tvHomeRentalSummary)
        val rental = RentalRepository.getAllRentals().firstOrNull()
        val car = rental?.let { saved ->
            CarRepository.cars.firstOrNull { it.id == saved.carId }
        }

        if (rental == null || car == null) {
            summary.visibility = View.GONE
            summary.setOnClickListener(null)
            return
        }

        summary.text = getString(
            R.string.home_rental_summary,
            car.name,
            rental.pickUpDate,
            rental.dropOffDate,
            rental.totalCost
        )
        summary.visibility = View.VISIBLE
        summary.setOnClickListener { openPage(RentalsActivity::class.java) }
    }

    // ===== CAR NAVIGATION =====
    // Enable or fade the arrows according to the current position.
    // Enables or fades arrows based on the current car position.
    private fun updateNavigationButtons() {
        val previous = findViewById<ImageButton>(R.id.btnPrevious)
        val next = findViewById<ImageButton>(R.id.btnNext)

        previous.isEnabled = currentCarIndex > 0
        next.isEnabled = currentCarIndex < visibleCars.lastIndex

        previous.alpha = if (previous.isEnabled) 1f else 0.35f
        next.alpha = if (next.isEnabled) 1f else 0.35f
    }

    // Connects arrows to move between visible cars.
    private fun setupPreviousAndNextButtons() {
        findViewById<ImageButton>(R.id.btnPrevious).setOnClickListener {
            if (currentCarIndex > 0) {
                currentCarIndex--
                showCurrentCar()
            }
        }

        findViewById<ImageButton>(R.id.btnNext).setOnClickListener {
            if (currentCarIndex < visibleCars.lastIndex) {
                currentCarIndex++
                showCurrentCar()
            }
        }
    }

    // ===== FAVORITES =====
    // Updates the favorite icon and accessibility description.
    private fun updateFavoriteIcon(button: ImageButton, carId: Int) {
        val isFavorite = CarCatalog.isFavorite(carId)
        val color = if (isFavorite) Color.RED else getColor(R.color.text_secondary)

        button.imageTintList = ColorStateList.valueOf(color)
        button.contentDescription =
            if (isFavorite) getString(R.string.a11y_remove_favorite)
            else getString(R.string.a11y_add_favorite)
    }

    // ===== PAGE NAVIGATION =====
    // Connects See All to the full car list.
    private fun setupSeeAllButton() {
        findViewById<TextView>(R.id.tvSeeAll).setOnClickListener {
            openPage(CarActivity::class.java)
        }
    }

    // Connects the bottom navigation items for this screen.
    private fun setupBottomNavigation() {
        BottomNavigationHelper.setup(
            activity = this,
            selectedTab = BottomTab.HOME,
            onHomeClick = {
                // Home is already open.
            },
            onCarsClick = {
                openPage(CarActivity::class.java)
            },
            onFavoriteClick = {
                openPage(FavoriteActivity::class.java)
            },
            onRentalsClick = {
                openPage(RentalsActivity::class.java)
            },
            onSettingsClick = {
                openPage(SettingsActivity::class.java)
            }
        )
    }

    // Opens the requested screen.
    private fun openPage(destination: Class<out AppCompatActivity>) {
        startActivity(Intent(this, destination))
    }

    // Opens the details screen for the selected car.
    private fun openCarDetails(car: Car) {
        val intent = Intent(this, CarDetailsActivity::class.java)
            .putExtra(CarDetailsActivity.EXTRA_CAR, car)
            .putExtra(CarDetailsActivity.EXTRA_CAR_ID, car.id)

        startActivity(intent)
    }

    // Displays the current wallet balance.
    private fun updateBalanceText() {
        findViewById<TextView>(R.id.tvBalance).text =
            getString(R.string.wallet_balance, WalletRepository.getBalance())
    }

    // Connects the top-of-screen dark-mode switch to the saved app setting.
    private fun setupThemeToggle() {
        val toggle = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchDarkModeTop)
        toggle.isChecked = AppSettingsRepository.isDarkMode(this)
        toggle.setOnCheckedChangeListener { _, enabled ->
            if (enabled != AppSettingsRepository.isDarkMode(this)) {
                AppSettingsRepository.setDarkMode(this, enabled)
                recreateWithoutTransition()
            }
        }
    }

    companion object {
        private const val STATE_CAR_INDEX = "state_car_index"
    }

    // Refreshes screen content when this activity becomes visible.
    override fun onResume() {
        super.onResume()

        // Refresh the heart icon after returning from another page.
        if (::filterController.isInitialized) {
            filterController.refresh()
            updateBalanceText()
        }
    }
}
