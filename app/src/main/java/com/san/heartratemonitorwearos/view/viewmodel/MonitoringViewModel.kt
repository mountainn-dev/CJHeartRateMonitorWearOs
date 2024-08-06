package com.san.heartratemonitorwearos.view.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData

interface MonitoringViewModel {
    val viewModelError: LiveData<Boolean>

    fun urgent(location: Location)
    fun setHeartRate(heartRate: Int)
}