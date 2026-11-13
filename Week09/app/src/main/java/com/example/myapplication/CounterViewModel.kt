
package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CounterViewModel : ViewModel() {

    // Counter value
    private var tapCount = 0

    private val _taps = MutableLiveData<Int>(0)
    val taps: LiveData<Int>
        get() = _taps

    // Button status
    private val _buttonEnabled = MutableLiveData<Boolean>(true)
    val buttonEnabled: LiveData<Boolean>
        get() = _buttonEnabled

    // Message status
    private val _status = MutableLiveData<String>("Ready")
    val status: LiveData<String>
        get() = _status

    // Handle button click
    fun updateTaps() {

        // Prevent multiple clicks
        if (_buttonEnabled.value == false) return

        viewModelScope.launch {

            // Disable button
            _buttonEnabled.value = false
            _status.value = "Processing..."

            // Wait 3 seconds
            delay(3000)

            // Update counter
            tapCount++
            _taps.value = tapCount

            // Enable button
            _buttonEnabled.value = true
            _status.value = "Completed"
        }
    }
}
