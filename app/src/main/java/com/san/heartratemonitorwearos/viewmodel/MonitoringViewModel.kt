package com.san.heartratemonitorwearos.viewmodel

import androidx.lifecycle.LiveData

interface MonitoringViewModel {
    val viewModelError: LiveData<Boolean>

    fun addHeartRateData(data: Int)
}