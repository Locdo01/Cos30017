package com.example.carrental.feature.cars

import com.example.carrental.data.model.Car
import com.example.carrental.data.model.CarFilters
import com.example.carrental.data.model.SortDirection
import com.example.carrental.data.repository.CarRepository
import com.example.carrental.data.repository.RentalRepository

// Stores shared favorite state and applies car search, brand, and sort rules.
object CarCatalog {

    // ===== FAVORITE STATE =====
    // Shared in-memory favorite state for Home, Cars, and Favorites.
    private val favoriteCarIds = mutableSetOf<Int>()

    // Checks whether the car is currently a favorite.
    fun isFavorite(carId: Int): Boolean {
        return carId in favoriteCarIds
    }

    // Adds the car to favorites or removes it if already saved.
    fun toggleFavorite(carId: Int) {
        if (carId in favoriteCarIds) {
            favoriteCarIds.remove(carId)
        } else {
            favoriteCarIds.add(carId)
        }
    }

    // ===== FILTERING AND SORTING =====
    // Search and sort all cars, or only cars in the Favorites page.
    // Filters the car catalog and applies the selected sort orders.
    fun getCars(
        filters: CarFilters,
        favoritesOnly: Boolean
    ): List<Car> {
        val sourceCars = if (favoritesOnly) {
            CarRepository.cars.filter { it.id in favoriteCarIds }
        } else {
            // Home and Cars show only cars that are currently available to rent.
            CarRepository.cars.filterNot { RentalRepository.isRented(it.id) }
        }

        val matchingCars = sourceCars.filter { car ->
            val matchesQuery = filters.query.isBlank() ||
                        car.name.contains(filters.query, ignoreCase = true) ||
                        car.model.contains(filters.query, ignoreCase = true)

            val matchesBrand = filters.brand == null ||
                        car.brand.equals(filters.brand, ignoreCase = true)

            matchesQuery && matchesBrand
        }

        // Sort in reverse priority order:
        // Rating has the highest priority, then Cost, then Year.
        var result = matchingCars

        result = when (filters.yearSort) {
            SortDirection.LOW_TO_HIGH -> result.sortedBy { it.year }
            SortDirection.HIGH_TO_LOW -> result.sortedByDescending { it.year }
            null -> result
        }

        result = when (filters.costSort) {
            SortDirection.LOW_TO_HIGH -> result.sortedBy { it.dailyCost }
            SortDirection.HIGH_TO_LOW -> result.sortedByDescending { it.dailyCost }
            null -> result
        }

        result = when (filters.ratingSort) {
            SortDirection.LOW_TO_HIGH -> result.sortedBy { it.rating }
            SortDirection.HIGH_TO_LOW -> result.sortedByDescending { it.rating }
            null -> result
        }

        return result
    }
}
