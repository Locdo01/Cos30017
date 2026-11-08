package com.example.carrental.feature.cars



// QUICK FIND: Favorites screen.
// Uses the shared car list screen and shows only favorited cars.

class FavoriteActivity : BaseCarListActivity() {

    // The Favorite page shows only cars marked as favorites.
    override val favoritesOnly: Boolean = true
}
