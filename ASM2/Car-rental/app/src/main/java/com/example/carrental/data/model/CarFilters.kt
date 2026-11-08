package com.example.carrental.data.model



// Search and sort options shared by Home, Cars, and Favorites.
enum class SortDirection {
    LOW_TO_HIGH,
    HIGH_TO_LOW
}

data class CarFilters(
    val query: String = "",
    val brand: String? = null,
    val ratingSort: SortDirection? = null,
    val costSort: SortDirection? = null,
    val yearSort: SortDirection? = null
)
