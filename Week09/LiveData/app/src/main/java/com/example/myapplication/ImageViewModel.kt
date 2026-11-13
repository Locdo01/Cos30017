
package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {

    private val images = listOf(
        R.drawable.ic_assignment_turned_in_24px,
        R.drawable.ic_lightbulb_24px
    )

    private var currentIndex = 0

    private val _image = MutableLiveData<Int>()
    val image: LiveData<Int> = _image

    private val _imageName = MutableLiveData<String>()
    val imageName: LiveData<String> = _imageName

    init {
        updateImage()
    }

    fun nextImage() {
        currentIndex = (currentIndex + 1) % images.size
        updateImage()
    }

    private fun updateImage() {
        _image.value = images[currentIndex]

        _imageName.value = when (currentIndex) {
            0 -> "ic_assignment_turned_in_24px"
            1 -> "ic_lightbulb_24px"
            else -> "Unknown"
        }
    }
}
