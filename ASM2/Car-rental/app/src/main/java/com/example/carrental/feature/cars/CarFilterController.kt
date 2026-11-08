package com.example.carrental.feature.cars

import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.carrental.data.model.Car
import com.example.carrental.data.model.CarFilters
import com.example.carrental.data.model.SortDirection
import com.example.carrental.R

// Connects the shared search, brand, and sort controls to the car catalog.


class CarFilterController(
    private val activity: AppCompatActivity,
    private val favoritesOnly: Boolean,
    private val onResultsChanged: (List<Car>) -> Unit
) {
    // ===== FILTER CONTROL SETUP =====
    private val stateKey = "${activity::class.java.name}:$favoritesOnly"
    private var filters = savedFilters[stateKey] ?: CarFilters()

    companion object {
        // Keeps each screen's search and sort choices through a light/dark refresh.
        private val savedFilters = mutableMapOf<String, CarFilters>()
    }

    // Sets up search, brand, and sort controls for this screen.
    fun start() {
        setupSearch()
        setupBrandButtons()
        setupSortMenus()
        refresh()
    }

    // ===== REFRESH RESULTS =====
    // Re-run filters after the Favorite state changes.
    // Reapplies filters and displays the resulting cars.
    fun refresh() {
        restoreSortLabels()
        onResultsChanged(
            CarCatalog.getCars(
                filters = filters,
                favoritesOnly = favoritesOnly
            )
        )
    }

    // ===== SEARCH AND BRAND FILTERS =====
    // Updates results when the search query changes.
    private fun setupSearch() {
        activity.findViewById<EditText>(R.id.et_search)
            .addTextChangedListener(object : TextWatcher {

                // Implements the text watcher callback; no action is needed before editing.
                override fun beforeTextChanged(
                    text: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                // Stores the new search query and refreshes results.
                override fun onTextChanged(
                    text: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    updateFilters(filters.copy(query = text.toString().trim()))
                    refresh()
                }

                // Implements the text watcher callback; no action is needed after editing.
                override fun afterTextChanged(text: Editable?) = Unit
            })
    }

    // Connects brand buttons to the brand filter.
    private fun setupBrandButtons() {
        val brandButtons = listOf(
            R.id.btnPorsche to "Porsche",
            R.id.btnVW to "Volkswagen",
            R.id.btnToyota to "Toyota",
            R.id.btnVinFast to "VinFast"
        )

        brandButtons.forEach { (buttonId, brand) ->
            activity.findViewById<ImageButton>(buttonId)
                .setOnClickListener {
                    updateFilters(filters.copy(
                        brand = if (filters.brand == brand) null else brand
                    ))

                    updateBrandButtonAppearance(brandButtons)
                    refresh()
                }
        }

        updateBrandButtonAppearance(brandButtons)
    }

    // Shows which brand button matches the active filter.
    private fun updateBrandButtonAppearance(
        buttons: List<Pair<Int, String>>
    ) {
        buttons.forEach { (buttonId, brand) -> activity.findViewById<ImageButton>(buttonId).alpha =
                if (filters.brand == null || filters.brand == brand) {
                    1f
                } else {
                    0.45f
                }
        }
    }

    // ===== SORT MENUS =====
    // Connects the rating, cost, and year sort dropdowns.
    private fun setupSortMenus() {
        val ratingMenu = activity.findViewById<AutoCompleteTextView>(R.id.dropdown_top_rate)

        val costMenu = activity.findViewById<AutoCompleteTextView>(R.id.dropdown_cost)

        val yearMenu = activity.findViewById<AutoCompleteTextView>(R.id.dropdown_year)

        setupDropdown(
            dropdown = ratingMenu,
            options = listOf(
                activity.getString(R.string.sort_all),
                activity.getString(R.string.sort_low),
                activity.getString(R.string.sort_high)
            ),
            arrowButtonId = R.id.btnRatingSortArrow
        ) { direction ->
            updateFilters(filters.copy(ratingSort = direction))
            refresh()
        }

        setupDropdown(
            dropdown = costMenu,
            options = listOf(
                activity.getString(R.string.sort_all),
                activity.getString(R.string.sort_low),
                activity.getString(R.string.sort_high)
            ),
            arrowButtonId = R.id.btnCostSortArrow
        ) { direction ->
            updateFilters(filters.copy(costSort = direction))
            refresh()
        }

        setupDropdown(
            dropdown = yearMenu,
            options = listOf(
                activity.getString(R.string.sort_all),
                activity.getString(R.string.sort_old),
                activity.getString(R.string.sort_new)
            ),
            arrowButtonId = R.id.btnYearSortArrow
        ) { direction ->
            updateFilters(filters.copy(yearSort = direction))
            refresh()
        }
    }

    // Creates a sort dropdown and handles its selection.
    private fun setupDropdown(
        dropdown: AutoCompleteTextView,
        options: List<String>,
        arrowButtonId: Int,
        onSelected: (SortDirection?) -> Unit
    ) {
        dropdown.threshold = 0
        dropdown.setAdapter(
            ArrayAdapter(
                activity,
                R.layout.item_sort_option,
                options
            )
        )
        dropdown.setText(activity.getString(R.string.sort_all), false)

        // Tapping the text field or arrow opens the same menu.
        val openMenu = {
            dropdown.setText("", false)
            dropdown.showDropDown()
        }

        dropdown.setOnClickListener {
            openMenu()
        }

        activity.findViewById<ImageButton>(arrowButtonId)
            .setOnClickListener {
                openMenu()
            }

        dropdown.setOnItemClickListener { _, _, position, _ ->
            onSelected(directionFromPosition(position))
        }

        dropdown.setOnDismissListener {
            restoreSortLabels()
        }
    }

    // Converts a dropdown position into a sort direction.
    private fun directionFromPosition(position: Int): SortDirection? {
        return when (position) {
            1 -> SortDirection.LOW_TO_HIGH
            2 -> SortDirection.HIGH_TO_LOW
            else -> null
        }
    }

    // Displays the current choice in each sort dropdown.
    private fun restoreSortLabels() {
        activity.findViewById<AutoCompleteTextView>(R.id.dropdown_top_rate)
            .setText(
                labelFor(
                    filters.ratingSort,
                    activity.getString(R.string.sort_all),
                    activity.getString(R.string.sort_low),
                    activity.getString(R.string.sort_high)
                ),
                false
            )

        activity.findViewById<AutoCompleteTextView>(R.id.dropdown_cost)
            .setText(
                labelFor(
                    filters.costSort,
                    activity.getString(R.string.sort_all),
                    activity.getString(R.string.sort_low),
                    activity.getString(R.string.sort_high)
                ),
                false
            )

        activity.findViewById<AutoCompleteTextView>(R.id.dropdown_year)
            .setText(
                labelFor(
                    filters.yearSort,
                    activity.getString(R.string.sort_all),
                    activity.getString(R.string.sort_old),
                    activity.getString(R.string.sort_new)
                ),
                false
            )
    }

    // Saves filter choices so the screen can restore them after a theme refresh.
    private fun updateFilters(updatedFilters: CarFilters) {
        filters = updatedFilters
        savedFilters[stateKey] = updatedFilters
    }

    // Returns the label for the selected sort direction.
    private fun labelFor(
        direction: SortDirection?,
        allLabel: String,
        lowLabel: String,
        highLabel: String
    ): String {
        return when (direction) {
            SortDirection.LOW_TO_HIGH -> lowLabel
            SortDirection.HIGH_TO_LOW -> highLabel
            null -> allLabel
        }
    }
}
