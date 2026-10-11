
package com.example.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SampleViewModel : ViewModel() {

    private val _count = MutableLiveData(0)

    val badgeCount: LiveData<Int>
        get() = _count

    fun incrementBadgeCount() {
        _count.value = (_count.value ?: 0) + 1
    }

    override fun onCleared() {
        super.onCleared()
    }
}
