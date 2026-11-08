package com.example.carrental.feature.cars



// QUICK FIND: all cars screen.
// Uses the shared list screen with the Favorites-only filter turned off.

class CarActivity : BaseCarListActivity() {

    // The Cars page shows every car.
    override val favoritesOnly: Boolean = false
}
