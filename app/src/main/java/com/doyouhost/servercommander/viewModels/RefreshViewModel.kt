package com.doyouhost.servercommander.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RefreshViewModel : ViewModel() {
    private val refreshEnabled = MutableLiveData<Boolean>(false)
    private val refreshInterval = MutableLiveData<Int>(30)

    val enabled: LiveData<Boolean> get() = refreshEnabled

    val interval: LiveData<Int> get() = refreshInterval

    fun enabled(state: Boolean) {
        refreshEnabled.value = state
    }

    fun interval(interval: Int) {
        refreshInterval.value = interval
    }
}