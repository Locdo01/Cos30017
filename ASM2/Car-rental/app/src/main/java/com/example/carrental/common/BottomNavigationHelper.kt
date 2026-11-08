package com.example.carrental.common

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.carrental.feature.cars.CarActivity
import com.example.carrental.feature.cars.FavoriteActivity
import com.example.carrental.feature.rentals.RentalsActivity
import com.example.carrental.feature.settings.SettingsActivity
import com.example.carrental.MainActivity
import com.example.carrental.R

// QUICK FIND: bottom navigation, selected tab, click handlers, selected colors.
// Keeps navigation setup consistent across the app's main screens.


enum class BottomTab {
    HOME,
    CARS,
    FAVORITE,
    RENTALS,
    SETTINGS
}

object BottomNavigationHelper {

    // Connects the navigation items and selects the current tab.
    fun setup(
        activity: MainActivity,
        selectedTab: BottomTab,
        onHomeClick: () -> Unit,
        onCarsClick: () -> Unit,
        onFavoriteClick: () -> Unit,
        onRentalsClick: () -> Unit = {},
        onSettingsClick: () -> Unit = {}
    ) {
        setSelected(activity.findViewById(R.id.navHome), selectedTab == BottomTab.HOME)
        setSelected(activity.findViewById(R.id.navCars), selectedTab == BottomTab.CARS)
        setSelected(activity.findViewById(R.id.navFavorite), selectedTab == BottomTab.FAVORITE)
        setSelected(activity.findViewById(R.id.navRentals), selectedTab == BottomTab.RENTALS)
        setSelected(activity.findViewById(R.id.navSettings), selectedTab == BottomTab.SETTINGS)

        activity.findViewById<View>(R.id.navHome).setOnClickListener {
            onHomeClick()
        }

        activity.findViewById<View>(R.id.navCars).setOnClickListener {
            onCarsClick()
        }

        activity.findViewById<View>(R.id.navFavorite).setOnClickListener {
            onFavoriteClick()
        }

        activity.findViewById<View>(R.id.navRentals).setOnClickListener {
            onRentalsClick()
        }

        activity.findViewById<View>(R.id.navSettings).setOnClickListener {
            onSettingsClick()
        }
    }

    // Connects the navigation items and selects the current tab.
    fun setup(
        activity: androidx.appcompat.app.AppCompatActivity,
        selectedTab: BottomTab,
        onHomeClick: () -> Unit,
        onCarsClick: () -> Unit,
        onFavoriteClick: () -> Unit,
        onRentalsClick: () -> Unit = {},
        onSettingsClick: () -> Unit = {}
    ) {
        setSelected(activity.findViewById(R.id.navHome), selectedTab == BottomTab.HOME)
        setSelected(activity.findViewById(R.id.navCars), selectedTab == BottomTab.CARS)
        setSelected(activity.findViewById(R.id.navFavorite), selectedTab == BottomTab.FAVORITE)
        setSelected(activity.findViewById(R.id.navRentals), selectedTab == BottomTab.RENTALS)
        setSelected(activity.findViewById(R.id.navSettings), selectedTab == BottomTab.SETTINGS)

        activity.findViewById<View>(R.id.navHome).setOnClickListener {
            onHomeClick()
        }

        activity.findViewById<View>(R.id.navCars).setOnClickListener {
            onCarsClick()
        }

        activity.findViewById<View>(R.id.navFavorite).setOnClickListener {
            onFavoriteClick()
        }

        activity.findViewById<View>(R.id.navRentals).setOnClickListener {
            onRentalsClick()
        }

        activity.findViewById<View>(R.id.navSettings).setOnClickListener {
            onSettingsClick()
        }
    }

    // Updates the colors for the selected navigation item.
    private fun setSelected(item: View, selected: Boolean) {
        val container = item as? ViewGroup ?: return
        if (container.childCount < 2) return

        val icon = container.getChildAt(0) as? ImageView ?: return
        val label = container.getChildAt(1) as? TextView ?: return

        val colorResource = if (selected) {
            R.color.nav_selected
        } else {
            R.color.nav_unselected
        }
        val color = ContextCompat.getColor(item.context, colorResource)

        icon.imageTintList = ColorStateList.valueOf(color)
        label.setTextColor(color)
        item.isSelected = selected
    }
}
