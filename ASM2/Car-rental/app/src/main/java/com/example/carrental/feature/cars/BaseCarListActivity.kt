package com.example.carrental.feature.cars

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.carrental.common.BaseCarRentalActivity
import com.example.carrental.common.CarUiText
import com.example.carrental.common.BottomNavigationHelper
import com.example.carrental.common.BottomTab
import com.example.carrental.data.model.Car
import com.example.carrental.data.repository.AppSettingsRepository
import com.example.carrental.data.repository.CarRepository
import com.example.carrental.feature.details.CarDetailsActivity
import com.example.carrental.feature.rentals.RentalsActivity
import com.example.carrental.feature.settings.SettingsActivity
import com.example.carrental.MainActivity
import com.example.carrental.R

// QUICK FIND: shared car list, favorite list, card binding, bottom navigation.
// Provides the common list behavior used by the Cars and Favorites screens.


abstract class BaseCarListActivity : BaseCarRentalActivity() {

    // Subclasses decide whether to show all cars or only favorites.
    protected abstract val favoritesOnly: Boolean

    private lateinit var filterController: CarFilterController

    // ===== SCREEN SETUP =====
    // Initializes the screen and connects its controls.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car)

        findViewById<TextView>(R.id.tvListTitle).text = getString(
            if (favoritesOnly) R.string.favorite_list_title else R.string.all_cars_title
        )
        findViewById<View>(R.id.btnBrowseCars).setOnClickListener {
            openPage(CarActivity::class.java)
        }

        setupBottomNavigation()
        setupThemeToggle()

        filterController = CarFilterController(
            activity = this,
            favoritesOnly = favoritesOnly,
            onResultsChanged = { cars ->
                showCars(cars)
            }
        )

        filterController.start()
    }

    // Refreshes availability after returning from rental details or cancellation.
    override fun onResume() {
        super.onResume()
        if (::filterController.isInitialized) {
            filterController.refresh()
        }
    }

    // ===== LIST AND CARD DISPLAY =====
    // Rebuild the visible list using the filtered cars.
    // Builds the car list or displays its empty state.
    private fun showCars(cars: List<Car>) {
        val list = findViewById<LinearLayout>(R.id.carListContainer)
        val total = findViewById<TextView>(R.id.tvListTotal)
        val notFound = findViewById<View>(R.id.notFoundContainer)
        val emptyFavorites = findViewById<View>(R.id.emptyFavoritesContainer)

        list.removeAllViews()
        total.text = getString(R.string.cars_total_short, cars.size)

        if (cars.isEmpty()) {
            list.visibility = View.GONE

            if (favoritesOnly) {
                emptyFavorites.visibility = View.VISIBLE
                notFound.visibility = View.GONE
            } else {
                emptyFavorites.visibility = View.GONE
                notFound.visibility = View.VISIBLE
            }
            return
        }

        list.visibility = View.VISIBLE
        emptyFavorites.visibility = View.GONE
        notFound.visibility = View.GONE

        cars.forEach { car ->
            val card = layoutInflater.inflate(
                R.layout.item_car_card,
                list,
                false
            )

            bindCar(card, car)

            card.findViewById<View>(R.id.btnRentNow).setOnClickListener {
                openCarDetails(car)
            }

            list.addView(card)
        }
    }

    // Fill one list card with the selected car's data.
    // Fills a car card with vehicle data and favorite actions.
    private fun bindCar(card: View, car: Car) {
        card.findViewById<android.widget.ImageView>(R.id.ivCarImage).apply {
            setImageResource(car.imageCar)
            contentDescription = car.name
        }

        card.findViewById<TextView>(R.id.tvBodyType).text = CarUiText.bodyType(this, car.bodyType)
        card.findViewById<TextView>(R.id.tvCarName).text = car.name
        card.findViewById<TextView>(R.id.tvCarMileage).text =
            getString(R.string.car_mileage, car.kilometres)
        card.findViewById<TextView>(R.id.tvDailyCost).text =
            getString(R.string.car_daily_cost, car.dailyCost)
        card.findViewById<TextView>(R.id.tvCarRating).text = String.format("%.1f", car.rating)
        card.findViewById<android.widget.RatingBar>(R.id.rbCarRating).rating = car.rating
        card.findViewById<TextView>(R.id.tvTransmission).text =
            CarUiText.transmission(this, car.transmission)
        card.findViewById<TextView>(R.id.tvFuelType).text = CarUiText.fuel(this, car.fuelType)
        card.findViewById<TextView>(R.id.tvSeats).text =
            CarUiText.seatCount(this, car.seats)
        card.findViewById<TextView>(R.id.tvYear).text = car.year.toString()

        val favoriteButton = card.findViewById<android.widget.ImageButton>(R.id.btnFavorite)

        updateFavoriteIcon(favoriteButton, car.id)

        favoriteButton.setOnClickListener {
            CarCatalog.toggleFavorite(car.id)

            if (favoritesOnly) {
                // Remove the car from this list after it is unfavorited.
                filterController.refresh()
            } else {
                updateFavoriteIcon(favoriteButton, car.id)
            }
        }
    }

    // ===== FAVORITES =====
    // Updates the favorite icon and accessibility description.
    private fun updateFavoriteIcon(
        button: android.widget.ImageButton,
        carId: Int
    ) {
        val isFavorite = CarCatalog.isFavorite(carId)
        val color = if (isFavorite) android.graphics.Color.RED
        else getColor(R.color.text_secondary)

        button.imageTintList = android.content.res.ColorStateList.valueOf(color)

        button.contentDescription =
            if (isFavorite) getString(R.string.a11y_remove_favorite)
            else getString(R.string.a11y_add_favorite)
    }

    // ===== NAVIGATION =====
    // Connects the bottom navigation items for this screen.
    private fun setupBottomNavigation() {
        val selectedTab = if (favoritesOnly) BottomTab.FAVORITE else BottomTab.CARS

        BottomNavigationHelper.setup(
            activity = this,
            selectedTab = selectedTab,
            onHomeClick = {
                openPage(MainActivity::class.java)
            },
            onCarsClick = {
                if (favoritesOnly) {
                    openPage(CarActivity::class.java)
                }
            },
            onFavoriteClick = {
                if (!favoritesOnly) {
                    openPage(FavoriteActivity::class.java)
                }
            },
            onRentalsClick = {
                openPage(RentalsActivity::class.java)
            },
            onSettingsClick = {
                openPage(SettingsActivity::class.java)
            }
        )
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

    // Opens the requested screen.
    private fun openPage(destination: Class<out AppCompatActivity>) {
        startActivity(Intent(this, destination))
        finish()
    }

    // Opens the details screen for the selected car.
    private fun openCarDetails(car: Car) {
        val intent = Intent(this, CarDetailsActivity::class.java)
            .putExtra(CarDetailsActivity.EXTRA_CAR, car)
            .putExtra(CarDetailsActivity.EXTRA_CAR_ID, car.id)

        startActivity(intent)
    }
}
